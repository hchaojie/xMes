package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料
 *
 * @author xmes
 */
@Data
@TableName("mes_md_material")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料")
public class MdMaterial extends Model<MdMaterial> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 物料编码 */
	@NotBlank(message = "物料编码不能为空")
	@Schema(description = "物料编码")
	private String materialCode;

	/** 物料名称 */
	@NotBlank(message = "物料名称不能为空")
	@Schema(description = "物料名称")
	private String materialName;

	/** 物料类型：RAW-原材料，SEMI-半成品，FINISHED-成品，AUX-辅料 */
	@Schema(description = "物料类型：RAW-原材料，SEMI-半成品，FINISHED-成品，AUX-辅料")
	private String materialType;

	/** 规格型号 */
	@Schema(description = "规格型号")
	private String spec;

	/** 计量单位 */
	@NotBlank(message = "计量单位不能为空")
	@Schema(description = "计量单位")
	private String unit;

	/** 批次管理策略：NONE-不管批，LOT-批次，SERIAL-序列号（一物一码） */
	@Schema(description = "批次管理策略：NONE-不管批，LOT-批次，SERIAL-序列号（一物一码）")
	private String lotStrategy;

	/** 保质期（天） */
	@Schema(description = "保质期（天）")
	private Integer shelfLifeDays;


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
