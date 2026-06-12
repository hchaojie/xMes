package com.pig4cloud.pig.mes.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.dto.BomDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdBom;
import com.pig4cloud.pig.mes.core.api.entity.MdBomLine;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.mapper.MdBomMapper;
import com.pig4cloud.pig.mes.core.mapper.MdBomLineMapper;
import com.pig4cloud.pig.mes.core.mapper.MdMaterialMapper;
import com.pig4cloud.pig.mes.core.service.MdBomService;
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
 * BOM服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdBomServiceImpl extends ServiceImpl<MdBomMapper, MdBom> implements MdBomService {

	private static final String DRAFT = "DRAFT";

	private static final String RELEASED = "RELEASED";

	private static final String FROZEN = "FROZEN";

	private final MdBomLineMapper lineMapper;

	private final MdMaterialMapper materialMapper;

	@Override
	public Page<BomDTO> pageDetail(Page<MdBom> page, Long materialId, String status) {
		Page<MdBom> result = this.page(page, Wrappers.<MdBom>lambdaQuery()
			.eq(materialId != null, MdBom::getMaterialId, materialId)
			.eq(status != null && !status.isEmpty(), MdBom::getBomStatus, status)
			.orderByDesc(MdBom::getMaterialId, MdBom::getVersion));

		List<Long> materialIds = result.getRecords()
			.stream()
			.map(MdBom::getMaterialId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		Map<Long, MdMaterial> materialMap = materialIds.isEmpty() ? Map.of()
				: materialMapper.selectBatchIds(materialIds)
					.stream()
					.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));

		Page<BomDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
		dtoPage.setRecords(result.getRecords().stream().map(head -> {
			BomDTO dto = BeanUtil.copyProperties(head, BomDTO.class);
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
	public BomDTO getDetail(Long id) {
		MdBom head = this.getById(id);
		Assert.notNull(head, "BOM不存在");
		BomDTO dto = BeanUtil.copyProperties(head, BomDTO.class);
		dto.setLines(listLines(id));
		MdMaterial material = materialMapper.selectById(head.getMaterialId());
		if (material != null) {
			dto.setMaterialCode(material.getMaterialCode());
			dto.setMaterialName(material.getMaterialName());
		}
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveDraft(BomDTO dto) {
		Assert.notNull(materialMapper.selectById(dto.getMaterialId()), "物料不存在");
		validateLines(dto.getLines());
		if (dto.getVersion() == null) {
			dto.setVersion(nextVersion(dto.getMaterialId()));
		}
		dto.setBomStatus(DRAFT);
		this.save(dto);
		insertLines(dto.getId(), dto.getLines());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateDraft(BomDTO dto) {
		MdBom db = this.getById(dto.getId());
		Assert.notNull(db, "BOM不存在");
		Assert.isTrue(DRAFT.equals(db.getBomStatus()), "仅草稿状态可修改");
		validateLines(dto.getLines());
		dto.setBomStatus(DRAFT);
		this.updateById(dto);
		lineMapper.delete(Wrappers.<MdBomLine>lambdaQuery().eq(MdBomLine::getBomId, dto.getId()));
		insertLines(dto.getId(), dto.getLines());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void release(Long id) {
		MdBom head = this.getById(id);
		Assert.notNull(head, "BOM不存在");
		Assert.isTrue(DRAFT.equals(head.getBomStatus()), "仅草稿状态可发布");
		validateLines(listLines(id));
		// 同物料其他已发布版本自动冻结（保证单一有效版本）
		this.update(Wrappers.<MdBom>lambdaUpdate()
			.eq(MdBom::getMaterialId, head.getMaterialId())
			.eq(MdBom::getBomStatus, RELEASED)
			.set(MdBom::getBomStatus, FROZEN));
		head.setBomStatus(RELEASED);
		this.updateById(head);
	}

	@Override
	public void freeze(Long id) {
		MdBom head = this.getById(id);
		Assert.notNull(head, "BOM不存在");
		Assert.isTrue(RELEASED.equals(head.getBomStatus()), "仅已发布状态可冻结");
		head.setBomStatus(FROZEN);
		this.updateById(head);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long copyVersion(Long id) {
		BomDTO source = getDetail(id);
		MdBom copy = BeanUtil.copyProperties(source, MdBom.class);
		copy.setId(null);
		copy.setVersion(nextVersion(source.getMaterialId()));
		copy.setBomStatus(DRAFT);
		copy.setCreateBy(null);
		copy.setCreateTime(null);
		copy.setUpdateBy(null);
		copy.setUpdateTime(null);
		this.save(copy);
		insertLines(copy.getId(), source.getLines());
		return copy.getId();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeDrafts(List<Long> ids) {
		Long nonDraft = this.baseMapper.selectCount(Wrappers.<MdBom>lambdaQuery()
			.in(MdBom::getId, ids)
			.ne(MdBom::getBomStatus, DRAFT));
		Assert.isTrue(nonDraft == 0, "仅草稿状态可删除，已发布/冻结版本请保留追溯");
		this.removeBatchByIds(ids);
		lineMapper.delete(Wrappers.<MdBomLine>lambdaQuery().in(MdBomLine::getBomId, ids));
	}

	private List<MdBomLine> listLines(Long headId) {
		return lineMapper.selectList(
				Wrappers.<MdBomLine>lambdaQuery().eq(MdBomLine::getBomId, headId).orderByAsc(MdBomLine::getSortOrder));
	}

	private Integer nextVersion(Long materialId) {
		return this.list(Wrappers.<MdBom>lambdaQuery().eq(MdBom::getMaterialId, materialId))
			.stream()
			.map(MdBom::getVersion)
			.filter(Objects::nonNull)
			.max(Integer::compareTo)
			.orElse(0) + 1;
	}

	private void insertLines(Long headId, List<MdBomLine> lines) {
		if (CollectionUtils.isEmpty(lines)) {
			return;
		}
		int sort = 0;
		for (MdBomLine line : lines) {
			line.setId(null);
			line.setBomId(headId);
			line.setSortOrder(sort++);
			lineMapper.insert(line);
		}
	}

	private void validateLines(List<MdBomLine> lines) {
		Assert.notEmpty(lines, "BOM 行不能为空");
		for (MdBomLine line : lines) {
			Assert.notNull(line.getChildMaterialId(), "BOM 行必须指定子物料");
			Assert.notNull(line.getQuantity(), "BOM 行必须填写用量");
		}
		long distinct = lines.stream().map(MdBomLine::getChildMaterialId).distinct().count();
		Assert.isTrue(distinct == lines.size(), "BOM 行存在重复子物料");
	}

}
