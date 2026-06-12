package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工厂日历
 *
 * @author xmes
 */
@Data
@TableName("mes_md_calendar")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工厂日历")
public class MdCalendar extends Model<MdCalendar> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 日历编码 */
	@NotBlank(message = "日历编码不能为空")
	@Schema(description = "日历编码")
	private String calendarCode;

	/** 日历名称 */
	@NotBlank(message = "日历名称不能为空")
	@Schema(description = "日历名称")
	private String calendarName;

	/** 所属工厂ID */
	@Schema(description = "所属工厂ID")
	private Long siteId;


	/** 排序值 */
	@Schema(description = "排序值")
	private Integer sortOrder;

	/** 状态：0-启用，1-停用 */
	@Schema(description = "状态：0-启用，1-停用")
	private String status;

	/** 备注 */
	@Schema(description = "备注")
	private String remark;

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
