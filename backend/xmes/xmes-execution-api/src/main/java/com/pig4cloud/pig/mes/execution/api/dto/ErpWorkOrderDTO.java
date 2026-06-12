package com.pig4cloud.pig.mes.execution.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * ERP 工单接收报文（FR-INT-12 简化版：单头+引用物料编码，工序由 MES 工作计划展开）
 *
 * @author xmes
 */
@Data
@Schema(description = "ERP 工单接收报文")
public class ErpWorkOrderDTO {

	/** ERP 单据号（幂等键） */
	@NotBlank(message = "ERP 单据号不能为空")
	@Schema(description = "ERP 单据号（幂等键）")
	private String erpOrderNo;

	/** 物料编码 */
	@NotBlank(message = "物料编码不能为空")
	@Schema(description = "物料编码")
	private String materialCode;

	/** 计划数量 */
	@NotNull(message = "计划数量不能为空")
	@Schema(description = "计划数量")
	private BigDecimal quantity;

	/** 计划开始日期 */
	@Schema(description = "计划开始日期")
	private LocalDate planStartDate;

	/** 计划完工日期 */
	@Schema(description = "计划完工日期")
	private LocalDate planEndDate;

	/** 优先级 */
	@Schema(description = "优先级")
	private Integer priority;

	/** 备注 */
	@Schema(description = "备注")
	private String remark;

}
