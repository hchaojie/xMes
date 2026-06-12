package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.entity.MdSite;
import com.pig4cloud.pig.mes.core.api.entity.MdArea;
import com.pig4cloud.pig.mes.core.mapper.MdAreaMapper;
import com.pig4cloud.pig.mes.core.mapper.MdSiteMapper;
import com.pig4cloud.pig.mes.core.service.MdSiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * MdSite 服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdSiteServiceImpl extends ServiceImpl<MdSiteMapper, MdSite> implements MdSiteService {

	private final MdAreaMapper childMapper;

	@Override
	public void removeSites(List<Long> ids) {
		Long count = childMapper.selectCount(Wrappers.<MdArea>lambdaQuery().in(MdArea::getSiteId, ids));
		Assert.isTrue(count == 0, "存在下级车间，无法删除工厂");
		this.removeBatchByIds(ids);
	}

}
