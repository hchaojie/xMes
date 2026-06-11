package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;

import java.util.List;

/**
 * 物料服务
 *
 * @author xmes
 */
public interface MdMaterialService extends IService<MdMaterial> {

	/**
	 * 删除物料（被 BOM/工作计划引用时拒绝）
	 * @param ids 物料ID列表
	 */
	void removeMaterials(List<Long> ids);

}
