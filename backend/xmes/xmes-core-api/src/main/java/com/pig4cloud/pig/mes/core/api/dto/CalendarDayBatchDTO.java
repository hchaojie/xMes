package com.pig4cloud.pig.mes.core.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 工厂日历批量设置传输对象：按日期区间与星期过滤批量设置日类型/班次
 *
 * @author xmes
 */
@Data
@Schema(description = "工厂日历批量设置")
public class CalendarDayBatchDTO {

	/** 工厂日历ID */
	@NotNull(message = "工厂日历ID不能为空")
	@Schema(description = "工厂日历ID")
	private Long calendarId;

	/** 开始日期 */
	@NotNull(message = "开始日期不能为空")
	@Schema(description = "开始日期")
	private LocalDate startDate;

	/** 结束日期 */
	@NotNull(message = "结束日期不能为空")
	@Schema(description = "结束日期")
	private LocalDate endDate;

	/** 适用星期（1-7，对应周一至周日；为空表示全部） */
	@Schema(description = "适用星期（1-7，对应周一至周日；为空表示全部）")
	private List<Integer> weekdays;

	/** 日类型：WORK-工作日，REST-休息日，HOLIDAY-节假日 */
	@NotNull(message = "日类型不能为空")
	@Schema(description = "日类型：WORK-工作日，REST-休息日，HOLIDAY-节假日")
	private String dayType;

	/** 班次模型ID（工作日必填） */
	@Schema(description = "班次模型ID（工作日必填）")
	private Long shiftModelId;

}
