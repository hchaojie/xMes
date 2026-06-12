package com.pig4cloud.pig.mes.quality.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.quality.api.dto.QcInspectionDetailDTO;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionResult;
import com.pig4cloud.pig.mes.quality.service.QcInspectionService;
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
 * 检验工作台
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/quality/inspection")
@Tag(description = "qcInspection", name = "检验工作台")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class QcInspectionController {

	private final QcInspectionService qcInspectionService;

	/**
	 * 扫描质量门完工作业生成检验单
	 * @return 新生成数量
	 */
	@Operation(description = "刷新质量门检验单", summary = "刷新质量门检验单")
	@SysLog("刷新质量门检验单")
	@PostMapping("/refresh-gate")
	@HasPermission("mes_qc_inspect")
	public R<Integer> refreshGate() {
		return R.ok(qcInspectionService.refreshGateOrders());
	}

	/**
	 * 分页查询检验单
	 * @param page 分页对象
	 * @param status 状态过滤
	 * @return 分页结果
	 */
	@Operation(description = "分页查询检验单", summary = "分页查询检验单")
	@GetMapping("/page")
	@HasPermission("mes_qc_view")
	public R<Page<QcInspectionOrder>> getPage(@ParameterObject Page<QcInspectionOrder> page,
			@RequestParam(value = "status", required = false) String status) {
		return R.ok(qcInspectionService.pageOrders(page, status));
	}

	/**
	 * 检验单详情（计划特性 + 已录结果）
	 * @param id 检验单ID
	 * @return 详情
	 */
	@Operation(description = "检验单详情", summary = "检验单详情")
	@GetMapping("/details/{id}")
	@HasPermission("mes_qc_view")
	public R<QcInspectionDetailDTO> getById(@PathVariable("id") Long id) {
		return R.ok(qcInspectionService.getDetail(id));
	}

	/**
	 * 领取检验单
	 * @param id 检验单ID
	 * @return R
	 */
	@Operation(description = "领取检验单", summary = "领取检验单")
	@SysLog("领取检验单")
	@PostMapping("/receive/{id}")
	@HasPermission("mes_qc_inspect")
	public R<Boolean> receive(@PathVariable("id") Long id) {
		qcInspectionService.receive(id);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 录入检验结果
	 * @param results 结果行列表
	 * @return R
	 */
	@Operation(description = "录入检验结果", summary = "录入检验结果")
	@SysLog("录入检验结果")
	@PostMapping("/record")
	@HasPermission("mes_qc_inspect")
	public R<Boolean> record(@Valid @RequestBody List<QcInspectionResult> results) {
		qcInspectionService.record(results);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 用途判定
	 * @param id 检验单ID
	 * @param judge PASSED/CONCESSION/REJECTED
	 * @param judgeRemark 判定说明
	 * @return R
	 */
	@Operation(description = "用途判定", summary = "用途判定")
	@SysLog("检验判定")
	@PostMapping("/judge/{id}")
	@HasPermission("mes_qc_judge")
	public R<Boolean> judge(@PathVariable("id") Long id, @RequestParam("judge") String judge,
			@RequestParam(value = "judgeRemark", required = false) String judgeRemark) {
		qcInspectionService.judge(id, judge, judgeRemark);
		return R.ok(Boolean.TRUE);
	}

}
