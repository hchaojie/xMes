package com.pig4cloud.pig.mes.execution.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工单
 *
 * @author xmes
 */
@Data
@TableName("mes_wo_order")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单")
public class WoOrder extends Model<WoOrder> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工单号（空则按规则自动生成） */
	@Schema(description = "工单号（空则按规则自动生成）")
	private String orderNo;

	/** 订单类型：PRODUCTION-生产，REWORK-返工，OVERHEAD-间接费用 */
	@Schema(description = "订单类型：PRODUCTION-生产，REWORK-返工，OVERHEAD-间接费用")
	private String orderType;

	/** 来源：PLAN-工作计划生成，ERP-接口，MANUAL-手工 */
	@Schema(description = "来源：PLAN-工作计划生成，ERP-接口，MANUAL-手工")
	private String sourceType;

	/** 物料ID */
	@NotNull(message = "物料ID不能为空")
	@Schema(description = "物料ID")
	private Long materialId;

	/** 计划数量 */
	@NotNull(message = "计划数量不能为空")
	@Schema(description = "计划数量")
	private java.math.BigDecimal quantity;

	/** 单位 */
	@Schema(description = "单位")
	private String unit;

	/** 计划开始日期 */
	@Schema(description = "计划开始日期")
	private LocalDate planStartDate;

	/** 计划完工日期 */
	@Schema(description = "计划完工日期")
	private LocalDate planEndDate;

	/** 优先级（数值越大越优先） */
	@Schema(description = "优先级（数值越大越优先）")
	private Integer priority;

	/** 工单状态：CREATED-已创建，RELEASED-已下达，IN_PROGRESS-执行中，HOLD-挂起，COMPLETED-已完工，CLOSED-已关闭，CANCELLED-已取消 */
	@Schema(description = "工单状态：CREATED-已创建，RELEASED-已下达，IN_PROGRESS-执行中，HOLD-挂起，COMPLETED-已完工，CLOSED-已关闭，CANCELLED-已取消")
	private String orderStatus;

	/** 工艺快照来源：工作计划ID */
	@Schema(description = "工艺快照来源：工作计划ID")
	private Long routingId;

	/** 工艺快照来源：工作计划版本 */
	@Schema(description = "工艺快照来源：工作计划版本")
	private Integer routingVersion;

	/** ERP 单据号 */
	@Schema(description = "ERP 单据号")
	private String erpOrderNo;

	/** 良品数量 */
	@Schema(description = "良品数量")
	private java.math.BigDecimal qtyGood;

	/** 报废数量 */
	@Schema(description = "报废数量")
	private java.math.BigDecimal qtyScrap;

	/** 返工数量 */
	@Schema(description = "返工数量")
	private java.math.BigDecimal qtyRework;

	/** 挂起原因 */
	@Schema(description = "挂起原因")
	private String holdReason;

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
