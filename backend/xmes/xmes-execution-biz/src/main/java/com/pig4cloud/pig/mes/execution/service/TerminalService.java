package com.pig4cloud.pig.mes.execution.service;

import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;

import java.util.List;

/**
 * 工位终端服务
 *
 * @author xmes
 */
public interface TerminalService {

	/**
	 * 工位任务队列：该工位 已派工/进行中/暂停 的作业（按排程开始时间排序），
	 * 以及派工到该工位所属工作中心但未指定工位的已排程作业
	 * @param workplaceId 工位ID
	 * @return 任务队列
	 */
	List<ScheduleBoardVO.TaskBar> queue(Long workplaceId);

}
