package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.mes.core.api.vo.FactoryTreeNode;
import com.pig4cloud.pig.mes.core.mapper.MdAreaMapper;
import com.pig4cloud.pig.mes.core.mapper.MdSiteMapper;
import com.pig4cloud.pig.mes.core.mapper.MdWorkCenterMapper;
import com.pig4cloud.pig.mes.core.mapper.MdWorkplaceMapper;
import com.pig4cloud.pig.mes.core.service.FactoryTreeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工厂结构树服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class FactoryTreeServiceImpl implements FactoryTreeService {

	private final MdSiteMapper siteMapper;

	private final MdAreaMapper areaMapper;

	private final MdWorkCenterMapper workCenterMapper;

	private final MdWorkplaceMapper workplaceMapper;

	@Override
	public List<FactoryTreeNode> buildTree() {
		List<FactoryTreeNode> sites = siteMapper.selectList(Wrappers.emptyWrapper())
			.stream()
			.map(s -> node(s.getId(), null, s.getSiteCode(), s.getSiteName(), "SITE", s.getStatus(), s.getSortOrder()))
			.collect(Collectors.toList());

		Map<Long, FactoryTreeNode> areaNodes = areaMapper.selectList(Wrappers.emptyWrapper())
			.stream()
			.map(a -> node(a.getId(), a.getSiteId(), a.getAreaCode(), a.getAreaName(), "AREA", a.getStatus(),
					a.getSortOrder()))
			.collect(Collectors.toMap(FactoryTreeNode::getId, Function.identity()));

		Map<Long, FactoryTreeNode> centerNodes = workCenterMapper.selectList(Wrappers.emptyWrapper())
			.stream()
			.map(c -> node(c.getId(), c.getAreaId(), c.getCenterCode(), c.getCenterName(), "WORK_CENTER",
					c.getStatus(), c.getSortOrder()))
			.collect(Collectors.toMap(FactoryTreeNode::getId, Function.identity()));

		workplaceMapper.selectList(Wrappers.emptyWrapper())
			.stream()
			.map(w -> node(w.getId(), w.getWorkCenterId(), w.getWorkplaceCode(), w.getWorkplaceName(), "WORKPLACE",
					w.getStatus(), w.getSortOrder()))
			.forEach(n -> mount(centerNodes.get(n.getParentId()), n));

		centerNodes.values().forEach(n -> mount(areaNodes.get(n.getParentId()), n));

		Map<Long, FactoryTreeNode> siteNodes = sites.stream()
			.collect(Collectors.toMap(FactoryTreeNode::getId, Function.identity()));
		areaNodes.values().forEach(n -> mount(siteNodes.get(n.getParentId()), n));
		return sites;
	}

	private void mount(FactoryTreeNode parent, FactoryTreeNode child) {
		if (parent != null) {
			parent.getChildren().add(child);
			parent.getChildren().sort(Comparator.comparing(FactoryTreeNode::getCode));
		}
	}

	private FactoryTreeNode node(Long id, Long parentId, String code, String name, String type, String status,
			Integer sortOrder) {
		FactoryTreeNode n = new FactoryTreeNode();
		n.setId(id);
		n.setParentId(parentId);
		n.setCode(code);
		n.setName(name);
		n.setNodeType(type);
		n.setStatus(status);
		return n;
	}

}
