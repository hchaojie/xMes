package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.core.mapper.MdWorkplaceMapper;
import com.pig4cloud.pig.mes.core.mapper.MdWorkCenterMapper;
import com.pig4cloud.pig.mes.core.service.MdWorkCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * MdWorkCenter 服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdWorkCenterServiceImpl extends ServiceImpl<MdWorkCenterMapper, MdWorkCenter> implements MdWorkCenterService {

	private final MdWorkplaceMapper childMapper;

	@Override
	public void removeWorkCenters(List<Long> ids) {
		Long count = childMapper.selectCount(Wrappers.<MdWorkplace>lambdaQuery().in(MdWorkplace::getWorkCenterId, ids));
		Assert.isTrue(count == 0, "存在下级工位，无法删除工作中心");
		this.removeBatchByIds(ids);
	}

}
