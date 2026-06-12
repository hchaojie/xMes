package com.pig4cloud.pig.mes.execution.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 生产分析服务（监控墙 / 基础 KPI / 报废 Pareto）。
 * <p>
 * 首期基于报工事件流实时推导；事件量增大后应改为按班次结算的结果记录表
 * （对象模型 ViPR Results 层），列为 KPI 切片补强项。
 *
 * @author xmes
 */
public interface AnalyticsService {

	/**
	 * 设备监控墙：每工位当前状态（运行/暂停/待机/空闲）、当前作业、今日产量
	 * @return 工位卡片列表
	 */
	List<Map<String, Object>> workplaceBoard();

	/**
	 * 基础 KPI：时间窗内按工位聚合 良/废/返、一次合格率、生产时长（分钟）
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 汇总 + 工位明细
	 */
	Map<String, Object> kpi(LocalDate startDate, LocalDate endDate);

	/**
	 * 报废原因 Pareto（含冲销抵消）
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 原因 → 数量（降序）
	 */
	List<Map<String, Object>> scrapPareto(LocalDate startDate, LocalDate endDate);

}
