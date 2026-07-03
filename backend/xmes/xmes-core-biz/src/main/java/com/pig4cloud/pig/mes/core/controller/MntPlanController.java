package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.MntPlanDTO;
import com.pig4cloud.pig.mes.core.api.entity.MntPlan;
import com.pig4cloud.pig.mes.core.service.MntPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 维护计划管理（FR-RES-31：保养/点检计划与检查表）
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/maintenance/plan")
@Tag(description = "mntPlan", name = "维护计划管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MntPlanController {

	private final MntPlanService mntPlanService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_mntplan_view")
	public R<Page<MntPlan>> getPage(@ParameterObject Page<MntPlan> page, @ParameterObject MntPlan query) {
		return R.ok(mntPlanService.page(page, Wrappers.<MntPlan>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getPlanName()), MntPlan::getPlanName, query.getPlanName())
			.eq(StrUtil.isNotBlank(query.getPlanType()), MntPlan::getPlanType, query.getPlanType())
			.eq(query.getWorkplaceId() != null, MntPlan::getWorkplaceId, query.getWorkplaceId())
			.eq(StrUtil.isNotBlank(query.getStatus()), MntPlan::getStatus, query.getStatus())
			.orderByAsc(MntPlan::getPlanCode)));
	}

	/**
	 * 通过ID查询详情（含检查表）
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_mntplan_view")
	public R<MntPlanDTO> getById(@PathVariable("id") Long id) {
		return R.ok(mntPlanService.getDetail(id));
	}

	/**
	 * 新增维护计划
	 * @param dto 维护计划（含检查表）
	 * @return R
	 */
	@Operation(description = "新增维护计划", summary = "新增维护计划")
	@SysLog("新增维护计划")
	@PostMapping
	@HasPermission("mes_mntplan_add")
	public R<Boolean> save(@Valid @RequestBody MntPlanDTO dto) {
		mntPlanService.saveWithItems(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 修改维护计划
	 * @param dto 维护计划（含检查表）
	 * @return R
	 */
	@Operation(description = "修改维护计划", summary = "修改维护计划")
	@SysLog("修改维护计划")
	@PutMapping
	@HasPermission("mes_mntplan_edit")
	public R<Boolean> updateById(@Valid @RequestBody MntPlanDTO dto) {
		mntPlanService.updateWithItems(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 通过ID批量删除维护计划
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除维护计划", summary = "批量删除维护计划")
	@SysLog("删除维护计划")
	@DeleteMapping
	@HasPermission("mes_mntplan_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mntPlanService.removeWithItems(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 扫描到期计划生成维护工单（FR-RES-32）
	 * @return 生成的工单数
	 */
	@Operation(description = "生成到期维护工单", summary = "生成到期维护工单")
	@SysLog("生成到期维护工单")
	@PostMapping("/generate-due")
	@HasPermission("mes_mntplan_gen")
	public R<Integer> generateDue() {
		return R.ok(mntPlanService.generateDueOrders());
	}

}
