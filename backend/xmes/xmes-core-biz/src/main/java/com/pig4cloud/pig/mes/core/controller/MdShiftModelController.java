package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.ShiftModelDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdShiftModel;
import com.pig4cloud.pig.mes.core.service.MdShiftModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班次模型管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/shift-model")
@Tag(description = "shiftModel", name = "班次模型管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdShiftModelController {

	private final MdShiftModelService mdShiftModelService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_shift_view")
	public R<Page<MdShiftModel>> getPage(@ParameterObject Page<MdShiftModel> page,
			@ParameterObject MdShiftModel query) {
		return R.ok(mdShiftModelService.page(page, Wrappers.<MdShiftModel>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getModelName()), MdShiftModel::getModelName, query.getModelName())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdShiftModel::getStatus, query.getStatus())));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_shift_view")
	public R<List<MdShiftModel>> getList() {
		return R.ok(mdShiftModelService.list());
	}

	/**
	 * 通过ID查询详情（含班段）
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情（含班段）", summary = "通过ID查询详情（含班段）")
	@GetMapping("/details/{id}")
	@HasPermission("mes_shift_view")
	public R<ShiftModelDTO> getById(@PathVariable("id") Long id) {
		return R.ok(mdShiftModelService.getDetail(id));
	}

	/**
	 * 新增班次模型（含班段）
	 * @param dto 班次模型
	 * @return R
	 */
	@Operation(description = "新增班次模型", summary = "新增班次模型")
	@SysLog("新增班次模型")
	@PostMapping
	@HasPermission("mes_shift_add")
	public R<Boolean> save(@Valid @RequestBody ShiftModelDTO dto) {
		mdShiftModelService.saveWithSegments(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 修改班次模型（全量替换班段）
	 * @param dto 班次模型
	 * @return R
	 */
	@Operation(description = "修改班次模型", summary = "修改班次模型")
	@SysLog("修改班次模型")
	@PutMapping
	@HasPermission("mes_shift_edit")
	public R<Boolean> updateById(@Valid @RequestBody ShiftModelDTO dto) {
		mdShiftModelService.updateWithSegments(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 通过ID批量删除班次模型
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除班次模型", summary = "批量删除班次模型")
	@SysLog("删除班次模型")
	@DeleteMapping
	@HasPermission("mes_shift_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdShiftModelService.removeWithSegments(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
