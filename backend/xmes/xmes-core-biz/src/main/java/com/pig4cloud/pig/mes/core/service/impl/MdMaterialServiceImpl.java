package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.entity.MdBom;
import com.pig4cloud.pig.mes.core.api.entity.MdBomLine;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.mapper.MdBomLineMapper;
import com.pig4cloud.pig.mes.core.mapper.MdBomMapper;
import com.pig4cloud.pig.mes.core.mapper.MdMaterialMapper;
import com.pig4cloud.pig.mes.core.mapper.MdRoutingMapper;
import com.pig4cloud.pig.mes.core.service.MdMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 物料服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdMaterialServiceImpl extends ServiceImpl<MdMaterialMapper, MdMaterial> implements MdMaterialService {

	private final MdBomMapper bomMapper;

	private final MdBomLineMapper bomLineMapper;

	private final MdRoutingMapper routingMapper;

	@Override
	public void removeMaterials(List<Long> ids) {
		Long bomRef = bomMapper.selectCount(Wrappers.<MdBom>lambdaQuery().in(MdBom::getMaterialId, ids));
		Long lineRef = bomLineMapper
			.selectCount(Wrappers.<MdBomLine>lambdaQuery().in(MdBomLine::getChildMaterialId, ids));
		Long routeRef = routingMapper
			.selectCount(Wrappers.<MdRouting>lambdaQuery().in(MdRouting::getMaterialId, ids));
		Assert.isTrue(bomRef == 0 && lineRef == 0 && routeRef == 0, "物料已被 BOM 或工作计划引用，无法删除");
		this.removeBatchByIds(ids);
	}

}
