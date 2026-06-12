package com.pig4cloud.pig.mes.execution.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.execution.api.dto.BookingQtyDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoBooking;
import com.pig4cloud.pig.mes.execution.service.WoBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报工管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/booking")
@Tag(description = "booking", name = "报工管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class WoBookingController {

	private final WoBookingService woBookingService;

	/**
	 * 开工
	 * @param taskId 作业ID
	 * @param workplaceId 工位ID（可空）
	 * @return R
	 */
	@Operation(description = "开工", summary = "开工")
	@SysLog("作业开工")
	@PostMapping("/start/{taskId}")
	@HasPermission("mes_booking_operate")
	public R<Boolean> start(@PathVariable("taskId") Long taskId,
			@RequestParam(value = "workplaceId", required = false) Long workplaceId,
			@RequestParam(value = "source", required = false) String source) {
		woBookingService.start(taskId, workplaceId, source);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 暂停
	 * @param taskId 作业ID
	 * @param reasonCode 暂停原因码
	 * @return R
	 */
	@Operation(description = "暂停", summary = "暂停")
	@SysLog("作业暂停")
	@PostMapping("/pause/{taskId}")
	@HasPermission("mes_booking_operate")
	public R<Boolean> pause(@PathVariable("taskId") Long taskId, @RequestParam("reasonCode") String reasonCode,
			@RequestParam(value = "source", required = false) String source) {
		woBookingService.pause(taskId, reasonCode, source);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 恢复
	 * @param taskId 作业ID
	 * @return R
	 */
	@Operation(description = "恢复", summary = "恢复")
	@SysLog("作业恢复")
	@PostMapping("/resume/{taskId}")
	@HasPermission("mes_booking_operate")
	public R<Boolean> resume(@PathVariable("taskId") Long taskId,
			@RequestParam(value = "source", required = false) String source) {
		woBookingService.resume(taskId, source);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 报数
	 * @param dto 报数参数
	 * @return R
	 */
	@Operation(description = "报数", summary = "报数")
	@SysLog("作业报数")
	@PostMapping("/qty")
	@HasPermission("mes_booking_operate")
	public R<Boolean> reportQty(@Valid @RequestBody BookingQtyDTO dto) {
		woBookingService.reportQty(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 完工（可携带最后一笔数量）
	 * @param dto 完工参数
	 * @return R
	 */
	@Operation(description = "完工", summary = "完工")
	@SysLog("作业完工")
	@PostMapping("/finish")
	@HasPermission("mes_booking_operate")
	public R<Boolean> finish(@Valid @RequestBody BookingQtyDTO dto) {
		woBookingService.finish(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 冲销数量事件
	 * @param bookingId 原事件ID
	 * @param remark 冲销说明
	 * @return R
	 */
	@Operation(description = "冲销数量事件", summary = "冲销数量事件")
	@SysLog("报工冲销")
	@PostMapping("/reverse/{bookingId}")
	@HasPermission("mes_booking_reverse")
	public R<Boolean> reverse(@PathVariable("bookingId") Long bookingId,
			@RequestParam(value = "remark", required = false) String remark) {
		woBookingService.reverse(bookingId, remark);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 查询作业事件流
	 * @param taskId 作业ID
	 * @return 事件列表
	 */
	@Operation(description = "查询作业事件流", summary = "查询作业事件流")
	@GetMapping("/task/{taskId}")
	@HasPermission("mes_wo_view")
	public R<List<WoBooking>> listByTask(@PathVariable("taskId") Long taskId) {
		return R.ok(woBookingService.listByTask(taskId));
	}

}
