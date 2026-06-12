package com.pig4cloud.pig.mes.material.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.mes.core.api.entity.MdBom;
import com.pig4cloud.pig.mes.core.api.entity.MdBomLine;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.material.api.entity.MtLot;
import com.pig4cloud.pig.mes.material.api.entity.MtLotConsume;
import com.pig4cloud.pig.mes.material.mapper.MtLotConsumeMapper;
import com.pig4cloud.pig.mes.material.mapper.MtLotMapper;
import com.pig4cloud.pig.mes.material.service.MtLotService;
import com.pig4cloud.pig.mes.quality.mapper.ExecReadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 批次台账与上料服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MtLotServiceImpl extends ServiceImpl<MtLotMapper, MtLot> implements MtLotService {

	private final MtLotConsumeMapper consumeMapper;

	private final ExecReadMapper.MaterialReadMapper materialReadMapper;

	private final ExecReadMapper.TaskReadMapper taskReadMapper;

	private final ExecReadMapper.OrderReadMapper orderReadMapper;

	private final ExecReadMapper.BomReadMapper bomReadMapper;

	private final ExecReadMapper.BomLineReadMapper bomLineReadMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long registerLot(MtLot lot) {
		MdMaterial material = materialReadMapper.selectById(lot.getMaterialId());
		Assert.notNull(material, "物料不存在");
		Assert.isTrue(lot.getQty() != null && lot.getQty().signum() > 0, "批次数量必须大于 0");
		if (StrUtil.isBlank(lot.getLotNo())) {
			lot.setLotNo(nextLotNo());
		}
		lot.setMaterialCode(material.getMaterialCode());
		lot.setMaterialName(material.getMaterialName());
		lot.setUnit(material.getUnit());
		lot.setLotStatus(StrUtil.blankToDefault(lot.getLotStatus(), "AVAILABLE"));
		lot.setSourceType(StrUtil.blankToDefault(lot.getSourceType(), "PURCHASE"));
		// 保质期推算失效日期
		if (lot.getExpireDate() == null && lot.getProdDate() != null && material.getShelfLifeDays() != null) {
			lot.setExpireDate(lot.getProdDate().plusDays(material.getShelfLifeDays()));
		}
		this.save(lot);
		return lot.getId();
	}

	@Override
	public void changeStatus(Long id, String status) {
		Assert.isTrue(List.of("AVAILABLE", "HOLD", "FROZEN").contains(status), "状态值不合法");
		MtLot lot = this.getById(id);
		Assert.notNull(lot, "批次不存在");
		Assert.isTrue(!"CONSUMED".equals(lot.getLotStatus()), "已耗尽批次不可变更状态");
		lot.setLotStatus(status);
		this.updateById(lot);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void feed(Long taskId, String lotNo, BigDecimal qty) {
		Assert.isTrue(qty != null && qty.signum() > 0, "上料数量必须大于 0");
		WoTask task = taskReadMapper.selectById(taskId);
		Assert.notNull(task, "作业不存在");
		Assert.isTrue(List.of("RUNNING", "PAUSED", "DISPATCHED", "SCHEDULED").contains(task.getTaskStatus()),
				"作业当前状态不允许上料");
		MtLot lot = this.getOne(Wrappers.<MtLot>lambdaQuery().eq(MtLot::getLotNo, lotNo), false);
		Assert.notNull(lot, StrUtil.format("批次 {} 不存在", lotNo));
		// 批次状态防错（R6-2）
		Assert.isTrue("AVAILABLE".equals(lot.getLotStatus()),
				StrUtil.format("批次 {} 状态为 {}，禁止上料", lotNo, lot.getLotStatus()));
		Assert.isTrue(lot.getQty().compareTo(qty) >= 0,
				StrUtil.format("批次余量 {} 不足本次上料 {}", lot.getQty(), qty));
		// 失效日期防错
		Assert.isTrue(lot.getExpireDate() == null || !lot.getExpireDate().isBefore(LocalDate.now()),
				StrUtil.format("批次 {} 已过失效日期 {}", lotNo, lot.getExpireDate()));

		// BOM 物料匹配防错：上料物料须在工单物料当前已发布 BOM 中
		// （工单暂未保存 BOM 快照，此处取当前已发布版本；BOM 快照列为补强项）
		WoOrder order = orderReadMapper.selectById(task.getOrderId());
		Assert.notNull(order, "工单不存在");
		MdBom bom = bomReadMapper.selectOne(Wrappers.<MdBom>lambdaQuery()
			.eq(MdBom::getMaterialId, order.getMaterialId())
			.eq(MdBom::getBomStatus, "RELEASED")
			.last("limit 1"));
		Assert.notNull(bom, "工单物料没有已发布的 BOM，无法校验上料");
		List<Long> bomMaterials = bomLineReadMapper
			.selectList(Wrappers.<MdBomLine>lambdaQuery().eq(MdBomLine::getBomId, bom.getId()))
			.stream()
			.map(MdBomLine::getChildMaterialId)
			.collect(Collectors.toList());
		Assert.isTrue(bomMaterials.contains(lot.getMaterialId()),
				StrUtil.format("物料 {} 不在工单 BOM 用料清单中，禁止上料", lot.getMaterialCode()));

		// 扣减余量并记录消耗（不可变）
		BigDecimal remain = lot.getQty().subtract(qty);
		lot.setQty(remain);
		if (remain.signum() == 0) {
			lot.setLotStatus("CONSUMED");
		}
		this.updateById(lot);

		MtLotConsume consume = new MtLotConsume();
		consume.setTaskId(taskId);
		consume.setOrderId(task.getOrderId());
		consume.setLotId(lot.getId());
		consume.setLotNo(lot.getLotNo());
		consume.setMaterialId(lot.getMaterialId());
		consume.setMaterialCode(lot.getMaterialCode());
		consume.setQty(qty);
		consume.setPersonName(SecurityUtils.getUser() != null ? SecurityUtils.getUser().getUsername() : null);
		consume.setFeedTime(LocalDateTime.now());
		consumeMapper.insert(consume);
	}

	@Override
	public List<MtLotConsume> listConsumes(Long taskId) {
		return consumeMapper.selectList(
				Wrappers.<MtLotConsume>lambdaQuery().eq(MtLotConsume::getTaskId, taskId).orderByAsc(MtLotConsume::getFeedTime));
	}

	private String nextLotNo() {
		String prefix = "LOT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		MtLot last = this.getOne(Wrappers.<MtLot>lambdaQuery()
			.likeRight(MtLot::getLotNo, prefix)
			.orderByDesc(MtLot::getLotNo)
			.last("limit 1"), false);
		int seq = last == null ? 1 : Integer.parseInt(last.getLotNo().substring(prefix.length())) + 1;
		return prefix + String.format("%04d", seq);
	}

}
