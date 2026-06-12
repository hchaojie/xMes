package com.pig4cloud.pig.mes.core.api.dto;

import com.pig4cloud.pig.mes.core.api.entity.MdBom;
import com.pig4cloud.pig.mes.core.api.entity.MdBomLine;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * BOM（含行）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "BOM（含行）")
public class BomDTO extends MdBom {

	private static final long serialVersionUID = 1L;

	/** BOM 行列表 */
	@Valid
	@Schema(description = "BOM 行列表")
	private List<MdBomLine> lines;

	/** 父物料编码（展示用） */
	@Schema(description = "父物料编码")
	private String materialCode;

	/** 父物料名称（展示用） */
	@Schema(description = "父物料名称")
	private String materialName;

}
