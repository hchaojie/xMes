package com.pig4cloud.pig.mes.execution.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报工事件（不可变：只插入不更新，修正以负向冲销事件实现）
 *
 * @author xmes
 */
@Data
@TableName("mes_wo_booking")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "报工事件")
public class WoBooking extends Model<WoBooking> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 作业ID */
	@Schema(description = "作业ID")
	private Long taskId;

	/** 工单ID（冗余，便于按单查询） */
	@Schema(description = "工单ID")
	private Long orderId;

	/** 事件类型：START-开工，PAUSE-暂停，RESUME-恢复，QTY-报数，FINISH-完工，REVERSE-冲销 */
	@Schema(description = "事件类型：START/PAUSE/RESUME/QTY/FINISH/REVERSE")
	private String bookingType;

	/** 良品数量（冲销事件为负数） */
	@Schema(description = "良品数量")
	private BigDecimal qtyGood;

	/** 报废数量（冲销事件为负数） */
	@Schema(description = "报废数量")
	private BigDecimal qtyScrap;

	/** 返工数量（冲销事件为负数） */
	@Schema(description = "返工数量")
	private BigDecimal qtyRework;

	/** 报废/暂停原因码 */
	@Schema(description = "报废/暂停原因码")
	private String reasonCode;

	/** 工位ID */
	@Schema(description = "工位ID")
	private Long workplaceId;

	/** 报工人（pig 用户名） */
	@Schema(description = "报工人")
	private String personName;

	/** 发生时间 */
	@Schema(description = "发生时间")
	private LocalDateTime bookingTime;

	/** 来源：TERMINAL-工位终端，PDA，MANUAL-管理端，AUTO-自动 */
	@Schema(description = "来源：TERMINAL/PDA/MANUAL/AUTO")
	private String source;

	/** 冲销指向的原事件ID */
	@Schema(description = "冲销指向的原事件ID")
	private Long reverseOfId;

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

	/** 删除标志：0-未删除，1-已删除 */
	@TableLogic
	@Schema(description = "删除标志")
	private String delFlag;

	/** 租户ID */
	@Schema(description = "租户ID")
	private Long tenantId;

}
