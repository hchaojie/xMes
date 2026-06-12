package com.pig4cloud.pig.mes.quality.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.quality.api.dto.QcPlanDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcPlan;
import com.pig4cloud.pig.mes.quality.service.QcPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 检验计划管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/quality/plan")
@Tag(description = "qcPlan", name = "检验计划管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class QcPlanController {

	private final QcPlanService qcPlanService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_qcplan_view")
	public R<Page<QcPlan>> getPage(@ParameterObject Page<QcPlan> page, @ParameterObject QcPlan query) {
		return R.ok(qcPlanService.page(page, Wrappers.<QcPlan>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getPlanName()), QcPlan::getPlanName, query.getPlanName())
			.eq(StrUtil.isNotBlank(query.getPlanType()), QcPlan::getPlanType, query.getPlanType())
			.eq(StrUtil.isNotBlank(query.getStatus()), QcPlan::getStatus, query.getStatus())
			.orderByDesc(QcPlan::getCreateTime)));
	}

	/**
	 * 通过ID查询详情（含特性）
	 * @param id 计划ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_qcplan_view")
	public R<QcPlanDTO> getById(@PathVariable("id") Long id) {
		return R.ok(qcPlanService.getDetail(id));
	}

	/**
	 * 新增检验计划（含特性）
	 * @param dto 检验计划
	 * @return R
	 */
	@Operation(description = "新增检验计划", summary = "新增检验计划")
	@SysLog("新增检验计划")
	@PostMapping
	@HasPermission("mes_qcplan_add")
	public R<Boolean> save(@Valid @RequestBody QcPlanDTO dto) {
		qcPlanService.saveWithCharacteristics(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 修改检验计划（全量替换特性）
	 * @param dto 检验计划
	 * @return R
	 */
	@Operation(description = "修改检验计划", summary = "修改检验计划")
	@SysLog("修改检验计划")
	@PutMapping
	@HasPermission("mes_qcplan_edit")
	public R<Boolean> update(@Valid @RequestBody QcPlanDTO dto) {
		qcPlanService.updateWithCharacteristics(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 批量删除检验计划
	 * @param ids 计划ID列表
	 * @return R
	 */
	@Operation(description = "批量删除检验计划", summary = "批量删除检验计划")
	@SysLog("删除检验计划")
	@DeleteMapping
	@HasPermission("mes_qcplan_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		qcPlanService.removeWithCharacteristics(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
