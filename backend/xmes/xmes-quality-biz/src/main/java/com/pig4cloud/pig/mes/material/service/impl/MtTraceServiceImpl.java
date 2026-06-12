package com.pig4cloud.pig.mes.material.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.material.api.entity.MtGenealogy;
import com.pig4cloud.pig.mes.material.api.entity.MtLot;
import com.pig4cloud.pig.mes.material.api.entity.MtLotConsume;
import com.pig4cloud.pig.mes.material.api.entity.MtSerialUnit;
import com.pig4cloud.pig.mes.material.mapper.MtGenealogyMapper;
import com.pig4cloud.pig.mes.material.mapper.MtLotConsumeMapper;
import com.pig4cloud.pig.mes.material.mapper.MtLotMapper;
import com.pig4cloud.pig.mes.material.mapper.MtSerialUnitMapper;
import com.pig4cloud.pig.mes.material.service.MtTraceService;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.mapper.ExecReadMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcInspectionOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 产出生成与追溯服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MtTraceServiceImpl implements MtTraceService {

	private final MtLotMapper lotMapper;

	private final MtSerialUnitMapper serialMapper;

	private final MtLotConsumeMapper consumeMapper;

	private final MtGenealogyMapper genealogyMapper;

	private final QcInspectionOrderMapper qcOrderMapper;

	private final ExecReadMapper.OrderReadMapper orderReadMapper;

	private final ExecReadMapper.MaterialReadMapper materialReadMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int refreshOutputs() {
		List<WoOrder> completed = orderReadMapper.selectList(
				Wrappers.<WoOrder>lambdaQuery().in(WoOrder::getOrderStatus, List.of("COMPLETED", "CLOSED")));
		if (completed.isEmpty()) {
			return 0;
		}
		// 已有产出（批次或序列件）的工单跳过
		List<Long> doneByLot = lotMapper
			.selectList(Wrappers.<MtLot>lambdaQuery().in(MtLot::getWoOrderId,
					completed.stream().map(WoOrder::getId).collect(Collectors.toList())))
			.stream()
			.map(MtLot::getWoOrderId)
			.collect(Collectors.toList());
		List<Long> doneBySerial = serialMapper
			.selectList(Wrappers.<MtSerialUnit>lambdaQuery().in(MtSerialUnit::getWoOrderId,
					completed.stream().map(WoOrder::getId).collect(Collectors.toList())))
			.stream()
			.map(MtSerialUnit::getWoOrderId)
			.collect(Collectors.toList());

		int created = 0;
		for (WoOrder order : completed) {
			if (doneByLot.contains(order.getId()) || doneBySerial.contains(order.getId())
					|| order.getQtyGood().signum() <= 0) {
				continue;
			}
			MdMaterial material = materialReadMapper.selectById(order.getMaterialId());
			if (material == null || "NONE".equals(material.getLotStrategy())) {
				continue;
			}
			// 该工单全部上料消耗 → 谱系父项
			List<MtLotConsume> consumes = consumeMapper
				.selectList(Wrappers.<MtLotConsume>lambdaQuery().eq(MtLotConsume::getOrderId, order.getId()));

			if ("SERIAL".equals(material.getLotStrategy())) {
				// 一物一码：按良品数逐件生成序列件
				int count = order.getQtyGood().intValue();
				for (int i = 1; i <= count; i++) {
					MtSerialUnit unit = new MtSerialUnit();
					unit.setSerialNo(order.getOrderNo() + "-" + String.format("%04d", i));
					unit.setMaterialId(order.getMaterialId());
					unit.setMaterialCode(material.getMaterialCode());
					unit.setWoOrderId(order.getId());
					unit.setSerialStatus("OK");
					serialMapper.insert(unit);
					for (MtLotConsume c : consumes) {
						insertGenealogy(c.getLotId(), null, unit.getId(), order.getId(), c.getQty());
					}
				}
			}
			else {
				// 批次型产出
				MtLot lot = new MtLot();
				lot.setLotNo(nextOutputLotNo());
				lot.setMaterialId(order.getMaterialId());
				lot.setMaterialCode(material.getMaterialCode());
				lot.setMaterialName(material.getMaterialName());
				lot.setQty(order.getQtyGood());
				lot.setUnit(order.getUnit());
				lot.setLotStatus("AVAILABLE");
				lot.setSourceType("PRODUCTION");
				lot.setWoOrderId(order.getId());
				lot.setProdDate(LocalDate.now());
				lotMapper.insert(lot);
				for (MtLotConsume c : consumes) {
					insertGenealogy(c.getLotId(), lot.getId(), null, order.getId(), c.getQty());
				}
			}
			created++;
		}
		return created;
	}

	@Override
	public Map<String, Object> traceBackward(String no) {
		Map<String, Object> result = new LinkedHashMap<>();
		Long orderId;
		MtSerialUnit serial = serialMapper
			.selectOne(Wrappers.<MtSerialUnit>lambdaQuery().eq(MtSerialUnit::getSerialNo, no), false);
		if (serial != null) {
			result.put("type", "SERIAL");
			result.put("serialNo", serial.getSerialNo());
			result.put("materialCode", serial.getMaterialCode());
			result.put("status", serial.getSerialStatus());
			orderId = serial.getWoOrderId();
		}
		else {
			MtLot lot = lotMapper.selectOne(Wrappers.<MtLot>lambdaQuery().eq(MtLot::getLotNo, no), false);
			Assert.notNull(lot, "未找到该批次号/序列号");
			result.put("type", "LOT");
			result.put("lotNo", lot.getLotNo());
			result.put("materialCode", lot.getMaterialCode());
			result.put("qty", lot.getQty());
			result.put("status", lot.getLotStatus());
			orderId = lot.getWoOrderId();
		}
		if (orderId == null) {
			result.put("note", "采购批次，无生产履历");
			return result;
		}
		WoOrder order = orderReadMapper.selectById(orderId);
		if (order != null) {
			Map<String, Object> production = new LinkedHashMap<>();
			production.put("orderNo", order.getOrderNo());
			production.put("routingVersion", order.getRoutingVersion());
			production.put("qtyGood", order.getQtyGood());
			production.put("qtyScrap", order.getQtyScrap());
			result.put("production", production);
		}
		// 消耗批次（投入料）
		result.put("consumedLots",
				consumeMapper.selectList(Wrappers.<MtLotConsume>lambdaQuery().eq(MtLotConsume::getOrderId, orderId))
					.stream()
					.map(c -> Map.of("lotNo", c.getLotNo(), "materialCode", c.getMaterialCode(), "qty", c.getQty(),
							"person", c.getPersonName() == null ? "" : c.getPersonName()))
					.collect(Collectors.toList()));
		// 检验记录
		result.put("inspections",
				qcOrderMapper
					.selectList(Wrappers.<QcInspectionOrder>lambdaQuery().eq(QcInspectionOrder::getWoOrderId, orderId))
					.stream()
					.map(q -> Map.of("inspectionNo", q.getInspectionNo(), "operationNo",
							q.getOperationNo() == null ? "" : q.getOperationNo(), "status", q.getInspectStatus(),
							"inspector", q.getInspector() == null ? "" : q.getInspector()))
					.collect(Collectors.toList()));
		return result;
	}

	@Override
	public Map<String, Object> traceForward(String lotNo) {
		MtLot lot = lotMapper.selectOne(Wrappers.<MtLot>lambdaQuery().eq(MtLot::getLotNo, lotNo), false);
		Assert.notNull(lot, "批次不存在");
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("lotNo", lot.getLotNo());
		result.put("materialCode", lot.getMaterialCode());

		List<MtGenealogy> links = genealogyMapper
			.selectList(Wrappers.<MtGenealogy>lambdaQuery().eq(MtGenealogy::getParentLotId, lot.getId()));
		List<Map<String, Object>> outputs = new ArrayList<>();
		for (MtGenealogy g : links) {
			Map<String, Object> node = new LinkedHashMap<>();
			WoOrder order = g.getOrderId() != null ? orderReadMapper.selectById(g.getOrderId()) : null;
			node.put("orderNo", order != null ? order.getOrderNo() : "");
			if (g.getChildLotId() != null) {
				MtLot child = lotMapper.selectById(g.getChildLotId());
				if (child != null) {
					node.put("outputType", "LOT");
					node.put("no", child.getLotNo());
					node.put("materialCode", child.getMaterialCode());
				}
			}
			else if (g.getChildSerialId() != null) {
				MtSerialUnit child = serialMapper.selectById(g.getChildSerialId());
				if (child != null) {
					node.put("outputType", "SERIAL");
					node.put("no", child.getSerialNo());
					node.put("materialCode", child.getMaterialCode());
				}
			}
			outputs.add(node);
		}
		result.put("outputs", outputs);
		result.put("outputCount", outputs.size());
		return result;
	}

	private void insertGenealogy(Long parentLotId, Long childLotId, Long childSerialId, Long orderId,
			java.math.BigDecimal qty) {
		MtGenealogy g = new MtGenealogy();
		g.setParentLotId(parentLotId);
		g.setChildLotId(childLotId);
		g.setChildSerialId(childSerialId);
		g.setOrderId(orderId);
		g.setQty(qty);
		genealogyMapper.insert(g);
	}

	private String nextOutputLotNo() {
		String prefix = "PL" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		MtLot last = lotMapper.selectOne(Wrappers.<MtLot>lambdaQuery()
			.likeRight(MtLot::getLotNo, prefix)
			.orderByDesc(MtLot::getLotNo)
			.last("limit 1"), false);
		int seq = last == null ? 1 : Integer.parseInt(last.getLotNo().substring(prefix.length())) + 1;
		return prefix + String.format("%04d", seq);
	}

}
