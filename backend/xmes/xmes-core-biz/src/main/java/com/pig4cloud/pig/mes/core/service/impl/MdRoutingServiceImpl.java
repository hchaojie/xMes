package com.pig4cloud.pig.mes.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.dto.RoutingDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.api.entity.MdRoutingOperation;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.mapper.MdRoutingMapper;
import com.pig4cloud.pig.mes.core.mapper.MdRoutingOperationMapper;
import com.pig4cloud.pig.mes.core.mapper.MdMaterialMapper;
import com.pig4cloud.pig.mes.core.service.MdRoutingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工作计划服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdRoutingServiceImpl extends ServiceImpl<MdRoutingMapper, MdRouting> implements MdRoutingService {

	private static final String DRAFT = "DRAFT";

	private static final String RELEASED = "RELEASED";

	private static final String FROZEN = "FROZEN";

	private final MdRoutingOperationMapper lineMapper;

	private final MdMaterialMapper materialMapper;

	@Override
	public Page<RoutingDTO> pageDetail(Page<MdRouting> page, Long materialId, String status) {
		Page<MdRouting> result = this.page(page, Wrappers.<MdRouting>lambdaQuery()
			.eq(materialId != null, MdRouting::getMaterialId, materialId)
			.eq(status != null && !status.isEmpty(), MdRouting::getRoutingStatus, status)
			.orderByDesc(MdRouting::getMaterialId, MdRouting::getVersion));

		List<Long> materialIds = result.getRecords()
			.stream()
			.map(MdRouting::getMaterialId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		Map<Long, MdMaterial> materialMap = materialIds.isEmpty() ? Map.of()
				: materialMapper.selectBatchIds(materialIds)
					.stream()
					.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));

		Page<RoutingDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
		dtoPage.setRecords(result.getRecords().stream().map(head -> {
			RoutingDTO dto = BeanUtil.copyProperties(head, RoutingDTO.class);
			MdMaterial material = materialMap.get(head.getMaterialId());
			if (material != null) {
				dto.setMaterialCode(material.getMaterialCode());
				dto.setMaterialName(material.getMaterialName());
			}
			return dto;
		}).collect(Collectors.toList()));
		return dtoPage;
	}

	@Override
	public RoutingDTO getDetail(Long id) {
		MdRouting head = this.getById(id);
		Assert.notNull(head, "工作计划不存在");
		RoutingDTO dto = BeanUtil.copyProperties(head, RoutingDTO.class);
		dto.setOperations(listLines(id));
		MdMaterial material = materialMapper.selectById(head.getMaterialId());
		if (material != null) {
			dto.setMaterialCode(material.getMaterialCode());
			dto.setMaterialName(material.getMaterialName());
		}
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDraft(RoutingDTO dto) {
		Assert.notNull(materialMapper.selectById(dto.getMaterialId()), "物料不存在");
		validateLines(dto.getOperations());
		if (dto.getVersion() == null) {
			dto.setVersion(nextVersion(dto.getMaterialId()));
		}
		dto.setRoutingStatus(DRAFT);
		this.save(dto);
		insertLines(dto.getId(), dto.getOperations());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateDraft(RoutingDTO dto) {
		MdRouting db = this.getById(dto.getId());
		Assert.notNull(db, "工作计划不存在");
		Assert.isTrue(DRAFT.equals(db.getRoutingStatus()), "仅草稿状态可修改");
		validateLines(dto.getOperations());
		dto.setRoutingStatus(DRAFT);
		this.updateById(dto);
		lineMapper.delete(Wrappers.<MdRoutingOperation>lambdaQuery().eq(MdRoutingOperation::getRoutingId, dto.getId()));
		insertLines(dto.getId(), dto.getOperations());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void release(Long id) {
		MdRouting head = this.getById(id);
		Assert.notNull(head, "工作计划不存在");
		Assert.isTrue(DRAFT.equals(head.getRoutingStatus()), "仅草稿状态可发布");
		validateLines(listLines(id));
		// 同物料其他已发布版本自动冻结（保证单一有效版本）
		this.update(Wrappers.<MdRouting>lambdaUpdate()
			.eq(MdRouting::getMaterialId, head.getMaterialId())
			.eq(MdRouting::getRoutingStatus, RELEASED)
			.set(MdRouting::getRoutingStatus, FROZEN));
		head.setRoutingStatus(RELEASED);
		this.updateById(head);
	}

	@Override
	public void freeze(Long id) {
		MdRouting head = this.getById(id);
		Assert.notNull(head, "工作计划不存在");
		Assert.isTrue(RELEASED.equals(head.getRoutingStatus()), "仅已发布状态可冻结");
		head.setRoutingStatus(FROZEN);
		this.updateById(head);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long copyVersion(Long id) {
		RoutingDTO source = getDetail(id);
		MdRouting copy = BeanUtil.copyProperties(source, MdRouting.class);
		copy.setId(null);
		copy.setVersion(nextVersion(source.getMaterialId()));
		copy.setRoutingStatus(DRAFT);
		copy.setCreateBy(null);
		copy.setCreateTime(null);
		copy.setUpdateBy(null);
		copy.setUpdateTime(null);
		this.save(copy);
		insertLines(copy.getId(), source.getOperations());
		return copy.getId();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeDrafts(List<Long> ids) {
		Long nonDraft = this.baseMapper.selectCount(Wrappers.<MdRouting>lambdaQuery()
			.in(MdRouting::getId, ids)
			.ne(MdRouting::getRoutingStatus, DRAFT));
		Assert.isTrue(nonDraft == 0, "仅草稿状态可删除，已发布/冻结版本请保留追溯");
		this.removeBatchByIds(ids);
		lineMapper.delete(Wrappers.<MdRoutingOperation>lambdaQuery().in(MdRoutingOperation::getRoutingId, ids));
	}

	private List<MdRoutingOperation> listLines(Long headId) {
		return lineMapper.selectList(
				Wrappers.<MdRoutingOperation>lambdaQuery().eq(MdRoutingOperation::getRoutingId, headId).orderByAsc(MdRoutingOperation::getSortOrder));
	}

	private Integer nextVersion(Long materialId) {
		return this.list(Wrappers.<MdRouting>lambdaQuery().eq(MdRouting::getMaterialId, materialId))
			.stream()
			.map(MdRouting::getVersion)
			.filter(Objects::nonNull)
			.max(Integer::compareTo)
			.orElse(0) + 1;
	}

	private void insertLines(Long headId, List<MdRoutingOperation> lines) {
		if (CollectionUtils.isEmpty(lines)) {
			return;
		}
		int sort = 0;
		for (MdRoutingOperation line : lines) {
			line.setId(null);
			line.setRoutingId(headId);
			line.setSortOrder(sort++);
			lineMapper.insert(line);
		}
	}

	private void validateLines(List<MdRoutingOperation> lines) {
		Assert.notEmpty(lines, "工序不能为空");
		for (MdRoutingOperation op : lines) {
			Assert.hasText(op.getOperationNo(), "工序号不能为空");
			Assert.hasText(op.getOperationName(), "工序名称不能为空");
		}
		long distinct = lines.stream().map(MdRoutingOperation::getOperationNo).distinct().count();
		Assert.isTrue(distinct == lines.size(), "工序号重复");
	}

}
