package com.pig4cloud.pig.mes.execution.api.dto;

import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 排程看板数据（甘特视图）
 *
 * @author xmes
 */
@Data
@Schema(description = "排程看板数据")
public class ScheduleBoardVO {

	/** 资源行：工作中心 → 工位 */
	@Schema(description = "资源行：工作中心 → 工位")
	private List<WorkCenterRow> workCenters;

	/** 时间窗内已排程/执行中的作业 */
	@Schema(description = "时间窗内已排程的作业")
	private List<TaskBar> tasks;

	/** 待排清单（已下达工单的未排程作业） */
	@Schema(description = "待排清单")
	private List<TaskBar> backlog;

	/** 非工作日（底色渲染） */
	@Schema(description = "非工作日")
	private List<LocalDate> nonWorkDays;

	/**
	 * 工作中心行
	 */
	@Data
	@Schema(description = "工作中心行")
	public static class WorkCenterRow {

		/** 工作中心ID */
		@Schema(description = "工作中心ID")
		private Long id;

		/** 编码 */
		@Schema(description = "编码")
		private String code;

		/** 名称 */
		@Schema(description = "名称")
		private String name;

		/** 工位列表 */
		@Schema(description = "工位列表")
		private List<WorkplaceRow> workplaces;

	}

	/**
	 * 工位行
	 */
	@Data
	@Schema(description = "工位行")
	public static class WorkplaceRow {

		/** 工位ID */
		@Schema(description = "工位ID")
		private Long id;

		/** 编码 */
		@Schema(description = "编码")
		private String code;

		/** 名称 */
		@Schema(description = "名称")
		private String name;

	}

	/**
	 * 作业条（甘特条/待排卡片共用）
	 */
	@Data
	@Schema(description = "作业条")
	public static class TaskBar extends WoTask {

		/** 工单号 */
		@Schema(description = "工单号")
		private String orderNo;

		/** 物料编码 */
		@Schema(description = "物料编码")
		private String materialCode;

		/** 物料名称 */
		@Schema(description = "物料名称")
		private String materialName;

		/** 工单优先级 */
		@Schema(description = "工单优先级")
		private Integer orderPriority;

		/** 工单计划完工日期（延期标记用） */
		@Schema(description = "工单计划完工日期")
		private java.time.LocalDate orderPlanEndDate;

	}

}
