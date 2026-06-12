package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工厂日历日明细
 *
 * @author xmes
 */
@Data
@TableName("mes_md_calendar_day")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工厂日历日明细")
public class MdCalendarDay extends Model<MdCalendarDay> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工厂日历ID */
	@NotNull(message = "工厂日历ID不能为空")
	@Schema(description = "工厂日历ID")
	private Long calendarId;

	/** 日期 */
	@NotNull(message = "日期不能为空")
	@Schema(description = "日期")
	private LocalDate calDate;

	/** 日类型：WORK-工作日，REST-休息日，HOLIDAY-节假日 */
	@Schema(description = "日类型：WORK-工作日，REST-休息日，HOLIDAY-节假日")
	private String dayType;

	/** 班次模型ID（工作日必填） */
	@Schema(description = "班次模型ID（工作日必填）")
	private Long shiftModelId;


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
