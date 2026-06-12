package com.pig4cloud.pig.mes.execution.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.execution.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 生产分析（监控墙 / KPI / Pareto）
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/analytics")
@Tag(description = "analytics", name = "生产分析")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	/**
	 * 设备监控墙
	 * @return 工位状态卡片列表
	 */
	@Operation(description = "设备监控墙", summary = "设备监控墙")
	@GetMapping("/workplace-board")
	@HasPermission("mes_monitor_view")
	public R<List<Map<String, Object>>> workplaceBoard() {
		return R.ok(analyticsService.workplaceBoard());
	}

	/**
	 * 基础 KPI（产量/合格率/生产时长，按工位）
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return KPI 汇总与明细
	 */
	@Operation(description = "基础 KPI", summary = "基础 KPI")
	@GetMapping("/kpi")
	@HasPermission("mes_monitor_view")
	public R<Map<String, Object>> kpi(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return R.ok(analyticsService.kpi(startDate, endDate));
	}

	/**
	 * 报废原因 Pareto
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 原因排序列表
	 */
	@Operation(description = "报废原因 Pareto", summary = "报废原因 Pareto")
	@GetMapping("/scrap-pareto")
	@HasPermission("mes_monitor_view")
	public R<List<Map<String, Object>>> scrapPareto(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return R.ok(analyticsService.scrapPareto(startDate, endDate));
	}

}
