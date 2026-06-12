package com.pig4cloud.pig.mes.material.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 批次谱系（消耗批次 → 产出批次/序列件，只增不改）
 *
 * @author xmes
 */
@Data
@TableName("mes_mt_genealogy")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "批次谱系（消耗批次 → 产出批次/序列件，只增不改）")
public class MtGenealogy extends Model<MtGenealogy> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 消耗（投入）批次ID */
	@NotNull(message = "消耗（投入）批次ID不能为空")
	@Schema(description = "消耗（投入）批次ID")
	private Long parentLotId;

	/** 产出批次ID（批次型产出） */
	@Schema(description = "产出批次ID（批次型产出）")
	private Long childLotId;

	/** 产出序列件ID（一物一码产出） */
	@Schema(description = "产出序列件ID（一物一码产出）")
	private Long childSerialId;

	/** 工单ID */
	@NotNull(message = "工单ID不能为空")
	@Schema(description = "工单ID")
	private Long orderId;

	/** 消耗数量 */
	@Schema(description = "消耗数量")
	private java.math.BigDecimal qty;


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
