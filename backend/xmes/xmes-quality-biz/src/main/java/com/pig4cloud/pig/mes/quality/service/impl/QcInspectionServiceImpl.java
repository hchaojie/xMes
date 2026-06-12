package com.pig4cloud.pig.mes.quality.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.quality.api.dto.QcInspectionDetailDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcCharacteristic;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionResult;
import com.pig4cloud.pig.mes.quality.api.entity.QcPlan;
import com.pig4cloud.pig.mes.quality.mapper.ExecReadMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcCharacteristicMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcInspectionOrderMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcInspectionResultMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcPlanMapper;
import com.pig4cloud.pig.mes.quality.service.QcInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 检验单服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class QcInspectionServiceImpl extends ServiceImpl<QcInspectionOrderMapper, QcInspectionOrder>
		implements QcInspectionService {

	private final QcPlanMapper planMapper;

	private final QcCharacteristicMapper characteristicMapper;

	private final QcInspectionResultMapper resultMapper;

	private final ExecReadMapper.TaskReadMapper taskReadMapper;

	private final ExecReadMapper.OrderReadMapper orderReadMapper;

	private final ExecReadMapper.MaterialReadMapper materialReadMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int refreshGateOrders() {
		// 已完工的质量门作业
		List<WoTask> gateTasks = taskReadMapper.selectList(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getQualityGate, "1")
			.eq(WoTask::getTaskStatus, "COMPLETED"));
		if (gateTasks.isEmpty()) {
			return 0;
		}
		// 排除已有检验单的作业
		List<Long> existing = this
			.list(Wrappers.<QcInspectionOrder>lambdaQuery()
				.in(QcInspectionOrder::getTaskId, gateTasks.stream().map(WoTask::getId).collect(Collectors.toList())))
			.stream()
			.map(QcInspectionOrder::getTaskId)
			.collect(Collectors.toList());

		int created = 0;
		for (WoTask task : gateTasks) {
			if (existing.contains(task.getId())) {
				continue;
			}
			WoOrder order = orderReadMapper.selectById(task.getOrderId());
			if (order == null) {
				continue;
			}
			MdMaterial material = materialReadMapper.selectById(order.getMaterialId());
			// 匹配检验计划：物料+工序 精确 > 物料 > 工序 > 通用（启用的过程检验计划）
			QcPlan plan = matchPlan(order.getMaterialId(), task.getOperationNo());

			QcInspectionOrder qc = new QcInspectionOrder();
			qc.setInspectionNo(nextNo());
			qc.setPlanId(plan != null ? plan.getId() : null);
			qc.setPlanType(plan != null ? plan.getPlanType() : "PROCESS");
			qc.setWoOrderId(order.getId());
			qc.setTaskId(task.getId());
			qc.setOrderNo(order.getOrderNo());
			qc.setMaterialCode(material != null ? material.getMaterialCode() : null);
			qc.setMaterialName(material != null ? material.getMaterialName() : null);
			qc.setOperationNo(task.getOperationNo());
			qc.setInspectStatus("PENDING");
			qc.setSampleQty(plan != null && "FIXED".equals(plan.getSamplingType()) ? plan.getSampleSize()
					: task.getQtyGood());
			qc.setSourceType("AUTO_GATE");
			this.save(qc);
			created++;
		}
		return created;
	}

	@Override
	public Page<QcInspectionOrder> pageOrders(Page<QcInspectionOrder> page, String status) {
		return this.page(page, Wrappers.<QcInspectionOrder>lambdaQuery()
			.eq(StrUtil.isNotBlank(status), QcInspectionOrder::getInspectStatus, status)
			.orderByDesc(QcInspectionOrder::getCreateTime));
	}

	@Override
	public QcInspectionDetailDTO getDetail(Long id) {
		QcInspectionOrder order = this.getById(id);
		Assert.notNull(order, "检验单不存在");
		QcInspectionDetailDTO dto = BeanUtil.copyProperties(order, QcInspectionDetailDTO.class);
		if (order.getPlanId() != null) {
			dto.setCharacteristics(characteristicMapper.selectList(Wrappers.<QcCharacteristic>lambdaQuery()
				.eq(QcCharacteristic::getPlanId, order.getPlanId())
				.orderByAsc(QcCharacteristic::getSortOrder)));
		}
		dto.setResults(resultMapper.selectList(Wrappers.<QcInspectionResult>lambdaQuery()
			.eq(QcInspectionResult::getInspectionOrderId, id)
			.orderByAsc(QcInspectionResult::getCharacteristicId, QcInspectionResult::getSampleNo)));
		return dto;
	}

	@Override
	public void receive(Long id) {
		QcInspectionOrder order = this.getById(id);
		Assert.notNull(order, "检验单不存在");
		Assert.isTrue("PENDING".equals(order.getInspectStatus()), "仅待检状态可领取");
		order.setInspectStatus("INSPECTING");
		order.setInspector(SecurityUtils.getUser() != null ? SecurityUtils.getUser().getUsername() : null);
		this.updateById(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void record(List<QcInspectionResult> results) {
		Assert.notEmpty(results, "结果不能为空");
		QcInspectionOrder order = this.getById(results.get(0).getInspectionOrderId());
		Assert.notNull(order, "检验单不存在");
		Assert.isTrue("INSPECTING".equals(order.getInspectStatus()), "请先领取检验单（检验中状态才能录入）");

		for (QcInspectionResult r : results) {
			QcCharacteristic c = characteristicMapper.selectById(r.getCharacteristicId());
			Assert.notNull(c, "检验特性不存在");
			// 特性快照
			r.setId(null);
			r.setCharName(c.getCharName());
			r.setCharType(c.getCharType());
			r.setTargetValue(c.getTargetValue());
			r.setUpperLimit(c.getUpperLimit());
			r.setLowerLimit(c.getLowerLimit());
			// 计量特性自动判定；计数/目视由录入者给 judge
			if ("QUANT".equals(c.getCharType())) {
				Assert.notNull(r.getMeasuredValue(), StrUtil.format("特性 {} 必须录入实测值", c.getCharName()));
				boolean ok = (c.getUpperLimit() == null || r.getMeasuredValue().compareTo(c.getUpperLimit()) <= 0)
						&& (c.getLowerLimit() == null || r.getMeasuredValue().compareTo(c.getLowerLimit()) >= 0);
				r.setJudge(ok ? "OK" : "NG");
			}
			else {
				Assert.isTrue("OK".equals(r.getJudge()) || "NG".equals(r.getJudge()),
						StrUtil.format("特性 {} 必须给出 OK/NG 判定", c.getCharName()));
			}
			resultMapper.insert(r);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void judge(Long id, String judge, String judgeRemark) {
		Assert.isTrue(List.of("PASSED", "CONCESSION", "REJECTED").contains(judge), "判定值不合法");
		QcInspectionOrder order = this.getById(id);
		Assert.notNull(order, "检验单不存在");
		Assert.isTrue("INSPECTING".equals(order.getInspectStatus()), "仅检验中的单据可判定");

		Long ngCount = resultMapper.selectCount(Wrappers.<QcInspectionResult>lambdaQuery()
			.eq(QcInspectionResult::getInspectionOrderId, id)
			.eq(QcInspectionResult::getJudge, "NG"));
		if ("PASSED".equals(judge)) {
			Assert.isTrue(ngCount == 0, "存在 NG 结果，不能判定合格（可让步接收或判不合格）");
		}
		if ("CONCESSION".equals(judge) || "REJECTED".equals(judge)) {
			Assert.hasText(judgeRemark, "让步/不合格判定必须填写说明");
		}
		Long resultCount = resultMapper.selectCount(
				Wrappers.<QcInspectionResult>lambdaQuery().eq(QcInspectionResult::getInspectionOrderId, id));
		Assert.isTrue(resultCount > 0, "尚未录入任何检验结果");

		order.setInspectStatus(judge);
		order.setJudgeRemark(judgeRemark);
		order.setJudgeTime(LocalDateTime.now());
		this.updateById(order);
	}

	private QcPlan matchPlan(Long materialId, String operationNo) {
		List<QcPlan> plans = planMapper.selectList(Wrappers.<QcPlan>lambdaQuery()
			.eq(QcPlan::getStatus, "0")
			.eq(QcPlan::getPlanType, "PROCESS")
			.and(w -> w.eq(QcPlan::getMaterialId, materialId).or().isNull(QcPlan::getMaterialId))
			.and(w -> w.eq(QcPlan::getOperationNo, operationNo).or().isNull(QcPlan::getOperationNo)));
		return plans.stream()
			.min((a, b) -> Integer.compare(specificity(b, materialId, operationNo),
					specificity(a, materialId, operationNo)))
			.orElse(null);
	}

	private int specificity(QcPlan p, Long materialId, String operationNo) {
		int s = 0;
		if (materialId.equals(p.getMaterialId())) {
			s += 2;
		}
		if (operationNo.equals(p.getOperationNo())) {
			s += 1;
		}
		return s;
	}

	private String nextNo() {
		String prefix = "QC" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		QcInspectionOrder last = this.getOne(Wrappers.<QcInspectionOrder>lambdaQuery()
			.likeRight(QcInspectionOrder::getInspectionNo, prefix)
			.orderByDesc(QcInspectionOrder::getInspectionNo)
			.last("limit 1"), false);
		int seq = last == null ? 1 : Integer.parseInt(last.getInspectionNo().substring(prefix.length())) + 1;
		return prefix + String.format("%04d", seq);
	}

}
