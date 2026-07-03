package com.pig4cloud.pig.mes.core.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.MntOrderDTO;
import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import com.pig4cloud.pig.mes.core.service.MntOrderService;
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
import java.util.Map;

/**
 * 维护工单管理（FR-RES-32/33/35/36：报修、状态流、点检 NG 自动报修、MTBF/MTTR 分析）
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/maintenance/order")
@Tag(description = "mntOrder", name = "维护工单管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MntOrderController {

	private final MntOrderService mntOrderService;

	/**
	 * 分页查询（附设备信息）
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_mnt_view")
	public R<Page<MntOrderDTO>> getPage(@ParameterObject Page<MntOrder> page, @ParameterObject MntOrder query) {
		return R.ok(mntOrderService.pageWithWorkplace(page, query));
	}

	/**
	 * 通过ID查询详情（含检查项结果）
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_mnt_view")
	public R<MntOrderDTO> getById(@PathVariable("id") Long id) {
		return R.ok(mntOrderService.getDetail(id));
	}

	/**
	 * 故障报修（生成维修工单）
	 * @param order 报修信息（workplaceId + faultDesc）
	 * @return 工单ID
	 */
	@Operation(description = "故障报修", summary = "故障报修")
	@SysLog("故障报修")
	@PostMapping("/report")
	@HasPermission("mes_mnt_report")
	public R<Long> report(@Valid @RequestBody MntOrder order) {
		return R.ok(mntOrderService.reportRepair(order));
	}

	/**
	 * 接单开始维修（设备进入 MAINT）
	 * @param id 工单ID
	 * @return R
	 */
	@Operation(description = "接单开始维修", summary = "接单开始维修")
	@SysLog("维护工单接单")
	@PostMapping("/accept/{id}")
	@HasPermission("mes_mnt_handle")
	public R<Boolean> accept(@PathVariable("id") Long id) {
		mntOrderService.accept(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 完成维修/保养/点检（录入原因、措施、检查项结果；NG 项自动报修）
	 * @param dto 完工信息
	 * @return R
	 */
	@Operation(description = "完工提交", summary = "完工提交")
	@SysLog("维护工单完工")
	@PostMapping("/finish")
	@HasPermission("mes_mnt_handle")
	public R<Boolean> finish(@RequestBody MntOrderDTO dto) {
		mntOrderService.finish(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 验证关闭
	 * @param id 工单ID
	 * @return R
	 */
	@Operation(description = "验证关闭", summary = "验证关闭")
	@SysLog("维护工单关闭")
	@PostMapping("/close/{id}")
	@HasPermission("mes_mnt_close")
	public R<Boolean> close(@PathVariable("id") Long id) {
		mntOrderService.close(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 取消（仅待接单）
	 * @param id 工单ID
	 * @return R
	 */
	@Operation(description = "取消工单", summary = "取消工单")
	@SysLog("维护工单取消")
	@PostMapping("/cancel/{id}")
	@HasPermission("mes_mnt_handle")
	public R<Boolean> cancel(@PathVariable("id") Long id) {
		mntOrderService.cancel(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 维护分析（MTBF/MTTR、故障 Pareto、保养达成率）
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 分析结果
	 */
	@Operation(description = "维护分析", summary = "维护分析")
	@GetMapping("/analysis")
	@HasPermission("mes_mnt_analysis")
	public R<Map<String, Object>> analysis(
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
		return R.ok(mntOrderService.analysis(startDate, endDate));
	}

}
