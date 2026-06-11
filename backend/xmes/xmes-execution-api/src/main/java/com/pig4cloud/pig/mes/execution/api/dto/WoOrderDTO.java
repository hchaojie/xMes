package com.pig4cloud.pig.mes.execution.api.dto;

import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 工单（含作业与物料展示信息）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工单（含作业）")
public class WoOrderDTO extends WoOrder {

	private static final long serialVersionUID = 1L;

	/** 作业列表 */
	@Schema(description = "作业列表")
	private List<WoTask> tasks;

	/** 物料编码（展示用） */
	@Schema(description = "物料编码")
	private String materialCode;

	/** 物料名称（展示用） */
	@Schema(description = "物料名称")
	private String materialName;

}
