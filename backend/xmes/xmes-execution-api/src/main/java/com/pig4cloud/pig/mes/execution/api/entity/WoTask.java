package com.pig4cloud.pig.mes.execution.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 作业（工单×工序的执行实例，含工序快照）
 *
 * @author xmes
 */
@Data
@TableName("mes_wo_task")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "作业（工单×工序的执行实例，含工序快照）")
public class WoTask extends Model<WoTask> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工单ID */
	@NotNull(message = "工单ID不能为空")
	@Schema(description = "工单ID")
	private Long orderId;

	/** 工序号（快照） */
	@Schema(description = "工序号（快照）")
	private String operationNo;

	/** 工序名称（快照） */
	@Schema(description = "工序名称（快照）")
	private String operationName;

	/** 工序类型（快照） */
	@Schema(description = "工序类型（快照）")
	private String operationType;

	/** 作业状态：PENDING-待排，SCHEDULED-已排程，DISPATCHED-已派工，RUNNING-进行中，PAUSED-暂停，COMPLETED-完工 */
	@Schema(description = "作业状态：PENDING-待排，SCHEDULED-已排程，DISPATCHED-已派工，RUNNING-进行中，PAUSED-暂停，COMPLETED-完工")
	private String taskStatus;

	/** 工作中心ID（快照，可排程调整） */
	@Schema(description = "工作中心ID（快照，可排程调整）")
	private Long workCenterId;

	/** 排程工位ID */
	@Schema(description = "排程工位ID")
	private Long workplaceId;

	/** 排程开始时间 */
	@Schema(description = "排程开始时间")
	private LocalDateTime planStart;

	/** 排程结束时间 */
	@Schema(description = "排程结束时间")
	private LocalDateTime planEnd;

	/** 应做数量 */
	@Schema(description = "应做数量")
	private java.math.BigDecimal qtyTarget;

	/** 良品数量 */
	@Schema(description = "良品数量")
	private java.math.BigDecimal qtyGood;

	/** 报废数量 */
	@Schema(description = "报废数量")
	private java.math.BigDecimal qtyScrap;

	/** 返工数量 */
	@Schema(description = "返工数量")
	private java.math.BigDecimal qtyRework;

	/** 准备时间（分钟，快照） */
	@Schema(description = "准备时间（分钟，快照）")
	private java.math.BigDecimal setupTime;

	/** 单件时间（分钟，快照） */
	@Schema(description = "单件时间（分钟，快照）")
	private java.math.BigDecimal unitTime;

	/** 等待时间（分钟，快照） */
	@Schema(description = "等待时间（分钟，快照）")
	private java.math.BigDecimal waitTime;

	/** 报工方式（快照） */
	@Schema(description = "报工方式（快照）")
	private String reportMode;

	/** 是否质量门（快照）：0-否，1-是 */
	@Schema(description = "是否质量门（快照）：0-否，1-是")
	private String qualityGate;

	/** 转移批量（快照） */
	@Schema(description = "转移批量（快照）")
	private java.math.BigDecimal transferQty;

	/** 拆分序号（同工序多作业并行时 >0） */
	@Schema(description = "拆分序号（同工序多作业并行时 >0）")
	private Integer splitSeq;

	/** 排序值（工序顺序） */
	@Schema(description = "排序值（工序顺序）")
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
