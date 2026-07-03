package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.dto.MntPlanDTO;
import com.pig4cloud.pig.mes.core.api.entity.MntPlan;

import java.util.List;

/**
 * 维护计划服务（FR-RES-31）
 *
 * @author xmes
 */
public interface MntPlanService extends IService<MntPlan> {

	/**
	 * 查询详情（含标准作业项）
	 * @param id 计划ID
	 * @return 详情
	 */
	MntPlanDTO getDetail(Long id);

	/**
	 * 新增计划（含标准作业项）
	 * @param dto 计划
	 */
	void saveWithItems(MntPlanDTO dto);

	/**
	 * 修改计划（含标准作业项，全量替换）
	 * @param dto 计划
	 */
	void updateWithItems(MntPlanDTO dto);

	/**
	 * 删除计划（含标准作业项）
	 * @param ids 计划ID列表
	 */
	void removeWithItems(List<Long> ids);

	/**
	 * 扫描到期计划生成维护工单（FR-RES-32：DAY 周期按到期日滚动；
	 * RUNTIME/OUTPUT 周期待物联网模块提供运行数据，当前支持手工维护到期日后一并生成）
	 * @return 生成的工单数
	 */
	int generateDueOrders();

}
