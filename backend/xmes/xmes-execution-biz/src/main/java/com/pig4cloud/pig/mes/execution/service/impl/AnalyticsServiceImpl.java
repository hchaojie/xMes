package com.pig4cloud.pig.mes.execution.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.execution.api.entity.WoBooking;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.execution.mapper.MdReadMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoBookingMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoOrderMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoTaskMapper;
import com.pig4cloud.pig.mes.execution.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 生产分析服务实现（事件流实时推导口径）
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

	private final WoTaskMapper taskMapper;

	private final WoOrderMapper orderMapper;

	private final WoBookingMapper bookingMapper;

	private final MdReadMapper.WorkplaceReadMapper workplaceReadMapper;

	private final MdReadMapper.MaterialReadMapper materialReadMapper;

	@Override
	public List<Map<String, Object>> workplaceBoard() {
		List<MdWorkplace> workplaces = workplaceReadMapper.selectList(
				Wrappers.<MdWorkplace>lambdaQuery().eq(MdWorkplace::getStatus, "0").orderByAsc(MdWorkplace::getSortOrder));
		// 各工位活动作业
		List<WoTask> active = taskMapper.selectList(Wrappers.<WoTask>lambdaQuery()
			.isNotNull(WoTask::getWorkplaceId)
			.in(WoTask::getTaskStatus, List.of("RUNNING", "PAUSED", "DISPATCHED", "SCHEDULED")));
		Map<Long, List<WoTask>> byWp = active.stream().collect(Collectors.groupingBy(WoTask::getWorkplaceId));
		Map<Long, WoOrder> orderMap = active.isEmpty() ? Map.of()
				: orderMapper.selectBatchIds(active.stream().map(WoTask::getOrderId).distinct().toList())
					.stream()
					.collect(Collectors.toMap(WoOrder::getId, Function.identity()));
		Map<Long, MdMaterial> materialMap = orderMap.isEmpty() ? Map.of()
				: materialReadMapper
					.selectBatchIds(orderMap.values().stream().map(WoOrder::getMaterialId).distinct().toList())
					.stream()
					.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));

		// 今日产量（QTY/FINISH/REVERSE 事件聚合）
		LocalDateTime dayStart = LocalDate.now().atStartOfDay();
		Map<Long, BigDecimal[]> todayQty = new HashMap<>();
		bookingMapper
			.selectList(Wrappers.<WoBooking>lambdaQuery()
				.ge(WoBooking::getBookingTime, dayStart)
				.in(WoBooking::getBookingType, List.of("QTY", "FINISH", "REVERSE"))
				.isNotNull(WoBooking::getWorkplaceId))
			.forEach(b -> {
				BigDecimal[] acc = todayQty.computeIfAbsent(b.getWorkplaceId(),
						k -> new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
				acc[0] = acc[0].add(nz(b.getQtyGood()));
				acc[1] = acc[1].add(nz(b.getQtyScrap()));
			});

		List<Map<String, Object>> board = new ArrayList<>();
		for (MdWorkplace wp : workplaces) {
			Map<String, Object> card = new LinkedHashMap<>();
			card.put("workplaceId", wp.getId());
			card.put("code", wp.getWorkplaceCode());
			card.put("name", wp.getWorkplaceName());
			List<WoTask> tasks = byWp.getOrDefault(wp.getId(), List.of());
			WoTask current = tasks.stream()
				.min(Comparator.comparing(t -> "RUNNING".equals(t.getTaskStatus()) ? 0
						: "PAUSED".equals(t.getTaskStatus()) ? 1 : 2))
				.orElse(null);
			String state = current == null ? "IDLE"
					: switch (current.getTaskStatus()) {
						case "RUNNING" -> "RUNNING";
						case "PAUSED" -> "PAUSED";
						default -> "WAITING";
					};
			card.put("state", state);
			if (current != null) {
				WoOrder order = orderMap.get(current.getOrderId());
				card.put("orderNo", order != null ? order.getOrderNo() : null);
				card.put("operationNo", current.getOperationNo());
				card.put("operationName", current.getOperationName());
				MdMaterial material = order != null ? materialMap.get(order.getMaterialId()) : null;
				card.put("materialCode", material != null ? material.getMaterialCode() : null);
				card.put("progress", current.getQtyGood() + "/" + current.getQtyTarget());
			}
			BigDecimal[] acc = todayQty.getOrDefault(wp.getId(),
					new BigDecimal[] { BigDecimal.ZERO, BigDecimal.ZERO });
			card.put("todayGood", acc[0]);
			card.put("todayScrap", acc[1]);
			board.add(card);
		}
		return board;
	}

	@Override
	public Map<String, Object> kpi(LocalDate startDate, LocalDate endDate) {
		LocalDateTime from = startDate.atStartOfDay();
		LocalDateTime to = endDate.plusDays(1).atStartOfDay();
		List<WoBooking> events = bookingMapper.selectList(Wrappers.<WoBooking>lambdaQuery()
			.ge(WoBooking::getBookingTime, from)
			.lt(WoBooking::getBookingTime, to)
			.orderByAsc(WoBooking::getTaskId, WoBooking::getBookingTime, WoBooking::getId));

		Map<Long, MdWorkplace> wpMap = workplaceReadMapper.selectList(Wrappers.emptyWrapper())
			.stream()
			.collect(Collectors.toMap(MdWorkplace::getId, Function.identity()));

		// 按工位聚合数量；生产时长按作业事件配对（START/RESUME → PAUSE/FINISH，窗内近似）
		Map<Long, Map<String, Object>> rows = new LinkedHashMap<>();
		Map<Long, LocalDateTime> openStart = new HashMap<>();
		for (WoBooking b : events) {
			// 未绑定工位的事件归入虚拟分组（key=0），保证汇总口径完整
			Long wpId = b.getWorkplaceId() != null ? b.getWorkplaceId() : 0L;
			{
				Map<String, Object> row = rows.computeIfAbsent(wpId, k -> {
					Map<String, Object> m = new LinkedHashMap<>();
					MdWorkplace wp = wpMap.get(k);
					m.put("workplace", k == 0L ? "(未绑定工位)"
							: wp != null ? wp.getWorkplaceCode() + " " + wp.getWorkplaceName() : String.valueOf(k));
					m.put("good", BigDecimal.ZERO);
					m.put("scrap", BigDecimal.ZERO);
					m.put("rework", BigDecimal.ZERO);
					m.put("runMinutes", 0L);
					return m;
				});
				row.put("good", ((BigDecimal) row.get("good")).add(nz(b.getQtyGood())));
				row.put("scrap", ((BigDecimal) row.get("scrap")).add(nz(b.getQtyScrap())));
				row.put("rework", ((BigDecimal) row.get("rework")).add(nz(b.getQtyRework())));
				// 生产时长配对
				switch (b.getBookingType()) {
					case "START", "RESUME" -> openStart.put(b.getTaskId(), b.getBookingTime());
					case "PAUSE", "FINISH" -> {
						LocalDateTime open = openStart.remove(b.getTaskId());
						if (open != null) {
							long minutes = Duration.between(open, b.getBookingTime()).toMinutes();
							row.put("runMinutes", (Long) row.get("runMinutes") + Math.max(minutes, 0));
						}
					}
					default -> { }
				}
			}
		}

		BigDecimal totalGood = BigDecimal.ZERO;
		BigDecimal totalScrap = BigDecimal.ZERO;
		long totalRun = 0;
		for (Map<String, Object> row : rows.values()) {
			BigDecimal good = (BigDecimal) row.get("good");
			BigDecimal scrap = (BigDecimal) row.get("scrap");
			row.put("fpy", fpy(good, scrap));
			totalGood = totalGood.add(good);
			totalScrap = totalScrap.add(scrap);
			totalRun += (Long) row.get("runMinutes");
		}
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("totalGood", totalGood);
		result.put("totalScrap", totalScrap);
		result.put("totalRunMinutes", totalRun);
		result.put("fpy", fpy(totalGood, totalScrap));
		result.put("rows", new ArrayList<>(rows.values()));
		return result;
	}

	@Override
	public List<Map<String, Object>> scrapPareto(LocalDate startDate, LocalDate endDate) {
		LocalDateTime from = startDate.atStartOfDay();
		LocalDateTime to = endDate.plusDays(1).atStartOfDay();
		Map<String, BigDecimal> byReason = new LinkedHashMap<>();
		bookingMapper
			.selectList(Wrappers.<WoBooking>lambdaQuery()
				.ge(WoBooking::getBookingTime, from)
				.lt(WoBooking::getBookingTime, to)
				.in(WoBooking::getBookingType, List.of("QTY", "FINISH", "REVERSE")))
			.stream()
			.filter(b -> nz(b.getQtyScrap()).signum() != 0)
			.forEach(b -> byReason.merge(Objects.requireNonNullElse(b.getReasonCode(), "未填原因"),
					nz(b.getQtyScrap()), BigDecimal::add));
		return byReason.entrySet()
			.stream()
			.filter(e -> e.getValue().signum() > 0)
			.sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
			.map(e -> Map.<String, Object>of("reason", e.getKey(), "qty", e.getValue()))
			.collect(Collectors.toList());
	}

	private String fpy(BigDecimal good, BigDecimal scrap) {
		BigDecimal total = good.add(scrap);
		if (total.signum() == 0) {
			return "-";
		}
		return good.multiply(BigDecimal.valueOf(100)).divide(total, 1, RoundingMode.HALF_UP) + "%";
	}

	private BigDecimal nz(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

}
