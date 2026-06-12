package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.RoutingDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.service.MdRoutingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * 工作计划管理（版本化主数据：草稿 → 发布 → 冻结）
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/masterdata/routing")
@Tag(description = "routing", name = "工作计划管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdRoutingController {

	private final MdRoutingService mdRoutingService;

	/**
	 * 分页查询（携带物料编码/名称）
	 * @param page 分页对象
	 * @param materialId 物料ID（可空）
	 * @param status 状态（可空）
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_route_view")
	public R<Page<RoutingDTO>> getPage(@ParameterObject Page<MdRouting> page,
			@RequestParam(value = "materialId", required = false) Long materialId,
			@RequestParam(value = "status", required = false) String status) {
		return R.ok(mdRoutingService.pageDetail(page, materialId, status));
	}

	/**
	 * 通过ID查询详情（含明细行）
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_route_view")
	public R<RoutingDTO> getById(@PathVariable("id") Long id) {
		return R.ok(mdRoutingService.getDetail(id));
	}

	/**
	 * 新增工作计划草稿
	 * @param dto 工作计划
	 * @return R
	 */
	@Operation(description = "新增工作计划草稿", summary = "新增工作计划草稿")
	@SysLog("新增工作计划")
	@PostMapping
	@HasPermission("mes_route_add")
	public R<Boolean> save(@Valid @RequestBody RoutingDTO dto) {
		mdRoutingService.saveDraft(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 修改工作计划草稿
	 * @param dto 工作计划
	 * @return R
	 */
	@Operation(description = "修改工作计划草稿", summary = "修改工作计划草稿")
	@SysLog("修改工作计划")
	@PutMapping
	@HasPermission("mes_route_edit")
	public R<Boolean> update(@Valid @RequestBody RoutingDTO dto) {
		mdRoutingService.updateDraft(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 发布工作计划
	 * @param id 主键ID
	 * @return R
	 */
	@Operation(description = "发布工作计划", summary = "发布工作计划")
	@SysLog("发布工作计划")
	@PutMapping("/release/{id}")
	@HasPermission("mes_route_release")
	public R<Boolean> release(@PathVariable("id") Long id) {
		mdRoutingService.release(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 冻结工作计划
	 * @param id 主键ID
	 * @return R
	 */
	@Operation(description = "冻结工作计划", summary = "冻结工作计划")
	@SysLog("冻结工作计划")
	@PutMapping("/freeze/{id}")
	@HasPermission("mes_route_release")
	public R<Boolean> freeze(@PathVariable("id") Long id) {
		mdRoutingService.freeze(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 复制为新草稿版本
	 * @param id 源版本ID
	 * @return 新版本ID
	 */
	@Operation(description = "复制为新草稿版本", summary = "复制为新草稿版本")
	@SysLog("复制工作计划版本")
	@PostMapping("/copy/{id}")
	@HasPermission("mes_route_add")
	public R<Long> copyVersion(@PathVariable("id") Long id) {
		return R.ok(mdRoutingService.copyVersion(id));
	}

	/**
	 * 通过ID批量删除工作计划（仅草稿）
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工作计划", summary = "批量删除工作计划")
	@SysLog("删除工作计划")
	@DeleteMapping
	@HasPermission("mes_route_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdRoutingService.removeDrafts(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
