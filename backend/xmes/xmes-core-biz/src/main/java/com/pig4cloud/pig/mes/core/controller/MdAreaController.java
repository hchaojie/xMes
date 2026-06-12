package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdArea;
import com.pig4cloud.pig.mes.core.service.MdAreaService;
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
 * 车间管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/area")
@Tag(description = "mdAreaService", name = "车间管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdAreaController {

	private final MdAreaService mdAreaService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_area_view")
	public R<Page<MdArea>> getPage(@ParameterObject Page<MdArea> page, @ParameterObject MdArea query) {
		return R.ok(mdAreaService.page(page, Wrappers.<MdArea>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getAreaName()), MdArea::getAreaName, query.getAreaName())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdArea::getStatus, query.getStatus())
			.orderByAsc(MdArea::getSortOrder)));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_area_view")
	public R<List<MdArea>> getList(MdArea query) {
		return R.ok(mdAreaService.list(Wrappers.query(query).orderByAsc("sort_order")));
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_area_view")
	public R<MdArea> getById(@PathVariable("id") Long id) {
		return R.ok(mdAreaService.getById(id));
	}

	/**
	 * 新增车间
	 * @param entity 车间
	 * @return R
	 */
	@Operation(description = "新增车间", summary = "新增车间")
	@SysLog("新增车间")
	@PostMapping
	@HasPermission("mes_area_add")
	public R<Boolean> save(@Valid @RequestBody MdArea entity) {
		return R.ok(mdAreaService.save(entity));
	}

	/**
	 * 修改车间
	 * @param entity 车间
	 * @return R
	 */
	@Operation(description = "修改车间", summary = "修改车间")
	@SysLog("修改车间")
	@PutMapping
	@HasPermission("mes_area_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdArea entity) {
		return R.ok(mdAreaService.updateById(entity));
	}

	/**
	 * 通过ID批量删除车间
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除车间", summary = "批量删除车间")
	@SysLog("删除车间")
	@DeleteMapping
	@HasPermission("mes_area_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdAreaService.removeAreas(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
