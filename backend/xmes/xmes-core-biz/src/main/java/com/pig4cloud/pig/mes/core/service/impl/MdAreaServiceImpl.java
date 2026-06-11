package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.entity.MdArea;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.mapper.MdWorkCenterMapper;
import com.pig4cloud.pig.mes.core.mapper.MdAreaMapper;
import com.pig4cloud.pig.mes.core.service.MdAreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * MdArea 服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdAreaServiceImpl extends ServiceImpl<MdAreaMapper, MdArea> implements MdAreaService {

	private final MdWorkCenterMapper childMapper;

	@Override
	public void removeAreas(List<Long> ids) {
		Long count = childMapper.selectCount(Wrappers.<MdWorkCenter>lambdaQuery().in(MdWorkCenter::getAreaId, ids));
		Assert.isTrue(count == 0, "存在下级工作中心，无法删除车间");
		this.removeBatchByIds(ids);
	}

}
