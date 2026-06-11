package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdSite;
import com.pig4cloud.pig.mes.core.service.MdSiteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import com.pig4cloud.pig.mes.core.api.vo.FactoryTreeNode;
import com.pig4cloud.pig.mes.core.service.FactoryTreeService;

import java.util.List;

/**
 * 工厂管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/site")
@Tag(description = "mdSiteService", name = "工厂管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdSiteController {

	private final MdSiteService mdSiteService;

	private final FactoryTreeService factoryTreeService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_site_view")
	public R<Page<MdSite>> getPage(@ParameterObject Page<MdSite> page, @ParameterObject MdSite query) {
		return R.ok(mdSiteService.page(page, Wrappers.<MdSite>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getSiteName()), MdSite::getSiteName, query.getSiteName())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdSite::getStatus, query.getStatus())
			.orderByAsc(MdSite::getSortOrder)));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_site_view")
	public R<List<MdSite>> getList(MdSite query) {
		return R.ok(mdSiteService.list(Wrappers.query(query).orderByAsc("sort_order")));
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_site_view")
	public R<MdSite> getById(@PathVariable("id") Long id) {
		return R.ok(mdSiteService.getById(id));
	}

	/**
	 * 新增工厂
	 * @param entity 工厂
	 * @return R
	 */
	@Operation(description = "新增工厂", summary = "新增工厂")
	@SysLog("新增工厂")
	@PostMapping
	@HasPermission("mes_site_add")
	public R<Boolean> save(@Valid @RequestBody MdSite entity) {
		return R.ok(mdSiteService.save(entity));
	}

	/**
	 * 修改工厂
	 * @param entity 工厂
	 * @return R
	 */
	@Operation(description = "修改工厂", summary = "修改工厂")
	@SysLog("修改工厂")
	@PutMapping
	@HasPermission("mes_site_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdSite entity) {
		return R.ok(mdSiteService.updateById(entity));
	}

	/**
	 * 通过ID批量删除工厂
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工厂", summary = "批量删除工厂")
	@SysLog("删除工厂")
	@DeleteMapping
	@HasPermission("mes_site_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdSiteService.removeSites(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 查询工厂结构树（工厂→车间→工作中心→工位）
	 * @return 树形结构
	 */
	@Operation(description = "查询工厂结构树", summary = "查询工厂结构树")
	@GetMapping("/tree")
	@HasPermission("mes_site_view")
	public R<List<FactoryTreeNode>> getTree() {
		return R.ok(factoryTreeService.buildTree());
	}

}
