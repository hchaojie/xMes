package com.pig4cloud.pig.mes.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.dto.ShiftModelDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;
import com.pig4cloud.pig.mes.core.api.entity.MdShiftModel;
import com.pig4cloud.pig.mes.core.api.entity.MdShiftSegment;
import com.pig4cloud.pig.mes.core.mapper.MdCalendarDayMapper;
import com.pig4cloud.pig.mes.core.mapper.MdShiftModelMapper;
import com.pig4cloud.pig.mes.core.mapper.MdShiftSegmentMapper;
import com.pig4cloud.pig.mes.core.service.MdShiftModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 班次模型服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdShiftModelServiceImpl extends ServiceImpl<MdShiftModelMapper, MdShiftModel>
		implements MdShiftModelService {

	private final MdShiftSegmentMapper segmentMapper;

	private final MdCalendarDayMapper calendarDayMapper;

	@Override
	public ShiftModelDTO getDetail(Long id) {
		MdShiftModel model = this.getById(id);
		Assert.notNull(model, "班次模型不存在");
		ShiftModelDTO dto = BeanUtil.copyProperties(model, ShiftModelDTO.class);
		dto.setSegments(segmentMapper.selectList(Wrappers.<MdShiftSegment>lambdaQuery()
			.eq(MdShiftSegment::getShiftModelId, id)
			.orderByAsc(MdShiftSegment::getSortOrder)));
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveWithSegments(ShiftModelDTO dto) {
		Assert.notEmpty(dto.getSegments(), "班段不能为空");
		this.save(dto);
		insertSegments(dto.getId(), dto.getSegments());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateWithSegments(ShiftModelDTO dto) {
		Assert.notEmpty(dto.getSegments(), "班段不能为空");
		this.updateById(dto);
		segmentMapper.delete(
				Wrappers.<MdShiftSegment>lambdaQuery().eq(MdShiftSegment::getShiftModelId, dto.getId()));
		insertSegments(dto.getId(), dto.getSegments());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeWithSegments(List<Long> ids) {
		Long refCount = calendarDayMapper
			.selectCount(Wrappers.<MdCalendarDay>lambdaQuery().in(MdCalendarDay::getShiftModelId, ids));
		Assert.isTrue(refCount == 0, "班次模型已被工厂日历引用，无法删除");
		this.removeBatchByIds(ids);
		segmentMapper.delete(Wrappers.<MdShiftSegment>lambdaQuery().in(MdShiftSegment::getShiftModelId, ids));
	}

	private void insertSegments(Long modelId, List<MdShiftSegment> segments) {
		if (CollectionUtils.isEmpty(segments)) {
			return;
		}
		int sort = 0;
		for (MdShiftSegment segment : segments) {
			segment.setId(null);
			segment.setShiftModelId(modelId);
			segment.setSortOrder(sort++);
			segmentMapper.insert(segment);
		}
	}

}
