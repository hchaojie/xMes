package com.pig4cloud.pig.mes.execution.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 排程分派传输对象
 *
 * @author xmes
 */
@Data
@Schema(description = "排程分派参数")
public class ScheduleAssignDTO {

	/** 作业ID */
	@NotNull(message = "作业ID不能为空")
	@Schema(description = "作业ID")
	private Long taskId;

	/** 目标工位ID */
	@NotNull(message = "工位ID不能为空")
	@Schema(description = "目标工位ID")
	private Long workplaceId;

	/** 排程开始时间（结束时间由服务端按 准备时间+数量×单件时间 计算） */
	@NotNull(message = "排程开始时间不能为空")
	@Schema(description = "排程开始时间")
	private LocalDateTime planStart;

}
