package com.pig4cloud.pig.mes.quality.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.quality.api.dto.QcInspectionDetailDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionResult;

import java.util.List;

/**
 * 检验单服务
 *
 * @author xmes
 */
public interface QcInspectionService extends IService<QcInspectionOrder> {

	/**
	 * 扫描质量门完工作业生成检验单（pull 模式：打开工作台/定时触发）。
	 * 服务拆分后改为订阅作业完工事件。
	 * @return 新生成的检验单数量
	 */
	int refreshGateOrders();

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param status 状态过滤（可空）
	 * @return 分页结果
	 */
	Page<QcInspectionOrder> pageOrders(Page<QcInspectionOrder> page, String status);

	/**
	 * 详情（计划特性 + 已录结果）
	 * @param id 检验单ID
	 * @return 详情
	 */
	QcInspectionDetailDTO getDetail(Long id);

	/**
	 * 领取检验单：待检 → 检验中
	 * @param id 检验单ID
	 */
	void receive(Long id);

	/**
	 * 录入检验结果（计量特性按上下限自动判定 OK/NG）
	 * @param results 结果行列表
	 */
	void record(List<QcInspectionResult> results);

	/**
	 * 用途判定：合格 / 让步接收 / 不合格（不合格须填判定说明）
	 * @param id 检验单ID
	 * @param judge PASSED/CONCESSION/REJECTED
	 * @param judgeRemark 判定说明
	 */
	void judge(Long id, String judge, String judgeRemark);

}
