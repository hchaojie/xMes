package com.pig4cloud.pig.mes.quality.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验特性
 *
 * @author xmes
 */
@Data
@TableName("mes_qc_characteristic")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验特性")
public class QcCharacteristic extends Model<QcCharacteristic> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 检验计划ID */
	@Schema(description = "检验计划ID")
	private Long planId;

	/** 特性号 */
	@NotBlank(message = "特性号不能为空")
	@Schema(description = "特性号")
	private String charNo;

	/** 特性名称 */
	@NotBlank(message = "特性名称不能为空")
	@Schema(description = "特性名称")
	private String charName;

	/** 特性类型：QUANT-计量，COUNT-计数，VISUAL-目视 */
	@Schema(description = "特性类型：QUANT-计量，COUNT-计数，VISUAL-目视")
	private String charType;

	/** 目标值（计量） */
	@Schema(description = "目标值（计量）")
	private java.math.BigDecimal targetValue;

	/** 上限（计量） */
	@Schema(description = "上限（计量）")
	private java.math.BigDecimal upperLimit;

	/** 下限（计量） */
	@Schema(description = "下限（计量）")
	private java.math.BigDecimal lowerLimit;

	/** 单位 */
	@Schema(description = "单位")
	private String unit;

	/** 是否关键特性（CTQ）：0-否，1-是 */
	@Schema(description = "是否关键特性（CTQ）：0-否，1-是")
	private String criticalFlag;

	/** 排序值 */
	@Schema(description = "排序值")
	private Integer sortOrder;

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
