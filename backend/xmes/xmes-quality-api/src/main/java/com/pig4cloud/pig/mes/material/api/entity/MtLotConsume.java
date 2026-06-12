package com.pig4cloud.pig.mes.material.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 上料消耗记录（不可变）
 *
 * @author xmes
 */
@Data
@TableName("mes_mt_lot_consume")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "上料消耗记录（不可变）")
public class MtLotConsume extends Model<MtLotConsume> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 作业ID */
	@NotNull(message = "作业ID不能为空")
	@Schema(description = "作业ID")
	private Long taskId;

	/** 工单ID */
	@Schema(description = "工单ID")
	private Long orderId;

	/** 消耗批次ID */
	@NotNull(message = "消耗批次ID不能为空")
	@Schema(description = "消耗批次ID")
	private Long lotId;

	/** 批次号（快照展示） */
	@Schema(description = "批次号（快照展示）")
	private String lotNo;

	/** 物料ID（快照） */
	@Schema(description = "物料ID（快照）")
	private Long materialId;

	/** 物料编码（快照展示） */
	@Schema(description = "物料编码（快照展示）")
	private String materialCode;

	/** 消耗数量 */
	@NotNull(message = "消耗数量不能为空")
	@Schema(description = "消耗数量")
	private java.math.BigDecimal qty;

	/** 上料人 */
	@Schema(description = "上料人")
	private String personName;

	/** 上料时间 */
	@Schema(description = "上料时间")
	private LocalDateTime feedTime;


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
