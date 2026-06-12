package com.pig4cloud.pig.mes.quality.api.dto;

import com.pig4cloud.pig.mes.quality.api.entity.QcCharacteristic;
import com.pig4cloud.pig.mes.quality.api.entity.QcPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 检验计划（含特性）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验计划（含特性）")
public class QcPlanDTO extends QcPlan {

	private static final long serialVersionUID = 1L;

	/** 特性列表 */
	@Valid
	@Schema(description = "特性列表")
	private List<QcCharacteristic> characteristics;

	/** 适用物料编码（展示用） */
	@Schema(description = "适用物料编码")
	private String materialCode;

}
