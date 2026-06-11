package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.CalendarDayBatchDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendar;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;
import com.pig4cloud.pig.mes.core.service.MdCalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 工厂日历管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/calendar")
@Tag(description = "calendar", name = "工厂日历管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdCalendarController {

	private final MdCalendarService mdCalendarService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_cal_view")
	public R<Page<MdCalendar>> getPage(@ParameterObject Page<MdCalendar> page, @ParameterObject MdCalendar query) {
		return R.ok(mdCalendarService.page(page, Wrappers.<MdCalendar>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getCalendarName()), MdCalendar::getCalendarName, query.getCalendarName())
			.eq(query.getSiteId() != null, MdCalendar::getSiteId, query.getSiteId())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdCalendar::getStatus, query.getStatus())));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_cal_view")
	public R<List<MdCalendar>> getList() {
		return R.ok(mdCalendarService.list());
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_cal_view")
	public R<MdCalendar> getById(@PathVariable("id") Long id) {
		return R.ok(mdCalendarService.getById(id));
	}

	/**
	 * 新增工厂日历
	 * @param calendar 工厂日历
	 * @return R
	 */
	@Operation(description = "新增工厂日历", summary = "新增工厂日历")
	@SysLog("新增工厂日历")
	@PostMapping
	@HasPermission("mes_cal_add")
	public R<Boolean> save(@Valid @RequestBody MdCalendar calendar) {
		return R.ok(mdCalendarService.save(calendar));
	}

	/**
	 * 修改工厂日历
	 * @param calendar 工厂日历
	 * @return R
	 */
	@Operation(description = "修改工厂日历", summary = "修改工厂日历")
	@SysLog("修改工厂日历")
	@PutMapping
	@HasPermission("mes_cal_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdCalendar calendar) {
		return R.ok(mdCalendarService.updateById(calendar));
	}

	/**
	 * 通过ID批量删除工厂日历
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工厂日历", summary = "批量删除工厂日历")
	@SysLog("删除工厂日历")
	@DeleteMapping
	@HasPermission("mes_cal_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdCalendarService.removeWithDays(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 查询日历日明细
	 * @param calendarId 日历ID
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 日明细列表
	 */
	@Operation(description = "查询日历日明细", summary = "查询日历日明细")
	@GetMapping("/days")
	@HasPermission("mes_cal_view")
	public R<List<MdCalendarDay>> listDays(@RequestParam("calendarId") Long calendarId,
			@RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return R.ok(mdCalendarService.listDays(calendarId, startDate, endDate));
	}

	/**
	 * 批量设置日历日（按日期区间与星期过滤，同日覆盖）
	 * @param dto 批量设置参数
	 * @return 影响天数
	 */
	@Operation(description = "批量设置日历日", summary = "批量设置日历日")
	@SysLog("批量设置日历日")
	@PostMapping("/days/batch")
	@HasPermission("mes_cal_edit")
	public R<Integer> batchSetDays(@Valid @RequestBody CalendarDayBatchDTO dto) {
		return R.ok(mdCalendarService.batchSetDays(dto));
	}

}
