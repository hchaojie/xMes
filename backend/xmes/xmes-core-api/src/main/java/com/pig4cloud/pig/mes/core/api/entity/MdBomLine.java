package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * BOM 行
 *
 * @author xmes
 */
@Data
@TableName("mes_md_bom_line")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "BOM 行")
public class MdBomLine extends Model<MdBomLine> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** BOM头ID */
	@NotNull(message = "BOM头ID不能为空")
	@Schema(description = "BOM头ID")
	private Long bomId;

	/** 子物料ID */
	@NotNull(message = "子物料ID不能为空")
	@Schema(description = "子物料ID")
	private Long childMaterialId;

	/** 单位用量 */
	@NotNull(message = "单位用量不能为空")
	@Schema(description = "单位用量")
	private java.math.BigDecimal quantity;

	/** 损耗率（%） */
	@Schema(description = "损耗率（%）")
	private java.math.BigDecimal lossRate;

	/** 消耗工序号（空表示末道工序倒冲） */
	@Schema(description = "消耗工序号（空表示末道工序倒冲）")
	private String consumeOperationNo;

	/** 是否关键件（需扫码绑定）：0-否，1-是 */
	@Schema(description = "是否关键件（需扫码绑定）：0-否，1-是")
	private String keyFlag;

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
