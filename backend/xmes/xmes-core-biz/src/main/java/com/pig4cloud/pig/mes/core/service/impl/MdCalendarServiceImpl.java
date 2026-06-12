package com.pig4cloud.pig.mes.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.dto.CalendarDayBatchDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendar;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;
import com.pig4cloud.pig.mes.core.api.entity.MdSite;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.mapper.MdCalendarDayMapper;
import com.pig4cloud.pig.mes.core.mapper.MdCalendarMapper;
import com.pig4cloud.pig.mes.core.mapper.MdSiteMapper;
import com.pig4cloud.pig.mes.core.mapper.MdWorkCenterMapper;
import com.pig4cloud.pig.mes.core.service.MdCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 工厂日历服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MdCalendarServiceImpl extends ServiceImpl<MdCalendarMapper, MdCalendar> implements MdCalendarService {

	private final MdCalendarDayMapper dayMapper;

	private final MdSiteMapper siteMapper;

	private final MdWorkCenterMapper workCenterMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int batchSetDays(CalendarDayBatchDTO dto) {
		Assert.notNull(this.getById(dto.getCalendarId()), "工厂日历不存在");
		Assert.isTrue(!dto.getEndDate().isBefore(dto.getStartDate()), "结束日期不能早于开始日期");
		Assert.isTrue(!"WORK".equals(dto.getDayType()) || dto.getShiftModelId() != null, "工作日必须指定班次模型");

		int affected = 0;
		for (LocalDate date = dto.getStartDate(); !date.isAfter(dto.getEndDate()); date = date.plusDays(1)) {
			if (!CollectionUtils.isEmpty(dto.getWeekdays())
					&& !dto.getWeekdays().contains(date.getDayOfWeek().getValue())) {
				continue;
			}
			// 同日覆盖：先删后插
			LocalDate current = date;
			dayMapper.delete(Wrappers.<MdCalendarDay>lambdaQuery()
				.eq(MdCalendarDay::getCalendarId, dto.getCalendarId())
				.eq(MdCalendarDay::getCalDate, current));
			MdCalendarDay day = new MdCalendarDay();
			day.setCalendarId(dto.getCalendarId());
			day.setCalDate(date);
			day.setDayType(dto.getDayType());
			day.setShiftModelId("WORK".equals(dto.getDayType()) ? dto.getShiftModelId() : null);
			dayMapper.insert(day);
			affected++;
		}
		return affected;
	}

	@Override
	public List<MdCalendarDay> listDays(Long calendarId, LocalDate startDate, LocalDate endDate) {
		return dayMapper.selectList(Wrappers.<MdCalendarDay>lambdaQuery()
			.eq(MdCalendarDay::getCalendarId, calendarId)
			.ge(startDate != null, MdCalendarDay::getCalDate, startDate)
			.le(endDate != null, MdCalendarDay::getCalDate, endDate)
			.orderByAsc(MdCalendarDay::getCalDate));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeWithDays(List<Long> ids) {
		Long siteRef = siteMapper.selectCount(Wrappers.<MdSite>lambdaQuery().in(MdSite::getCalendarId, ids));
		Long centerRef = workCenterMapper
			.selectCount(Wrappers.<MdWorkCenter>lambdaQuery().in(MdWorkCenter::getCalendarId, ids));
		Assert.isTrue(siteRef == 0 && centerRef == 0, "日历已被工厂或工作中心引用，无法删除");
		this.removeBatchByIds(ids);
		dayMapper.delete(Wrappers.<MdCalendarDay>lambdaQuery().in(MdCalendarDay::getCalendarId, ids));
	}

}
