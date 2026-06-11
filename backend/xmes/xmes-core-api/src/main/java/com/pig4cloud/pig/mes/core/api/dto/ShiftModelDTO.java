package com.pig4cloud.pig.mes.core.api.dto;

import com.pig4cloud.pig.mes.core.api.entity.MdShiftModel;
import com.pig4cloud.pig.mes.core.api.entity.MdShiftSegment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 班次模型（含班段）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "班次模型（含班段）")
public class ShiftModelDTO extends MdShiftModel {

	private static final long serialVersionUID = 1L;

	/** 班段列表 */
	@Valid
	@Schema(description = "班段列表")
	private List<MdShiftSegment> segments;

}
