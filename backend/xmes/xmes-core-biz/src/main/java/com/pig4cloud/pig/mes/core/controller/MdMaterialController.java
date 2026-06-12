package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.service.MdMaterialService;
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
 * 物料管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/masterdata/material")
@Tag(description = "material", name = "物料管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdMaterialController {

	private final MdMaterialService mdMaterialService;

	/**
	 * 分页查询（编码/名称模糊，类型/批次策略精确）
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_mat_view")
	public R<Page<MdMaterial>> getPage(@ParameterObject Page<MdMaterial> page, @ParameterObject MdMaterial query) {
		return R.ok(mdMaterialService.page(page, Wrappers.<MdMaterial>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getMaterialCode()), MdMaterial::getMaterialCode, query.getMaterialCode())
			.like(StrUtil.isNotBlank(query.getMaterialName()), MdMaterial::getMaterialName, query.getMaterialName())
			.eq(StrUtil.isNotBlank(query.getMaterialType()), MdMaterial::getMaterialType, query.getMaterialType())
			.eq(StrUtil.isNotBlank(query.getLotStrategy()), MdMaterial::getLotStrategy, query.getLotStrategy())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdMaterial::getStatus, query.getStatus())
			.orderByDesc(MdMaterial::getCreateTime)));
	}

	/**
	 * 查询列表（编码/名称模糊，供选择器使用）
	 * @param keyword 关键字
	 * @return 列表
	 */
	@Operation(description = "查询列表", summary = "查询列表")
	@GetMapping("/list")
	@HasPermission("mes_mat_view")
	public R<List<MdMaterial>> getList(@RequestParam(value = "keyword", required = false) String keyword) {
		return R.ok(mdMaterialService.list(Wrappers.<MdMaterial>lambdaQuery()
			.and(StrUtil.isNotBlank(keyword),
					w -> w.like(MdMaterial::getMaterialCode, keyword).or().like(MdMaterial::getMaterialName, keyword))
			.eq(MdMaterial::getStatus, "0")
			.last("limit 50")));
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_mat_view")
	public R<MdMaterial> getById(@PathVariable("id") Long id) {
		return R.ok(mdMaterialService.getById(id));
	}

	/**
	 * 新增物料
	 * @param material 物料
	 * @return R
	 */
	@Operation(description = "新增物料", summary = "新增物料")
	@SysLog("新增物料")
	@PostMapping
	@HasPermission("mes_mat_add")
	public R<Boolean> save(@Valid @RequestBody MdMaterial material) {
		return R.ok(mdMaterialService.save(material));
	}

	/**
	 * 修改物料
	 * @param material 物料
	 * @return R
	 */
	@Operation(description = "修改物料", summary = "修改物料")
	@SysLog("修改物料")
	@PutMapping
	@HasPermission("mes_mat_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdMaterial material) {
		return R.ok(mdMaterialService.updateById(material));
	}

	/**
	 * 通过ID批量删除物料
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除物料", summary = "批量删除物料")
	@SysLog("删除物料")
	@DeleteMapping
	@HasPermission("mes_mat_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdMaterialService.removeMaterials(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
