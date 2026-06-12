package com.pig4cloud.pig.mes.quality.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验结果（按特性×样本）
 *
 * @author xmes
 */
@Data
@TableName("mes_qc_inspection_result")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验结果（按特性×样本）")
public class QcInspectionResult extends Model<QcInspectionResult> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 检验单ID */
	@NotNull(message = "检验单ID不能为空")
	@Schema(description = "检验单ID")
	private Long inspectionOrderId;

	/** 特性ID */
	@NotNull(message = "特性ID不能为空")
	@Schema(description = "特性ID")
	private Long characteristicId;

	/** 特性名称（快照） */
	@Schema(description = "特性名称（快照）")
	private String charName;

	/** 特性类型（快照） */
	@Schema(description = "特性类型（快照）")
	private String charType;

	/** 目标值（快照） */
	@Schema(description = "目标值（快照）")
	private java.math.BigDecimal targetValue;

	/** 上限（快照） */
	@Schema(description = "上限（快照）")
	private java.math.BigDecimal upperLimit;

	/** 下限（快照） */
	@Schema(description = "下限（快照）")
	private java.math.BigDecimal lowerLimit;

	/** 样本号 */
	@Schema(description = "样本号")
	private Integer sampleNo;

	/** 实测值（计量） */
	@Schema(description = "实测值（计量）")
	private java.math.BigDecimal measuredValue;

	/** 判定：OK/NG */
	@Schema(description = "判定：OK/NG")
	private String judge;

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
