package com.pig4cloud.pig.mes.core.api.dto;

import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.api.entity.MdRoutingOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 工作计划（含工序）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "工作计划（含工序）")
public class RoutingDTO extends MdRouting {

	private static final long serialVersionUID = 1L;

	/** 工序列表 */
	@Valid
	@Schema(description = "工序列表")
	private List<MdRoutingOperation> operations;

	/** 物料编码（展示用） */
	@Schema(description = "物料编码")
	private String materialCode;

	/** 物料名称（展示用） */
	@Schema(description = "物料名称")
	private String materialName;

}
