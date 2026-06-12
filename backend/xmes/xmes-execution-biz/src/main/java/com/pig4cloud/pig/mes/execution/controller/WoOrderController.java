package com.pig4cloud.pig.mes.execution.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.execution.api.dto.WoOrderDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.service.WoOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 工单管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/work-order")
@Tag(description = "workOrder", name = "工单管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class WoOrderController {

	private final WoOrderService woOrderService;

	/**
	 * 分页查询（携带物料编码/名称）
	 * @param page 分页对象
	 * @param query 过滤条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_wo_view")
	public R<Page<WoOrderDTO>> getPage(@ParameterObject Page<WoOrder> page, @ParameterObject WoOrder query) {
		return R.ok(woOrderService.pageDetail(page, query));
	}

	/**
	 * 通过ID查询详情（含作业列表）
	 * @param id 工单ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_wo_view")
	public R<WoOrderDTO> getById(@PathVariable("id") Long id) {
		return R.ok(woOrderService.getDetail(id));
	}

	/**
	 * 从工作计划生成工单
	 * @param dto 工单
	 * @return 工单ID
	 */
	@Operation(description = "从工作计划生成工单", summary = "从工作计划生成工单")
	@SysLog("创建工单")
	@PostMapping
	@HasPermission("mes_wo_add")
	public R<Long> save(@Valid @RequestBody WoOrderDTO dto) {
		return R.ok(woOrderService.createFromPlan(dto));
	}

	/**
	 * 批量下达工单
	 * @param ids 工单ID列表
	 * @return R
	 */
	@Operation(description = "批量下达工单", summary = "批量下达工单")
	@SysLog("下达工单")
	@PutMapping("/release")
	@HasPermission("mes_wo_release")
	public R<Boolean> release(@RequestBody Long[] ids) {
		woOrderService.release(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 挂起工单
	 * @param id 工单ID
	 * @param reason 挂起原因
	 * @return R
	 */
	@Operation(description = "挂起工单", summary = "挂起工单")
	@SysLog("挂起工单")
	@PutMapping("/hold/{id}")
	@HasPermission("mes_wo_hold")
	public R<Boolean> hold(@PathVariable("id") Long id, @RequestParam("reason") String reason) {
		woOrderService.hold(id, reason);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 恢复工单
	 * @param id 工单ID
	 * @return R
	 */
	@Operation(description = "恢复工单", summary = "恢复工单")
	@SysLog("恢复工单")
	@PutMapping("/resume/{id}")
	@HasPermission("mes_wo_hold")
	public R<Boolean> resume(@PathVariable("id") Long id) {
		woOrderService.resume(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 取消工单
	 * @param id 工单ID
	 * @return R
	 */
	@Operation(description = "取消工单", summary = "取消工单")
	@SysLog("取消工单")
	@PutMapping("/cancel/{id}")
	@HasPermission("mes_wo_cancel")
	public R<Boolean> cancel(@PathVariable("id") Long id) {
		woOrderService.cancel(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 批量删除工单（仅已创建状态）
	 * @param ids 工单ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工单", summary = "批量删除工单")
	@SysLog("删除工单")
	@DeleteMapping
	@HasPermission("mes_wo_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		woOrderService.removeCreated(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
