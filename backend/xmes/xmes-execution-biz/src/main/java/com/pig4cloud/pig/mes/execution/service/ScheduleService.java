package com.pig4cloud.pig.mes.execution.service;

import com.pig4cloud.pig.mes.execution.api.dto.ScheduleAssignDTO;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 作业排程服务（人工交互式排程，有限产能冲突检测）
 *
 * @author xmes
 */
public interface ScheduleService {

	/**
	 * 加载排程看板：资源行（工作中心→工位）、时间窗内作业条、待排清单、非工作日
	 * @param startDate 时间窗开始
	 * @param endDate 时间窗结束
	 * @param workCenterId 工作中心过滤（可空）
	 * @return 看板数据
	 */
	ScheduleBoardVO board(LocalDate startDate, LocalDate endDate, Long workCenterId);

	/**
	 * 分派作业到工位与时间（拖拽落点）：
	 * 结束时间 = 开始 + 准备时间 + 数量×单件时间；
	 * 有限产能校验（同工位时间重叠拒绝）；工序顺序校验（不得早于上道排程结束）
	 * @param dto 分派参数
	 */
	void assign(ScheduleAssignDTO dto);

	/**
	 * 取消排程：已排程 → 待排（清空工位与时间）
	 * @param taskId 作业ID
	 */
	void unassign(Long taskId);

	/**
	 * 批量派工：已排程 → 已派工（发布到工位执行队列）
	 * @param taskIds 作业ID列表
	 */
	void dispatch(List<Long> taskIds);

}
