package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.dto.MntOrderDTO;
import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import com.pig4cloud.pig.mes.core.api.entity.MntPlan;

import java.time.LocalDate;
import java.util.Map;

/**
 * 维护工单服务（FR-RES-32/33/35/36）
 *
 * @author xmes
 */
public interface MntOrderService extends IService<MntOrder> {

	/**
	 * 分页查询（附设备编码/名称）
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	Page<MntOrderDTO> pageWithWorkplace(Page<MntOrder> page, MntOrder query);

	/**
	 * 查询详情（含检查项结果）
	 * @param id 工单ID
	 * @return 详情
	 */
	MntOrderDTO getDetail(Long id);

	/**
	 * 由维护计划生成工单（PM/SPOT，PENDING）
	 * @param plan 到期的维护计划
	 * @return 已入库的工单
	 */
	MntOrder createFromPlan(MntPlan plan);

	/**
	 * 故障报修：创建 REPAIR 维护工单（PENDING）
	 * @param order 报修信息（workplaceId + faultDesc）
	 * @return 工单ID
	 */
	Long reportRepair(MntOrder order);

	/**
	 * 接单开始维修：PENDING → IN_PROGRESS（设备进入 MAINT，监控墙联动 FR-RES-33）
	 * @param id 工单ID
	 */
	void accept(Long id);

	/**
	 * 完成维修/保养/点检：IN_PROGRESS → REVIEW；
	 * 记录原因/措施/备件与检查项结果，点检 NG 项自动生成维修工单（FR-RES-36）
	 * @param dto 完工信息（含检查项结果）
	 */
	void finish(MntOrderDTO dto);

	/**
	 * 验证关闭：REVIEW → CLOSED
	 * @param id 工单ID
	 */
	void close(Long id);

	/**
	 * 取消：PENDING → CANCELLED
	 * @param id 工单ID
	 */
	void cancel(Long id);

	/**
	 * 维护分析（FR-RES-35）：MTBF/MTTR（按设备）、故障 Pareto、保养计划达成率
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 分析结果
	 */
	Map<String, Object> analysis(LocalDate startDate, LocalDate endDate);

}
