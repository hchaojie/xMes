package com.pig4cloud.pig.mes.quality.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验计划
 *
 * @author xmes
 */
@Data
@TableName("mes_qc_plan")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验计划")
public class QcPlan extends Model<QcPlan> {

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

	/** 检验类型：FIRST_ARTICLE-首件，PROCESS-过程（质量门），FINAL-终检，IQC-来料，PATROL-巡检 */
	@Schema(description = "检验类型：FIRST_ARTICLE-首件，PROCESS-过程（质量门），FINAL-终检，IQC-来料，PATROL-巡检")
	private String planType;

	/** 适用物料ID（空=通用） */
	@Schema(description = "适用物料ID（空=通用）")
	private Long materialId;

	/** 适用工序号（空=不限） */
	@Schema(description = "适用工序号（空=不限）")
	private String operationNo;

	/** 抽样方式：FULL-全检，FIXED-固定数量 */
	@Schema(description = "抽样方式：FULL-全检，FIXED-固定数量")
	private String samplingType;

	/** 抽样数量（FIXED 时生效） */
	@Schema(description = "抽样数量（FIXED 时生效）")
	private java.math.BigDecimal sampleSize;

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
