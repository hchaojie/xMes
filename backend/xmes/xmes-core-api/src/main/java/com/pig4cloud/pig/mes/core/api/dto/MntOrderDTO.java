package com.pig4cloud.pig.mes.core.api.dto;

import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import com.pig4cloud.pig.mes.core.api.entity.MntOrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 维护工单（含检查项结果）传输对象
 *
 * @author xmes
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "维护工单（含检查项结果）")
public class MntOrderDTO extends MntOrder {

	private static final long serialVersionUID = 1L;

	/** 检查项结果列表 */
	@Schema(description = "检查项结果列表")
	private List<MntOrderItem> items;

	/** 设备编码（展示用） */
	@Schema(description = "设备编码")
	private String workplaceCode;

	/** 设备名称（展示用） */
	@Schema(description = "设备名称")
	private String workplaceName;

}
