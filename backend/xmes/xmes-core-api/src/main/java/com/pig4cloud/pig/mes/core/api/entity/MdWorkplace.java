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
 * 工位（机台）
 *
 * @author xmes
 */
@Data
@TableName("mes_md_workplace")
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工位（机台）")
public class MdWorkplace extends Model<MdWorkplace> {

	private static final long serialVersionUID = 1L;

	/** 主键ID */
	@TableId(value = "id", type = IdType.ASSIGN_ID)
	@Schema(description = "主键ID")
	private Long id;

	/** 工位编码 */
	@NotBlank(message = "工位编码不能为空")
	@Schema(description = "工位编码")
	private String workplaceCode;

	/** 工位名称 */
	@NotBlank(message = "工位名称不能为空")
	@Schema(description = "工位名称")
	private String workplaceName;

	/** 所属工作中心ID */
	@NotNull(message = "所属工作中心ID不能为空")
	@Schema(description = "所属工作中心ID")
	private Long workCenterId;

	/** 类型：MACHINE-机加，ASSEMBLY-装配，INSPECT-检验，REWORK-返修，VIRTUAL-外协虚拟 */
	@Schema(description = "类型：MACHINE-机加，ASSEMBLY-装配，INSPECT-检验，REWORK-返修，VIRTUAL-外协虚拟")
	private String workplaceType;

	/** 是否需要上岗登录：0-否，1-是 */
	@Schema(description = "是否需要上岗登录：0-否，1-是")
	private String loginRequired;

	/** 是否允许多人同时上岗：0-否，1-是 */
	@Schema(description = "是否允许多人同时上岗：0-否，1-是")
	private String allowMultiPerson;

	/** 是否允许并行作业：0-否，1-是 */
	@Schema(description = "是否允许并行作业：0-否，1-是")
	private String allowParallelTask;

	/** 绑定终端编码 */
	@Schema(description = "绑定终端编码")
	private String terminalCode;


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
