package com.pig4cloud.pig.mes.material.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 序列件（一物一码）
 *
 * @author xmes
 */
@Data
@TableName("mes_mt_serial")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "序列件（一物一码）")
public class MtSerialUnit extends Model<MtSerialUnit> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 序列号 */
	@Schema(description = "序列号")
	private String serialNo;

	/** 物料ID */
	@Schema(description = "物料ID")
	private Long materialId;

	/** 物料编码（快照展示） */
	@Schema(description = "物料编码（快照展示）")
	private String materialCode;

	/** 所属批次ID（可空） */
	@Schema(description = "所属批次ID（可空）")
	private Long lotId;

	/** 产出工单ID */
	@Schema(description = "产出工单ID")
	private Long woOrderId;

	/** 状态：OK-合格，SCRAP-报废，HOLD-隔离 */
	@Schema(description = "状态：OK-合格，SCRAP-报废，HOLD-隔离")
	private String serialStatus;

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
