package com.pig4cloud.pig.mes.quality.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 检验单
 *
 * @author xmes
 */
@Data
@TableName("mes_qc_inspection_order")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验单")
public class QcInspectionOrder extends Model<QcInspectionOrder> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 检验单号 */
	@Schema(description = "检验单号")
	private String inspectionNo;

	/** 检验计划ID（快照引用） */
	@Schema(description = "检验计划ID（快照引用）")
	private Long planId;

	/** 检验类型（快照） */
	@Schema(description = "检验类型（快照）")
	private String planType;

	/** 工单ID */
	@Schema(description = "工单ID")
	private Long woOrderId;

	/** 作业ID（质量门触发时） */
	@Schema(description = "作业ID（质量门触发时）")
	private Long taskId;

	/** 工单号（快照展示） */
	@Schema(description = "工单号（快照展示）")
	private String orderNo;

	/** 物料编码（快照展示） */
	@Schema(description = "物料编码（快照展示）")
	private String materialCode;

	/** 物料名称（快照展示） */
	@Schema(description = "物料名称（快照展示）")
	private String materialName;

	/** 工序号（快照展示） */
	@Schema(description = "工序号（快照展示）")
	private String operationNo;

	/** 状态：PENDING-待检，INSPECTING-检验中，PASSED-合格，CONCESSION-让步接收，REJECTED-不合格 */
	@Schema(description = "状态：PENDING-待检，INSPECTING-检验中，PASSED-合格，CONCESSION-让步接收，REJECTED-不合格")
	private String inspectStatus;

	/** 抽样数量 */
	@Schema(description = "抽样数量")
	private java.math.BigDecimal sampleQty;

	/** 检验员 */
	@Schema(description = "检验员")
	private String inspector;

	/** 判定时间 */
	@Schema(description = "判定时间")
	private LocalDateTime judgeTime;

	/** 判定说明 */
	@Schema(description = "判定说明")
	private String judgeRemark;

	/** 来源：AUTO_GATE-质量门自动，MANUAL-手工 */
	@Schema(description = "来源：AUTO_GATE-质量门自动，MANUAL-手工")
	private String sourceType;

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
