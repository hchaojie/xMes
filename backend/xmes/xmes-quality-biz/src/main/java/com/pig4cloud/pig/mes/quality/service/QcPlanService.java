package com.pig4cloud.pig.mes.quality.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.quality.api.dto.QcPlanDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcPlan;

import java.util.List;

/**
 * 检验计划服务
 *
 * @author xmes
 */
public interface QcPlanService extends IService<QcPlan> {

	/**
	 * 查询详情（含特性）
	 * @param id 计划ID
	 * @return 详情
	 */
	QcPlanDTO getDetail(Long id);

	/**
	 * 新增（含特性）
	 * @param dto 计划
	 */
	void saveWithCharacteristics(QcPlanDTO dto);

	/**
	 * 修改（全量替换特性）
	 * @param dto 计划
	 */
	void updateWithCharacteristics(QcPlanDTO dto);

	/**
	 * 删除（被检验单引用时拒绝）
	 * @param ids 计划ID列表
	 */
	void removeWithCharacteristics(List<Long> ids);

}
