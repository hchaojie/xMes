package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.dto.CalendarDayBatchDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendar;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;

import java.time.LocalDate;
import java.util.List;

/**
 * 工厂日历服务
 *
 * @author xmes
 */
public interface MdCalendarService extends IService<MdCalendar> {

	/**
	 * 按日期区间与星期过滤批量设置日类型/班次（同日覆盖）
	 * @param dto 批量设置参数
	 * @return 影响天数
	 */
	int batchSetDays(CalendarDayBatchDTO dto);

	/**
	 * 查询日历日明细
	 * @param calendarId 日历ID
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 日明细列表
	 */
	List<MdCalendarDay> listDays(Long calendarId, LocalDate startDate, LocalDate endDate);

	/**
	 * 删除日历及日明细（被工厂/工作中心引用时拒绝）
	 * @param ids 日历ID列表
	 */
	void removeWithDays(List<Long> ids);

}
