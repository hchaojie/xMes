package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 维护工单（FR-RES-32：计划到期自动生成 / 故障报修；状态流 待接单→维修中→待验证→关闭）
 *
 * @author xmes
 */
@Data
@TableName("mes_mnt_order")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维护工单")
public class MntOrder extends Model<MntOrder> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 维护工单号 */
	@Schema(description = "维护工单号")
	private String orderNo;

	/** 类型：PM-保养，REPAIR-维修，SPOT-点检 */
	@Schema(description = "类型：PM-保养，REPAIR-维修，SPOT-点检")
	private String orderType;

	/** 来源维护计划ID（计划生成时） */
	@Schema(description = "来源维护计划ID")
	private Long planId;

	/** 来源维护工单ID（点检 NG 自动报修时） */
	@Schema(description = "来源维护工单ID（点检 NG 自动报修）")
	private Long sourceOrderId;

	/** 设备（工位）ID */
	@NotNull(message = "设备不能为空")
	@Schema(description = "设备（工位）ID")
	private Long workplaceId;

	/** 状态：PENDING-待接单，IN_PROGRESS-维修中，REVIEW-待验证，CLOSED-已关闭，CANCELLED-已取消 */
	@Schema(description = "状态：PENDING/IN_PROGRESS/REVIEW/CLOSED/CANCELLED")
	private String orderStatus;

	/** 要求完成日期 */
	@Schema(description = "要求完成日期")
	private LocalDate dueDate;

	/** 故障现象（报修描述） */
	@Schema(description = "故障现象（报修描述）")
	private String faultDesc;

	/** 故障原因 */
	@Schema(description = "故障原因")
	private String faultCause;

	/** 处理措施 */
	@Schema(description = "处理措施")
	private String actionTaken;

	/** 备件消耗（自由文本） */
	@Schema(description = "备件消耗")
	private String spareParts;

	/** 报修人 */
	@Schema(description = "报修人")
	private String reportBy;

	/** 报修/生成时间 */
	@Schema(description = "报修/生成时间")
	private LocalDateTime reportTime;

	/** 接单人（维修工） */
	@Schema(description = "接单人")
	private String acceptBy;

	/** 接单时间（维修开始，设备进入 MAINT） */
	@Schema(description = "接单时间")
	private LocalDateTime acceptTime;

	/** 维修完成时间（设备退出 MAINT） */
	@Schema(description = "维修完成时间")
	private LocalDateTime finishTime;

	/** 验证关闭人 */
	@Schema(description = "验证关闭人")
	private String closeBy;

	/** 关闭时间 */
	@Schema(description = "关闭时间")
	private LocalDateTime closeTime;

	/** 停机时长（分钟，接单→完成） */
	@Schema(description = "停机时长（分钟）")
	private Integer downtimeMinutes;

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
