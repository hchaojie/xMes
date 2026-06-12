package com.pig4cloud.pig.mes.material.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 物料批次
 *
 * @author xmes
 */
@Data
@TableName("mes_mt_lot")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "物料批次")
public class MtLot extends Model<MtLot> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 批次号（空则按规则生成） */
	@Schema(description = "批次号（空则按规则生成）")
	private String lotNo;

	/** 物料ID */
	@NotNull(message = "物料ID不能为空")
	@Schema(description = "物料ID")
	private Long materialId;

	/** 物料编码（快照展示） */
	@Schema(description = "物料编码（快照展示）")
	private String materialCode;

	/** 物料名称（快照展示） */
	@Schema(description = "物料名称（快照展示）")
	private String materialName;

	/** 当前数量 */
	@NotNull(message = "当前数量不能为空")
	@Schema(description = "当前数量")
	private java.math.BigDecimal qty;

	/** 单位 */
	@Schema(description = "单位")
	private String unit;

	/** 状态：AVAILABLE-可用，HOLD-隔离，FROZEN-冻结，CONSUMED-耗尽 */
	@Schema(description = "状态：AVAILABLE-可用，HOLD-隔离，FROZEN-冻结，CONSUMED-耗尽")
	private String lotStatus;

	/** 来源：PURCHASE-采购收货，PRODUCTION-生产产出，SPLIT-拆分 */
	@Schema(description = "来源：PURCHASE-采购收货，PRODUCTION-生产产出，SPLIT-拆分")
	private String sourceType;

	/** 产出工单ID（生产产出时） */
	@Schema(description = "产出工单ID（生产产出时）")
	private Long woOrderId;

	/** 供应商批次号 */
	@Schema(description = "供应商批次号")
	private String supplierLotNo;

	/** 生产日期 */
	@Schema(description = "生产日期")
	private LocalDate prodDate;

	/** 失效日期 */
	@Schema(description = "失效日期")
	private LocalDate expireDate;

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
