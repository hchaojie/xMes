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
 * 物料清单（BOM）
 *
 * @author xmes
 */
@Data
@TableName("mes_md_bom")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料清单（BOM）")
public class MdBom extends Model<MdBom> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 父物料ID */
	@NotNull(message = "父物料ID不能为空")
	@Schema(description = "父物料ID")
	private Long materialId;

	/** 版本号 */
	@NotNull(message = "版本号不能为空")
	@Schema(description = "版本号")
	private Integer version;

	/** BOM状态：DRAFT-草稿，RELEASED-已发布，FROZEN-已冻结 */
	@Schema(description = "BOM状态：DRAFT-草稿，RELEASED-已发布，FROZEN-已冻结")
	private String bomStatus;

	/** 生效日期 */
	@Schema(description = "生效日期")
	private LocalDate effectiveFrom;

	/** 失效日期 */
	@Schema(description = "失效日期")
	private LocalDate effectiveTo;


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
