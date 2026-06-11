package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.core.service.MdWorkplaceService;
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
 * 工位管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/workplace")
@Tag(description = "mdWorkplaceService", name = "工位管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdWorkplaceController {

	private final MdWorkplaceService mdWorkplaceService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_wp_view")
	public R<Page<MdWorkplace>> getPage(@ParameterObject Page<MdWorkplace> page, @ParameterObject MdWorkplace query) {
		return R.ok(mdWorkplaceService.page(page, Wrappers.<MdWorkplace>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getWorkplaceName()), MdWorkplace::getWorkplaceName, query.getWorkplaceName())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdWorkplace::getStatus, query.getStatus())
			.orderByAsc(MdWorkplace::getSortOrder)));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_wp_view")
	public R<List<MdWorkplace>> getList(MdWorkplace query) {
		return R.ok(mdWorkplaceService.list(Wrappers.query(query).orderByAsc("sort_order")));
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_wp_view")
	public R<MdWorkplace> getById(@PathVariable("id") Long id) {
		return R.ok(mdWorkplaceService.getById(id));
	}

	/**
	 * 新增工位
	 * @param entity 工位
	 * @return R
	 */
	@Operation(description = "新增工位", summary = "新增工位")
	@SysLog("新增工位")
	@PostMapping
	@HasPermission("mes_wp_add")
	public R<Boolean> save(@Valid @RequestBody MdWorkplace entity) {
		return R.ok(mdWorkplaceService.save(entity));
	}

	/**
	 * 修改工位
	 * @param entity 工位
	 * @return R
	 */
	@Operation(description = "修改工位", summary = "修改工位")
	@SysLog("修改工位")
	@PutMapping
	@HasPermission("mes_wp_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdWorkplace entity) {
		return R.ok(mdWorkplaceService.updateById(entity));
	}

	/**
	 * 通过ID批量删除工位
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工位", summary = "批量删除工位")
	@SysLog("删除工位")
	@DeleteMapping
	@HasPermission("mes_wp_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdWorkplaceService.removeBatchByIds(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
