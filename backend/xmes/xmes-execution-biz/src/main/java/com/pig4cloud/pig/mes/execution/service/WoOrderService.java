package com.pig4cloud.pig.mes.execution.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.execution.api.dto.WoOrderDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;

import java.util.List;

/**
 * 工单服务
 *
 * @author xmes
 */
public interface WoOrderService extends IService<WoOrder> {

	/**
	 * 从工作计划生成工单（生成工艺快照与作业列表，状态=已创建）
	 * @param dto 工单（materialId/quantity 必填；routingId 可空，默认取该物料当前已发布版本）
	 * @return 工单ID
	 */
	Long createFromPlan(WoOrderDTO dto);

	/**
	 * 批量下达：已创建 → 已下达
	 * @param ids 工单ID列表
	 */
	void release(List<Long> ids);

	/**
	 * 挂起：已下达/执行中 → 挂起
	 * @param id 工单ID
	 * @param reason 挂起原因（必填）
	 */
	void hold(Long id, String reason);

	/**
	 * 恢复：挂起 → 已下达
	 * @param id 工单ID
	 */
	void resume(Long id);

	/**
	 * 取消：已创建/已下达且无任何报工数量 → 已取消
	 * @param id 工单ID
	 */
	void cancel(Long id);

	/**
	 * 删除（仅已创建状态，作业级联删除）
	 * @param ids 工单ID列表
	 */
	void removeCreated(List<Long> ids);

	/**
	 * 分页查询（携带物料编码/名称）
	 * @param page 分页对象
	 * @param query 过滤条件（orderNo 模糊、orderStatus、materialId）
	 * @return 分页结果
	 */
	Page<WoOrderDTO> pageDetail(Page<WoOrder> page, WoOrder query);

	/**
	 * 查询详情（含作业列表）
	 * @param id 工单ID
	 * @return 工单详情
	 */
	WoOrderDTO getDetail(Long id);

}
