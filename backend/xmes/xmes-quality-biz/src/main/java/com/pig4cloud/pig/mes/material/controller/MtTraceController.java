package com.pig4cloud.pig.mes.material.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.material.service.MtTraceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 产出与追溯
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/material/trace")
@Tag(description = "trace", name = "产出与追溯")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MtTraceController {

	private final MtTraceService mtTraceService;

	/**
	 * 扫描完工工单生成产出（批次/序列件 + 谱系）
	 * @return 新生成产出的工单数
	 */
	@Operation(description = "刷新产出", summary = "刷新产出")
	@SysLog("刷新产出")
	@PostMapping("/refresh-output")
	@HasPermission("mes_lot_add")
	public R<Integer> refreshOutputs() {
		return R.ok(mtTraceService.refreshOutputs());
	}

	/**
	 * 反向追溯（产出批次号/序列号 → 工单/消耗批次/检验）
	 * @param no 批次号或序列号
	 * @return 追溯结果
	 */
	@Operation(description = "反向追溯", summary = "反向追溯")
	@GetMapping("/backward")
	@HasPermission("mes_trace_view")
	public R<Map<String, Object>> backward(@RequestParam("no") String no) {
		return R.ok(mtTraceService.traceBackward(no));
	}

	/**
	 * 正向追溯（原料批次号 → 产出批次/序列件）
	 * @param lotNo 原料批次号
	 * @return 追溯结果
	 */
	@Operation(description = "正向追溯", summary = "正向追溯")
	@GetMapping("/forward")
	@HasPermission("mes_trace_view")
	public R<Map<String, Object>> forward(@RequestParam("lotNo") String lotNo) {
		return R.ok(mtTraceService.traceForward(lotNo));
	}

}
