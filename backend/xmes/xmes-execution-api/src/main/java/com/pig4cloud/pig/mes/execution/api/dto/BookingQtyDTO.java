package com.pig4cloud.pig.mes.execution.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 报数/完工传输对象
 *
 * @author xmes
 */
@Data
@Schema(description = "报数/完工参数")
public class BookingQtyDTO {

	/** 作业ID */
	@NotNull(message = "作业ID不能为空")
	@Schema(description = "作业ID")
	private Long taskId;

	/** 良品数量 */
	@Schema(description = "良品数量")
	private BigDecimal qtyGood;

	/** 报废数量 */
	@Schema(description = "报废数量")
	private BigDecimal qtyScrap;

	/** 返工数量 */
	@Schema(description = "返工数量")
	private BigDecimal qtyRework;

	/** 报废原因码（报废数量>0 时必填） */
	@Schema(description = "报废原因码")
	private String reasonCode;

	/** 工位ID */
	@Schema(description = "工位ID")
	private Long workplaceId;

	/** 备注 */
	@Schema(description = "备注")
	private String remark;

	/** 事件来源：TERMINAL-工位终端，PDA，MANUAL-管理端（默认） */
	@Schema(description = "事件来源：TERMINAL/PDA/MANUAL")
	private String source;

}
