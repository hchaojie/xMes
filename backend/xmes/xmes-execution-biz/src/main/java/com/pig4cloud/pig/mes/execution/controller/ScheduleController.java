package com.pig4cloud.pig.mes.execution.controller;

import cn.hutool.core.collection.CollUtil;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleAssignDTO;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;
import com.pig4cloud.pig.mes.execution.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 作业排程
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/schedule")
@Tag(description = "schedule", name = "作业排程")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ScheduleController {

	private final ScheduleService scheduleService;

	/**
	 * 排程看板数据
	 * @param startDate 时间窗开始
	 * @param endDate 时间窗结束
	 * @param workCenterId 工作中心过滤（可空）
	 * @return 看板数据
	 */
	@Operation(description = "排程看板数据", summary = "排程看板数据")
	@GetMapping("/board")
	@HasPermission("mes_schedule_view")
	public R<ScheduleBoardVO> board(
			@RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			@RequestParam(value = "workCenterId", required = false) Long workCenterId) {
		return R.ok(scheduleService.board(startDate, endDate, workCenterId));
	}

	/**
	 * 分派作业（拖拽落点）
	 * @param dto 分派参数
	 * @return R
	 */
	@Operation(description = "分派作业", summary = "分派作业")
	@SysLog("排程分派")
	@PutMapping("/assign")
	@HasPermission("mes_schedule_edit")
	public R<Boolean> assign(@Valid @RequestBody ScheduleAssignDTO dto) {
		scheduleService.assign(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 取消排程
	 * @param taskId 作业ID
	 * @return R
	 */
	@Operation(description = "取消排程", summary = "取消排程")
	@SysLog("取消排程")
	@PutMapping("/unassign/{taskId}")
	@HasPermission("mes_schedule_edit")
	public R<Boolean> unassign(@PathVariable("taskId") Long taskId) {
		scheduleService.unassign(taskId);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 批量派工（发布到工位执行队列）
	 * @param taskIds 作业ID列表
	 * @return R
	 */
	@Operation(description = "批量派工", summary = "批量派工")
	@SysLog("批量派工")
	@PutMapping("/dispatch")
	@HasPermission("mes_schedule_edit")
	public R<Boolean> dispatch(@RequestBody Long[] taskIds) {
		scheduleService.dispatch(CollUtil.toList(taskIds));
		return R.ok(Boolean.TRUE);
	}

}
