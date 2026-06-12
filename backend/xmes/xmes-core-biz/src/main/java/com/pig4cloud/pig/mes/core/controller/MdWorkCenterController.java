package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.service.MdWorkCenterService;
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
 * 工作中心管理
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/work-center")
@Tag(description = "mdWorkCenterService", name = "工作中心管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdWorkCenterController {

	private final MdWorkCenterService mdWorkCenterService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_wc_view")
	public R<Page<MdWorkCenter>> getPage(@ParameterObject Page<MdWorkCenter> page, @ParameterObject MdWorkCenter query) {
		return R.ok(mdWorkCenterService.page(page, Wrappers.<MdWorkCenter>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getCenterName()), MdWorkCenter::getCenterName, query.getCenterName())
			.eq(StrUtil.isNotBlank(query.getStatus()), MdWorkCenter::getStatus, query.getStatus())
			.orderByAsc(MdWorkCenter::getSortOrder)));
	}

	/**
	 * 查询全部列表
	 * @return 列表
	 */
	@Operation(description = "查询全部列表", summary = "查询全部列表")
	@GetMapping("/list")
	@HasPermission("mes_wc_view")
	public R<List<MdWorkCenter>> getList(MdWorkCenter query) {
		return R.ok(mdWorkCenterService.list(Wrappers.query(query).orderByAsc("sort_order")));
	}

	/**
	 * 通过ID查询详情
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_wc_view")
	public R<MdWorkCenter> getById(@PathVariable("id") Long id) {
		return R.ok(mdWorkCenterService.getById(id));
	}

	/**
	 * 新增工作中心
	 * @param entity 工作中心
	 * @return R
	 */
	@Operation(description = "新增工作中心", summary = "新增工作中心")
	@SysLog("新增工作中心")
	@PostMapping
	@HasPermission("mes_wc_add")
	public R<Boolean> save(@Valid @RequestBody MdWorkCenter entity) {
		return R.ok(mdWorkCenterService.save(entity));
	}

	/**
	 * 修改工作中心
	 * @param entity 工作中心
	 * @return R
	 */
	@Operation(description = "修改工作中心", summary = "修改工作中心")
	@SysLog("修改工作中心")
	@PutMapping
	@HasPermission("mes_wc_edit")
	public R<Boolean> updateById(@Valid @RequestBody MdWorkCenter entity) {
		return R.ok(mdWorkCenterService.updateById(entity));
	}

	/**
	 * 通过ID批量删除工作中心
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除工作中心", summary = "批量删除工作中心")
	@SysLog("删除工作中心")
	@DeleteMapping
	@HasPermission("mes_wc_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdWorkCenterService.removeWorkCenters(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
