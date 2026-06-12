package com.pig4cloud.pig.mes.quality.api.dto;

import com.pig4cloud.pig.mes.quality.api.entity.QcCharacteristic;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 检验单详情（特性 + 已录结果）
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "检验单详情")
public class QcInspectionDetailDTO extends QcInspectionOrder {

	private static final long serialVersionUID = 1L;

	/** 计划特性列表 */
	@Schema(description = "计划特性列表")
	private List<QcCharacteristic> characteristics;

	/** 已录入结果 */
	@Schema(description = "已录入结果")
	private List<QcInspectionResult> results;

}
