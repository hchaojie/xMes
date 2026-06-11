package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.entity.MdSite;

import java.util.List;

/**
 * MdSite 服务
 *
 * @author xmes
 */
public interface MdSiteService extends IService<MdSite> {

	/**
	 * 删除工厂（存在下级车间时拒绝）
	 * @param ids 工厂ID列表
	 */
	void removeSites(List<Long> ids);

}
