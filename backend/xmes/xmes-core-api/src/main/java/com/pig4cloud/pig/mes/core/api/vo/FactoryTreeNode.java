package com.pig4cloud.pig.mes.core.api.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 工厂结构树节点：工厂 → 车间 → 工作中心 → 工位
 *
 * @author xmes
 */
@Data
@Schema(description = "工厂结构树节点")
public class FactoryTreeNode {

	/** 节点ID */
	@Schema(description = "节点ID")
	private Long id;

	/** 父节点ID */
	@Schema(description = "父节点ID")
	private Long parentId;

	/** 编码 */
	@Schema(description = "编码")
	private String code;

	/** 名称 */
	@Schema(description = "名称")
	private String name;

	/** 节点类型：SITE/AREA/WORK_CENTER/WORKPLACE */
	@Schema(description = "节点类型：SITE/AREA/WORK_CENTER/WORKPLACE")
	private String nodeType;

	/** 状态：0-启用，1-停用 */
	@Schema(description = "状态")
	private String status;

	/** 子节点 */
	@Schema(description = "子节点")
	private List<FactoryTreeNode> children = new ArrayList<>();

}
