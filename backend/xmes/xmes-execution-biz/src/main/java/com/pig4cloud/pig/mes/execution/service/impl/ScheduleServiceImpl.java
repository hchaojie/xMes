package com.pig4cloud.pig.mes.execution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleAssignDTO;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.execution.mapper.MdReadMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoOrderMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoTaskMapper;
import com.pig4cloud.pig.mes.execution.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 作业排程服务实现（人工交互式排程）
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

	private static final List<String> SCHEDULABLE = List.of("PENDING", "SCHEDULED");

	private static final List<String> OCCUPYING = List.of("SCHEDULED", "DISPATCHED", "RUNNING", "PAUSED");

	private final WoTaskMapper taskMapper;

	private final WoOrderMapper orderMapper;

	private final MdReadMapper.WorkCenterReadMapper workCenterReadMapper;

	private final MdReadMapper.WorkplaceReadMapper workplaceReadMapper;

	private final MdReadMapper.MaterialReadMapper materialReadMapper;

	private final MdReadMapper.CalendarDayReadMapper calendarDayReadMapper;

	@Override
	public ScheduleBoardVO board(LocalDate startDate, LocalDate endDate, Long workCenterId) {
		ScheduleBoardVO vo = new ScheduleBoardVO();

		// 资源行：工作中心 → 工位
		List<MdWorkCenter> centers = workCenterReadMapper.selectList(Wrappers.<MdWorkCenter>lambdaQuery()
			.eq(workCenterId != null, MdWorkCenter::getId, workCenterId)
			.eq(MdWorkCenter::getStatus, "0")
			.orderByAsc(MdWorkCenter::getSortOrder));
		List<Long> centerIds = centers.stream().map(MdWorkCenter::getId).collect(Collectors.toList());
		Map<Long, List<MdWorkplace>> wpByCenter = centerIds.isEmpty() ? Map.of()
				: workplaceReadMapper
					.selectList(Wrappers.<MdWorkplace>lambdaQuery()
						.in(MdWorkplace::getWorkCenterId, centerIds)
						.eq(MdWorkplace::getStatus, "0")
						.orderByAsc(MdWorkplace::getSortOrder))
					.stream()
					.collect(Collectors.groupingBy(MdWorkplace::getWorkCenterId));
		vo.setWorkCenters(centers.stream().map(c -> {
			ScheduleBoardVO.WorkCenterRow row = new ScheduleBoardVO.WorkCenterRow();
			row.setId(c.getId());
			row.setCode(c.getCenterCode());
			row.setName(c.getCenterName());
			row.setWorkplaces(wpByCenter.getOrDefault(c.getId(), List.of()).stream().map(w -> {
				ScheduleBoardVO.WorkplaceRow wr = new ScheduleBoardVO.WorkplaceRow();
				wr.setId(w.getId());
				wr.setCode(w.getWorkplaceCode());
				wr.setName(w.getWorkplaceName());
				return wr;
			}).collect(Collectors.toList()));
			return row;
		}).collect(Collectors.toList()));

		// 时间窗内已排程作业
		List<WoTask> scheduled = taskMapper.selectList(Wrappers.<WoTask>lambdaQuery()
			.in(WoTask::getTaskStatus, OCCUPYING)
			.isNotNull(WoTask::getPlanStart)
			.lt(WoTask::getPlanStart, endDate.plusDays(1).atStartOfDay())
			.gt(WoTask::getPlanEnd, startDate.atStartOfDay()));

		// 待排清单：已下达/执行中工单的 PENDING 作业
		List<WoOrder> activeOrders = orderMapper.selectList(
				Wrappers.<WoOrder>lambdaQuery().in(WoOrder::getOrderStatus, List.of("RELEASED", "IN_PROGRESS")));
		Map<Long, WoOrder> orderMap = activeOrders.stream()
			.collect(Collectors.toMap(WoOrder::getId, Function.identity()));
		List<WoTask> pending = orderMap.isEmpty() ? List.of()
				: taskMapper.selectList(Wrappers.<WoTask>lambdaQuery()
					.in(WoTask::getOrderId, orderMap.keySet())
					.eq(WoTask::getTaskStatus, "PENDING")
					.eq(workCenterId != null, WoTask::getWorkCenterId, workCenterId)
					.orderByAsc(WoTask::getOrderId, WoTask::getSortOrder));

		// 展示字段补全（工单号/物料/优先级）
		Map<Long, WoOrder> allOrderMap = loadOrders(scheduled, orderMap);
		Map<Long, MdMaterial> materialMap = loadMaterials(allOrderMap);
		vo.setTasks(scheduled.stream().map(t -> toBar(t, allOrderMap, materialMap)).collect(Collectors.toList()));
		vo.setBacklog(pending.stream().map(t -> toBar(t, allOrderMap, materialMap)).collect(Collectors.toList()));

		// 非工作日（启用日历的 REST/HOLIDAY）
		vo.setNonWorkDays(calendarDayReadMapper
			.selectList(Wrappers.<MdCalendarDay>lambdaQuery()
				.ge(MdCalendarDay::getCalDate, startDate)
				.le(MdCalendarDay::getCalDate, endDate)
				.ne(MdCalendarDay::getDayType, "WORK"))
			.stream()
			.map(MdCalendarDay::getCalDate)
			.distinct()
			.sorted()
			.collect(Collectors.toList()));
		return vo;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void assign(ScheduleAssignDTO dto) {
		WoTask task = taskMapper.selectById(dto.getTaskId());
		Assert.notNull(task, "作业不存在");
		Assert.isTrue(SCHEDULABLE.contains(task.getTaskStatus()), "仅待排/已排程的作业可调整排程");
		MdWorkplace workplace = workplaceReadMapper.selectById(dto.getWorkplaceId());
		Assert.notNull(workplace, "工位不存在");

		// 结束时间 = 开始 + 准备时间 + 数量 × 单件时间（分钟）
		BigDecimal minutes = nz(task.getSetupTime())
			.add(nz(task.getUnitTime()).multiply(nz(task.getQtyTarget())));
		LocalDateTime planEnd = dto.getPlanStart().plusMinutes(Math.max(minutes.longValue(), 1));

		// 有限产能：同工位时间重叠拒绝
		Long overlap = taskMapper.selectCount(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getWorkplaceId, dto.getWorkplaceId())
			.in(WoTask::getTaskStatus, OCCUPYING)
			.ne(WoTask::getId, task.getId())
			.lt(WoTask::getPlanStart, planEnd)
			.gt(WoTask::getPlanEnd, dto.getPlanStart()));
		Assert.isTrue(overlap == 0,
				StrUtil.format("工位 {} 在该时段已有排程（有限产能模式不允许重叠）", workplace.getWorkplaceCode()));

		// 工序顺序：不得早于上道作业排程结束
		WoTask previous = taskMapper.selectOne(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getOrderId, task.getOrderId())
			.lt(WoTask::getSortOrder, task.getSortOrder())
			.orderByDesc(WoTask::getSortOrder)
			.last("limit 1"));
		if (previous != null && previous.getPlanEnd() != null) {
			Assert.isTrue(!dto.getPlanStart().isBefore(previous.getPlanEnd()),
					StrUtil.format("开始时间早于上道工序 {} 的排程结束时间 {}", previous.getOperationNo(),
							previous.getPlanEnd()));
		}

		task.setWorkplaceId(dto.getWorkplaceId());
		task.setPlanStart(dto.getPlanStart());
		task.setPlanEnd(planEnd);
		task.setTaskStatus("SCHEDULED");
		taskMapper.updateById(task);
	}

	@Override
	public void unassign(Long taskId) {
		WoTask task = taskMapper.selectById(taskId);
		Assert.notNull(task, "作业不存在");
		Assert.isTrue("SCHEDULED".equals(task.getTaskStatus()) || "DISPATCHED".equals(task.getTaskStatus()),
				"仅已排程/已派工的作业可取消排程");
		taskMapper.update(null, Wrappers.<WoTask>lambdaUpdate()
			.eq(WoTask::getId, taskId)
			.set(WoTask::getTaskStatus, "PENDING")
			.set(WoTask::getWorkplaceId, null)
			.set(WoTask::getPlanStart, null)
			.set(WoTask::getPlanEnd, null));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void dispatch(List<Long> taskIds) {
		List<WoTask> tasks = taskMapper.selectBatchIds(taskIds);
		Assert.isTrue(tasks.size() == taskIds.size(), "存在无效的作业ID");
		for (WoTask task : tasks) {
			Assert.isTrue("SCHEDULED".equals(task.getTaskStatus()),
					StrUtil.format("作业 {} 非已排程状态，无法派工", task.getOperationNo()));
			task.setTaskStatus("DISPATCHED");
			taskMapper.updateById(task);
		}
	}

	private Map<Long, WoOrder> loadOrders(List<WoTask> scheduled, Map<Long, WoOrder> known) {
		List<Long> missing = scheduled.stream()
			.map(WoTask::getOrderId)
			.filter(id -> !known.containsKey(id))
			.distinct()
			.collect(Collectors.toList());
		if (missing.isEmpty()) {
			return known;
		}
		Map<Long, WoOrder> all = new java.util.HashMap<>(known);
		orderMapper.selectBatchIds(missing).forEach(o -> all.put(o.getId(), o));
		return all;
	}

	private Map<Long, MdMaterial> loadMaterials(Map<Long, WoOrder> orderMap) {
		List<Long> ids = orderMap.values()
			.stream()
			.map(WoOrder::getMaterialId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		return ids.isEmpty() ? Map.of() : materialReadMapper.selectBatchIds(ids)
			.stream()
			.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));
	}

	private ScheduleBoardVO.TaskBar toBar(WoTask task, Map<Long, WoOrder> orderMap,
			Map<Long, MdMaterial> materialMap) {
		ScheduleBoardVO.TaskBar bar = BeanUtil.copyProperties(task, ScheduleBoardVO.TaskBar.class);
		WoOrder order = orderMap.get(task.getOrderId());
		if (order != null) {
			bar.setOrderNo(order.getOrderNo());
			bar.setOrderPriority(order.getPriority());
			bar.setOrderPlanEndDate(order.getPlanEndDate());
			MdMaterial material = materialMap.get(order.getMaterialId());
			if (material != null) {
				bar.setMaterialCode(material.getMaterialCode());
				bar.setMaterialName(material.getMaterialName());
			}
		}
		return bar;
	}

	private BigDecimal nz(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

}
