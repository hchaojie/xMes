package com.pig4cloud.pig.mes.execution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.execution.mapper.MdReadMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoOrderMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoTaskMapper;
import com.pig4cloud.pig.mes.execution.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工位终端服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

	private final WoTaskMapper taskMapper;

	private final WoOrderMapper orderMapper;

	private final MdReadMapper.MaterialReadMapper materialReadMapper;

	@Override
	public List<ScheduleBoardVO.TaskBar> queue(Long workplaceId) {
		List<WoTask> tasks = taskMapper.selectList(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getWorkplaceId, workplaceId)
			.in(WoTask::getTaskStatus, List.of("SCHEDULED", "DISPATCHED", "RUNNING", "PAUSED")));
		if (tasks.isEmpty()) {
			return List.of();
		}
		Map<Long, WoOrder> orderMap = orderMapper
			.selectBatchIds(tasks.stream().map(WoTask::getOrderId).distinct().collect(Collectors.toList()))
			.stream()
			.collect(Collectors.toMap(WoOrder::getId, Function.identity()));
		// 挂起工单的作业不进入终端队列
		tasks = tasks.stream().filter(t -> {
			WoOrder order = orderMap.get(t.getOrderId());
			return order != null && !"HOLD".equals(order.getOrderStatus());
		}).collect(Collectors.toList());

		List<Long> materialIds = tasks.stream()
			.map(t -> orderMap.get(t.getOrderId()))
			.filter(Objects::nonNull)
			.map(WoOrder::getMaterialId)
			.distinct()
			.collect(Collectors.toList());
		Map<Long, MdMaterial> materialMap = materialIds.isEmpty() ? Map.of()
				: materialReadMapper.selectBatchIds(materialIds)
					.stream()
					.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));

		return tasks.stream().map(t -> {
			ScheduleBoardVO.TaskBar bar = BeanUtil.copyProperties(t, ScheduleBoardVO.TaskBar.class);
			WoOrder order = orderMap.get(t.getOrderId());
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
		})
			.sorted(Comparator
				.comparing((ScheduleBoardVO.TaskBar b) -> "RUNNING".equals(b.getTaskStatus()) ? 0
						: "PAUSED".equals(b.getTaskStatus()) ? 1 : 2)
				.thenComparing(b -> b.getPlanStart() != null ? b.getPlanStart() : java.time.LocalDateTime.MAX))
			.collect(Collectors.toList());
	}

}
