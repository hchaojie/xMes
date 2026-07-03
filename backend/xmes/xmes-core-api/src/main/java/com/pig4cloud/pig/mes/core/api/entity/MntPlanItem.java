package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 维护计划标准作业项（检查表）
 *
 * @author xmes
 */
@Data
@TableName("mes_mnt_plan_item")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维护计划标准作业项")
public class MntPlanItem extends Model<MntPlanItem> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 维护计划ID */
	@Schema(description = "维护计划ID")
	private Long planId;

	/** 项目号 */
	@NotBlank(message = "项目号不能为空")
	@Schema(description = "项目号")
	private String itemNo;

	/** 项目名称 */
	@NotBlank(message = "项目名称不能为空")
	@Schema(description = "项目名称")
	private String itemName;

	/** 标准/要求 */
	@Schema(description = "标准/要求")
	private String standardDesc;

	/** 方法/工具 */
	@Schema(description = "方法/工具")
	private String methodDesc;

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
