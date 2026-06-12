package com.pig4cloud.pig.mes.core.service;

import com.pig4cloud.pig.mes.core.api.vo.FactoryTreeNode;

import java.util.List;

/**
 * 工厂结构树服务
 *
 * @author xmes
 */
public interface FactoryTreeService {

	/**
	 * 构建 工厂→车间→工作中心→工位 四级结构树
	 * @return 树形结构
	 */
	List<FactoryTreeNode> buildTree();

}
