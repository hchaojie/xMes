package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.entity.MdArea;

import java.util.List;

/**
 * MdArea 服务
 *
 * @author xmes
 */
public interface MdAreaService extends IService<MdArea> {

	/**
	 * 删除车间（存在下级工作中心时拒绝）
	 * @param ids 车间ID列表
	 */
	void removeAreas(List<Long> ids);

}
