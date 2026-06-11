package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班段
 *
 * @author xmes
 */
@Data
@TableName("mes_md_shift_segment")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "班段")
public class MdShiftSegment extends Model<MdShiftSegment> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 班次模型ID */
	@NotNull(message = "班次模型ID不能为空")
	@Schema(description = "班次模型ID")
	private Long shiftModelId;

	/** 班段编码（如 D/N） */
	@NotBlank(message = "班段编码（如 D/N）不能为空")
	@Schema(description = "班段编码（如 D/N）")
	private String segmentCode;

	/** 班段名称（如 早班/夜班） */
	@NotBlank(message = "班段名称（如 早班/夜班）不能为空")
	@Schema(description = "班段名称（如 早班/夜班）")
	private String segmentName;

	/** 开始时间 */
	@NotNull(message = "开始时间不能为空")
	@Schema(description = "开始时间")
	private LocalTime startTime;

	/** 结束时间 */
	@NotNull(message = "结束时间不能为空")
	@Schema(description = "结束时间")
	private LocalTime endTime;

	/** 结束时间是否跨天：0-否，1-是 */
	@Schema(description = "结束时间是否跨天：0-否，1-是")
	private String crossDay;

	/** 休息时长（分钟） */
	@Schema(description = "休息时长（分钟）")
	private Integer breakMinutes;

	/** 排序值 */
	@Schema(description = "排序值")
	private Integer sortOrder;


	/** 创建人 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建人")
	private String createBy;

	/** 创建时间 */
	@TableField(fill = FieldFill.INSERT)
	@Schema(description = "创建时间")
	private LocalDateTime createTime;

	/** 修改人 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改人")
	private String updateBy;

	/** 修改时间 */
	@TableField(fill = FieldFill.UPDATE)
	@Schema(description = "修改时间")
	private LocalDateTime updateTime;

	/** 删除标志：0-未删除，1-已删除 */
	@TableLogic
	@Schema(description = "删除标志")
	private String delFlag;

	/** 租户ID */
	@Schema(description = "租户ID")
	private Long tenantId;
}
