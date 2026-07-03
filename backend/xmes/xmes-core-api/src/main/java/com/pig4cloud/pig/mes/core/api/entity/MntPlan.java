package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维护计划（FR-RES-31：按周期定义保养/点检计划与标准作业项）
 *
 * @author xmes
 */
@Data
@TableName("mes_mnt_plan")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维护计划")
public class MntPlan extends Model<MntPlan> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 计划编码 */
	@NotBlank(message = "计划编码不能为空")
	@Schema(description = "计划编码")
	private String planCode;

	/** 计划名称 */
	@NotBlank(message = "计划名称不能为空")
	@Schema(description = "计划名称")
	private String planName;

	/** 类型：PM-保养计划，SPOT-点检计划 */
	@Schema(description = "类型：PM-保养计划，SPOT-点检计划")
	private String planType;

	/** 设备（工位）ID */
	@NotNull(message = "设备不能为空")
	@Schema(description = "设备（工位）ID")
	private Long workplaceId;

	/** 周期类型：DAY-日历天，RUNTIME-运行时长(小时)，OUTPUT-产量计数 */
	@Schema(description = "周期类型：DAY-日历天，RUNTIME-运行时长(小时)，OUTPUT-产量计数")
	private String cycleType;

	/** 周期值（天/小时/件） */
	@NotNull(message = "周期值不能为空")
	@Schema(description = "周期值（天/小时/件）")
	private Integer cycleValue;

	/** 下次到期日 */
	@Schema(description = "下次到期日")
	private LocalDate nextDueDate;

	/** 最近生成工单日期 */
	@Schema(description = "最近生成工单日期")
	private LocalDate lastOrderDate;

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
