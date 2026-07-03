package com.pig4cloud.pig.mes.core.api.dto;

import com.pig4cloud.pig.mes.core.api.entity.MntPlan;
import com.pig4cloud.pig.mes.core.api.entity.MntPlanItem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 维护计划（含标准作业项）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维护计划（含标准作业项）")
public class MntPlanDTO extends MntPlan {

	private static final long serialVersionUID = 1L;

	/** 标准作业项列表 */
	@Valid
	@Schema(description = "标准作业项列表")
	private List<MntPlanItem> items;

	/** 设备编码（展示用） */
	@Schema(description = "设备编码")
	private String workplaceCode;

	/** 设备名称（展示用） */
	@Schema(description = "设备名称")
	private String workplaceName;

}
