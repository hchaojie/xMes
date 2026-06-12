package com.pig4cloud.pig.mes.quality.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.quality.api.dto.QcPlanDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcCharacteristic;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.api.entity.QcPlan;
import com.pig4cloud.pig.mes.quality.mapper.QcCharacteristicMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcInspectionOrderMapper;
import com.pig4cloud.pig.mes.quality.mapper.QcPlanMapper;
import com.pig4cloud.pig.mes.quality.service.QcPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 检验计划服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class QcPlanServiceImpl extends ServiceImpl<QcPlanMapper, QcPlan> implements QcPlanService {

	private final QcCharacteristicMapper characteristicMapper;

	private final QcInspectionOrderMapper inspectionOrderMapper;

	@Override
	public QcPlanDTO getDetail(Long id) {
		QcPlan plan = this.getById(id);
		Assert.notNull(plan, "检验计划不存在");
		QcPlanDTO dto = BeanUtil.copyProperties(plan, QcPlanDTO.class);
		dto.setCharacteristics(characteristicMapper.selectList(Wrappers.<QcCharacteristic>lambdaQuery()
			.eq(QcCharacteristic::getPlanId, id)
			.orderByAsc(QcCharacteristic::getSortOrder)));
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveWithCharacteristics(QcPlanDTO dto) {
		Assert.notEmpty(dto.getCharacteristics(), "检验特性不能为空");
		this.save(dto);
		insertCharacteristics(dto.getId(), dto.getCharacteristics());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateWithCharacteristics(QcPlanDTO dto) {
		Assert.notEmpty(dto.getCharacteristics(), "检验特性不能为空");
		this.updateById(dto);
		characteristicMapper
			.delete(Wrappers.<QcCharacteristic>lambdaQuery().eq(QcCharacteristic::getPlanId, dto.getId()));
		insertCharacteristics(dto.getId(), dto.getCharacteristics());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeWithCharacteristics(List<Long> ids) {
		Long ref = inspectionOrderMapper
			.selectCount(Wrappers.<QcInspectionOrder>lambdaQuery().in(QcInspectionOrder::getPlanId, ids));
		Assert.isTrue(ref == 0, "检验计划已被检验单引用，无法删除（可停用）");
		this.removeBatchByIds(ids);
		characteristicMapper.delete(Wrappers.<QcCharacteristic>lambdaQuery().in(QcCharacteristic::getPlanId, ids));
	}

	private void insertCharacteristics(Long planId, List<QcCharacteristic> characteristics) {
		if (CollectionUtils.isEmpty(characteristics)) {
			return;
		}
		int sort = 0;
		for (QcCharacteristic c : characteristics) {
			Assert.isTrue(!"QUANT".equals(c.getCharType()) || c.getUpperLimit() != null || c.getLowerLimit() != null,
					"计量特性必须至少填写上限或下限");
			c.setId(null);
			c.setPlanId(planId);
			c.setSortOrder(sort++);
			characteristicMapper.insert(c);
		}
	}

}
