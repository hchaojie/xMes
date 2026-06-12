package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;

import java.util.List;

/**
 * MdWorkCenter 服务
 *
 * @author xmes
 */
public interface MdWorkCenterService extends IService<MdWorkCenter> {

	/**
	 * 删除工作中心（存在下级工位时拒绝）
	 * @param ids 工作中心ID列表
	 */
	void removeWorkCenters(List<Long> ids);

}
