package com.pig4cloud.pig.mes.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.mes.core.api.dto.MntOrderDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import com.pig4cloud.pig.mes.core.api.entity.MntOrderItem;
import com.pig4cloud.pig.mes.core.api.entity.MntPlan;
import com.pig4cloud.pig.mes.core.mapper.MntOrderItemMapper;
import com.pig4cloud.pig.mes.core.mapper.MntOrderMapper;
import com.pig4cloud.pig.mes.core.service.MdWorkplaceService;
import com.pig4cloud.pig.mes.core.service.MntOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 维护工单服务实现（状态流：PENDING → IN_PROGRESS → REVIEW → CLOSED）
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MntOrderServiceImpl extends ServiceImpl<MntOrderMapper, MntOrder> implements MntOrderService {

	private final MntOrderItemMapper orderItemMapper;

	private final MdWorkplaceService workplaceService;

	@Override
	public Page<MntOrderDTO> pageWithWorkplace(Page<MntOrder> page, MntOrder query) {
		Page<MntOrder> result = this.page(page, Wrappers.<MntOrder>lambdaQuery()
			.eq(StrUtil.isNotBlank(query.getOrderType()), MntOrder::getOrderType, query.getOrderType())
			.eq(StrUtil.isNotBlank(query.getOrderStatus()), MntOrder::getOrderStatus, query.getOrderStatus())
			.eq(query.getWorkplaceId() != null, MntOrder::getWorkplaceId, query.getWorkplaceId())
			.orderByDesc(MntOrder::getId));
		Map<Long, MdWorkplace> wpMap = result.getRecords().isEmpty() ? Map.of()
				: workplaceService
					.listByIds(result.getRecords().stream().map(MntOrder::getWorkplaceId).distinct().toList())
					.stream()
					.collect(Collectors.toMap(MdWorkplace::getId, Function.identity()));
		Page<MntOrderDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
		dtoPage.setRecords(result.getRecords().stream().map(order -> {
			MntOrderDTO dto = BeanUtil.copyProperties(order, MntOrderDTO.class);
			MdWorkplace wp = wpMap.get(order.getWorkplaceId());
			if (wp != null) {
				dto.setWorkplaceCode(wp.getWorkplaceCode());
				dto.setWorkplaceName(wp.getWorkplaceName());
			}
			return dto;
		}).toList());
		return dtoPage;
	}

	@Override
	public MntOrderDTO getDetail(Long id) {
		MntOrder order = this.getById(id);
		Assert.notNull(order, "维护工单不存在");
		MntOrderDTO dto = BeanUtil.copyProperties(order, MntOrderDTO.class);
		dto.setItems(orderItemMapper.selectList(Wrappers.<MntOrderItem>lambdaQuery()
			.eq(MntOrderItem::getOrderId, id)
			.orderByAsc(MntOrderItem::getSortOrder)));
		MdWorkplace wp = workplaceService.getById(order.getWorkplaceId());
		if (wp != null) {
			dto.setWorkplaceCode(wp.getWorkplaceCode());
			dto.setWorkplaceName(wp.getWorkplaceName());
		}
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public MntOrder createFromPlan(MntPlan plan) {
		MntOrder order = new MntOrder();
		order.setOrderNo(nextNo());
		order.setOrderType("SPOT".equals(plan.getPlanType()) ? "SPOT" : "PM");
		order.setPlanId(plan.getId());
		order.setWorkplaceId(plan.getWorkplaceId());
		order.setOrderStatus("PENDING");
		order.setDueDate(plan.getNextDueDate());
		order.setFaultDesc(plan.getPlanName());
		order.setReportBy("SYSTEM");
		order.setReportTime(LocalDateTime.now());
		this.save(order);
		return order;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long reportRepair(MntOrder order) {
		Assert.notNull(order.getWorkplaceId(), "报修设备不能为空");
		Assert.hasText(order.getFaultDesc(), "故障现象不能为空");
		MntOrder repair = new MntOrder();
		repair.setOrderNo(nextNo());
		repair.setOrderType("REPAIR");
		repair.setWorkplaceId(order.getWorkplaceId());
		repair.setOrderStatus("PENDING");
		repair.setDueDate(order.getDueDate());
		repair.setFaultDesc(order.getFaultDesc());
		repair.setRemark(order.getRemark());
		repair.setReportBy(currentUser());
		repair.setReportTime(LocalDateTime.now());
		this.save(repair);
		return repair.getId();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void accept(Long id) {
		MntOrder order = this.getById(id);
		Assert.notNull(order, "维护工单不存在");
		Assert.isTrue("PENDING".equals(order.getOrderStatus()), "仅待接单工单可接单");
		order.setOrderStatus("IN_PROGRESS");
		order.setAcceptBy(currentUser());
		order.setAcceptTime(LocalDateTime.now());
		this.updateById(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void finish(MntOrderDTO dto) {
		MntOrder order = this.getById(dto.getId());
		Assert.notNull(order, "维护工单不存在");
		Assert.isTrue("IN_PROGRESS".equals(order.getOrderStatus()), "仅维修中工单可完工");
		// 检查项结果回写 + 点检/保养 NG 项自动报修（FR-RES-36）
		List<MntOrderItem> existing = orderItemMapper
			.selectList(Wrappers.<MntOrderItem>lambdaQuery().eq(MntOrderItem::getOrderId, order.getId()));
		Map<Long, MntOrderItem> existingMap = existing.stream()
			.collect(Collectors.toMap(MntOrderItem::getId, Function.identity()));
		if (dto.getItems() != null) {
			for (MntOrderItem input : dto.getItems()) {
				MntOrderItem item = input.getId() != null ? existingMap.get(input.getId()) : null;
				if (item == null) {
					continue;
				}
				Assert.isTrue(StrUtil.isNotBlank(input.getCheckResult()), "检查项【" + item.getItemName() + "】未录入结果");
				item.setCheckResult(input.getCheckResult());
				item.setResultRemark(input.getResultRemark());
				if ("NG".equals(input.getCheckResult()) && item.getRepairOrderId() == null
						&& !"REPAIR".equals(order.getOrderType())) {
					MntOrder repair = new MntOrder();
					repair.setWorkplaceId(order.getWorkplaceId());
					repair.setFaultDesc("[" + order.getOrderNo() + "] 检查项 NG：" + item.getItemName()
							+ (StrUtil.isNotBlank(input.getResultRemark()) ? "（" + input.getResultRemark() + "）" : ""));
					Long repairId = reportRepair(repair);
					this.update(Wrappers.<MntOrder>lambdaUpdate()
						.eq(MntOrder::getId, repairId)
						.set(MntOrder::getSourceOrderId, order.getId()));
					item.setRepairOrderId(repairId);
				}
				orderItemMapper.updateById(item);
			}
		}
		long unfilled = existing.stream()
			.filter(item -> dto.getItems() == null
					|| dto.getItems().stream().noneMatch(i -> item.getId().equals(i.getId())))
			.count();
		Assert.isTrue(unfilled == 0, "存在未录入结果的检查项");
		order.setOrderStatus("REVIEW");
		order.setFaultCause(dto.getFaultCause());
		order.setActionTaken(dto.getActionTaken());
		order.setSpareParts(dto.getSpareParts());
		order.setFinishTime(LocalDateTime.now());
		if (order.getAcceptTime() != null) {
			order.setDowntimeMinutes((int) Duration.between(order.getAcceptTime(), order.getFinishTime()).toMinutes());
		}
		this.updateById(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void close(Long id) {
		MntOrder order = this.getById(id);
		Assert.notNull(order, "维护工单不存在");
		Assert.isTrue("REVIEW".equals(order.getOrderStatus()), "仅待验证工单可关闭");
		order.setOrderStatus("CLOSED");
		order.setCloseBy(currentUser());
		order.setCloseTime(LocalDateTime.now());
		this.updateById(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void cancel(Long id) {
		MntOrder order = this.getById(id);
		Assert.notNull(order, "维护工单不存在");
		Assert.isTrue("PENDING".equals(order.getOrderStatus()), "仅待接单工单可取消");
		order.setOrderStatus("CANCELLED");
		this.updateById(order);
	}

	@Override
	public Map<String, Object> analysis(LocalDate startDate, LocalDate endDate) {
		LocalDateTime start = startDate.atStartOfDay();
		LocalDateTime end = endDate.plusDays(1).atStartOfDay();
		List<MntOrder> orders = this.list(Wrappers.<MntOrder>lambdaQuery()
			.ge(MntOrder::getReportTime, start)
			.lt(MntOrder::getReportTime, end)
			.ne(MntOrder::getOrderStatus, "CANCELLED"));
		Map<Long, MdWorkplace> wpMap = orders.isEmpty() ? Map.of()
				: workplaceService.listByIds(orders.stream().map(MntOrder::getWorkplaceId).distinct().toList())
					.stream()
					.collect(Collectors.toMap(MdWorkplace::getId, Function.identity()));

		// MTBF/MTTR：按设备统计维修单（MTTR=平均停机时长；MTBF=期间时长/故障次数）
		long periodMinutes = Duration.between(start, end).toMinutes();
		List<Map<String, Object>> byWorkplace = new ArrayList<>();
		orders.stream()
			.filter(o -> "REPAIR".equals(o.getOrderType()))
			.collect(Collectors.groupingBy(MntOrder::getWorkplaceId, LinkedHashMap::new, Collectors.toList()))
			.forEach((wpId, list) -> {
				Map<String, Object> row = new LinkedHashMap<>();
				MdWorkplace wp = wpMap.get(wpId);
				row.put("workplaceId", wpId);
				row.put("workplaceCode", wp != null ? wp.getWorkplaceCode() : null);
				row.put("workplaceName", wp != null ? wp.getWorkplaceName() : null);
				int failures = list.size();
				long downtime = list.stream()
					.filter(o -> o.getDowntimeMinutes() != null)
					.mapToLong(MntOrder::getDowntimeMinutes)
					.sum();
				long repaired = list.stream().filter(o -> o.getDowntimeMinutes() != null).count();
				row.put("failureCount", failures);
				row.put("downtimeMinutes", downtime);
				row.put("mttrMinutes", repaired > 0 ? downtime / repaired : null);
				row.put("mtbfMinutes", failures > 0 ? (periodMinutes - downtime) / failures : null);
				byWorkplace.add(row);
			});

		// 故障 Pareto（按故障原因，未填原因的按现象归组）
		List<Map<String, Object>> pareto = orders.stream()
			.filter(o -> "REPAIR".equals(o.getOrderType()))
			.collect(Collectors.groupingBy(
					o -> StrUtil.isNotBlank(o.getFaultCause()) ? o.getFaultCause()
							: StrUtil.blankToDefault(o.getFaultDesc(), "未知"),
					LinkedHashMap::new, Collectors.counting()))
			.entrySet()
			.stream()
			.sorted(Map.Entry.<String, Long>comparingByValue().reversed())
			.map(e -> {
				Map<String, Object> row = new LinkedHashMap<>();
				row.put("cause", e.getKey());
				row.put("count", e.getValue());
				return row;
			})
			.collect(Collectors.toList());

		// 保养计划达成率：期间生成的 PM/SPOT 工单中按期（due_date 前）关闭的比例
		List<MntOrder> planned = orders.stream().filter(o -> !"REPAIR".equals(o.getOrderType())).toList();
		long onTime = planned.stream()
			.filter(o -> "CLOSED".equals(o.getOrderStatus()) && (o.getDueDate() == null || (o.getFinishTime() != null
					&& !o.getFinishTime().toLocalDate().isAfter(o.getDueDate()))))
			.count();

		Map<String, Object> result = new LinkedHashMap<>();
		result.put("byWorkplace", byWorkplace);
		result.put("faultPareto", pareto);
		result.put("plannedTotal", planned.size());
		result.put("plannedOnTime", onTime);
		result.put("planAchievementRate",
				planned.isEmpty() ? null : Math.round(onTime * 10000.0 / planned.size()) / 100.0);
		return result;
	}

	private String nextNo() {
		String prefix = "MNT" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		MntOrder last = this.getOne(Wrappers.<MntOrder>lambdaQuery()
			.likeRight(MntOrder::getOrderNo, prefix)
			.orderByDesc(MntOrder::getOrderNo)
			.last("limit 1"), false);
		int seq = 0;
		if (last != null && last.getOrderNo().length() > prefix.length()) {
			seq = Integer.parseInt(last.getOrderNo().substring(prefix.length()));
		}
		return prefix + String.format("%04d", seq + 1);
	}

	private String currentUser() {
		return SecurityUtils.getUser() != null ? SecurityUtils.getUser().getUsername() : null;
	}

}
