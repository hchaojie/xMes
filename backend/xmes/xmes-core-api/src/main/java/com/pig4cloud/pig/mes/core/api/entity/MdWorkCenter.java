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
 * 工作中心
 *
 * @author xmes
 */
@Data
@TableName("mes_md_work_center")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作中心")
public class MdWorkCenter extends Model<MdWorkCenter> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工作中心编码 */
	@NotBlank(message = "工作中心编码不能为空")
	@Schema(description = "工作中心编码")
	private String centerCode;

	/** 工作中心名称 */
	@NotBlank(message = "工作中心名称不能为空")
	@Schema(description = "工作中心名称")
	private String centerName;

	/** 所属车间ID */
	@NotNull(message = "所属车间ID不能为空")
	@Schema(description = "所属车间ID")
	private Long areaId;

	/** 类型：LINE-产线，CELL-设备组，TEAM-班组，OUTSOURCE-外协 */
	@Schema(description = "类型：LINE-产线，CELL-设备组，TEAM-班组，OUTSOURCE-外协")
	private String centerType;

	/** 工厂日历ID（覆盖工厂默认） */
	@Schema(description = "工厂日历ID（覆盖工厂默认）")
	private Long calendarId;

	/** 并行机台数（产能模型） */
	@Schema(description = "并行机台数（产能模型）")
	private Integer parallelCount;


	/** 排序值 */
	@Schema(description = "排序值")
	private Integer sortOrder;

	/** 状态：0-启用，1-停用 */
	@Schema(description = "状态：0-启用，1-停用")
	private String status;

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
