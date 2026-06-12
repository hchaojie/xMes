package com.pig4cloud.pig.mes.core.controller;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.dto.BomDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdBom;
import com.pig4cloud.pig.mes.core.service.MdBomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

/**
 * BOM管理（版本化主数据：草稿 → 发布 → 冻结）
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/masterdata/bom")
@Tag(description = "bom", name = "BOM管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MdBomController {

	private final MdBomService mdBomService;

	/**
	 * 分页查询（携带物料编码/名称）
	 * @param page 分页对象
	 * @param materialId 物料ID（可空）
	 * @param status 状态（可空）
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_bom_view")
	public R<Page<BomDTO>> getPage(@ParameterObject Page<MdBom> page,
			@RequestParam(value = "materialId", required = false) Long materialId,
			@RequestParam(value = "status", required = false) String status) {
		return R.ok(mdBomService.pageDetail(page, materialId, status));
	}

	/**
	 * 通过ID查询详情（含明细行）
	 * @param id 主键ID
	 * @return 详情
	 */
	@Operation(description = "通过ID查询详情", summary = "通过ID查询详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_bom_view")
	public R<BomDTO> getById(@PathVariable("id") Long id) {
		return R.ok(mdBomService.getDetail(id));
	}

	/**
	 * 新增BOM草稿
	 * @param dto BOM
	 * @return R
	 */
	@Operation(description = "新增BOM草稿", summary = "新增BOM草稿")
	@SysLog("新增BOM")
	@PostMapping
	@HasPermission("mes_bom_add")
	public R<Boolean> save(@Valid @RequestBody BomDTO dto) {
		mdBomService.saveDraft(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 修改BOM草稿
	 * @param dto BOM
	 * @return R
	 */
	@Operation(description = "修改BOM草稿", summary = "修改BOM草稿")
	@SysLog("修改BOM")
	@PutMapping
	@HasPermission("mes_bom_edit")
	public R<Boolean> update(@Valid @RequestBody BomDTO dto) {
		mdBomService.updateDraft(dto);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 发布BOM
	 * @param id 主键ID
	 * @return R
	 */
	@Operation(description = "发布BOM", summary = "发布BOM")
	@SysLog("发布BOM")
	@PutMapping("/release/{id}")
	@HasPermission("mes_bom_release")
	public R<Boolean> release(@PathVariable("id") Long id) {
		mdBomService.release(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 冻结BOM
	 * @param id 主键ID
	 * @return R
	 */
	@Operation(description = "冻结BOM", summary = "冻结BOM")
	@SysLog("冻结BOM")
	@PutMapping("/freeze/{id}")
	@HasPermission("mes_bom_release")
	public R<Boolean> freeze(@PathVariable("id") Long id) {
		mdBomService.freeze(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 复制为新草稿版本
	 * @param id 源版本ID
	 * @return 新版本ID
	 */
	@Operation(description = "复制为新草稿版本", summary = "复制为新草稿版本")
	@SysLog("复制BOM版本")
	@PostMapping("/copy/{id}")
	@HasPermission("mes_bom_add")
	public R<Long> copyVersion(@PathVariable("id") Long id) {
		return R.ok(mdBomService.copyVersion(id));
	}

	/**
	 * 通过ID批量删除BOM（仅草稿）
	 * @param ids 主键ID列表
	 * @return R
	 */
	@Operation(description = "批量删除BOM", summary = "批量删除BOM")
	@SysLog("删除BOM")
	@DeleteMapping
	@HasPermission("mes_bom_del")
	public R<Boolean> removeByIds(@RequestBody Long[] ids) {
		mdBomService.removeDrafts(CollUtil.toList(ids));
		return R.ok(Boolean.TRUE);
	}

}
