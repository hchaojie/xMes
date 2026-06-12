package com.pig4cloud.pig.mes.execution.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.execution.api.dto.BookingQtyDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoBooking;

import java.util.List;

/**
 * 报工服务（事件不可变，修正以负向冲销实现）
 *
 * @author xmes
 */
public interface WoBookingService extends IService<WoBooking> {

	/**
	 * 开工：校验工单状态、工序串行约束（上道完工或满足转移批量）后，作业 → 进行中
	 * @param taskId 作业ID
	 * @param workplaceId 工位ID（可空）
	 */
	void start(Long taskId, Long workplaceId, String source);

	/**
	 * 暂停：进行中 → 暂停
	 * @param taskId 作业ID
	 * @param reasonCode 暂停原因码（必填）
	 */
	void pause(Long taskId, String reasonCode, String source);

	/**
	 * 恢复：暂停 → 进行中
	 * @param taskId 作业ID
	 */
	void resume(Long taskId, String source);

	/**
	 * 报数：数量守恒校验（累计 ≤ 上道完工良品 / 工单计划数），报废必填原因
	 * @param dto 报数参数
	 */
	void reportQty(BookingQtyDTO dto);

	/**
	 * 完工（可携带最后一笔数量）：作业 → 完工；末道完工时同步工单数量并尝试完工工单
	 * @param dto 完工参数
	 */
	void finish(BookingQtyDTO dto);

	/**
	 * 冲销数量事件：生成负向 REVERSE 事件并回滚作业数量（仅未完工作业的 QTY 事件）
	 * @param bookingId 原事件ID
	 * @param remark 冲销说明
	 */
	void reverse(Long bookingId, String remark);

	/**
	 * 查询作业的事件流
	 * @param taskId 作业ID
	 * @return 事件列表（时间正序）
	 */
	List<WoBooking> listByTask(Long taskId);

}
