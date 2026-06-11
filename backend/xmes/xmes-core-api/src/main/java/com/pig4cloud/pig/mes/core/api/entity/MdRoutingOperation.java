package com.pig4cloud.pig.mes.core.api.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 工序
 *
 * @author xmes
 */
@Data
@TableName("mes_md_routing_operation")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工序")
public class MdRoutingOperation extends Model<MdRoutingOperation> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工作计划ID */
	@NotNull(message = "工作计划ID不能为空")
	@Schema(description = "工作计划ID")
	private Long routingId;

	/** 工序号（如 0010） */
	@NotBlank(message = "工序号（如 0010）不能为空")
	@Schema(description = "工序号（如 0010）")
	private String operationNo;

	/** 工序名称 */
	@NotBlank(message = "工序名称不能为空")
	@Schema(description = "工序名称")
	private String operationName;

	/** 工序类型：PROCESS-加工，ASSEMBLY-装配，INSPECT-检验，OUTSOURCE-外协，REWORK-返修 */
	@Schema(description = "工序类型：PROCESS-加工，ASSEMBLY-装配，INSPECT-检验，OUTSOURCE-外协，REWORK-返修")
	private String operationType;

	/** 默认工作中心ID */
	@Schema(description = "默认工作中心ID")
	private Long workCenterId;

	/** 准备时间（分钟） */
	@Schema(description = "准备时间（分钟）")
	private java.math.BigDecimal setupTime;

	/** 单件时间（分钟） */
	@Schema(description = "单件时间（分钟）")
	private java.math.BigDecimal unitTime;

	/** 等待时间（分钟） */
	@Schema(description = "等待时间（分钟）")
	private java.math.BigDecimal waitTime;

	/** 报工方式：PIECE-按件，TIME-按时，MILESTONE-里程碑 */
	@Schema(description = "报工方式：PIECE-按件，TIME-按时，MILESTONE-里程碑")
	private String reportMode;

	/** 是否质量门：0-否，1-是 */
	@Schema(description = "是否质量门：0-否，1-是")
	private String qualityGate;

	/** 转移批量（0 表示整批转移） */
	@Schema(description = "转移批量（0 表示整批转移）")
	private java.math.BigDecimal transferQty;

	/** 排序值 */
	@Schema(description = "排序值")
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
