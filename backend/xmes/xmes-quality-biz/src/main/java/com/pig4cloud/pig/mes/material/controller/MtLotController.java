package com.pig4cloud.pig.mes.material.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.material.api.entity.MtLot;
import com.pig4cloud.pig.mes.material.api.entity.MtLotConsume;
import com.pig4cloud.pig.mes.material.service.MtLotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批次台账与上料
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/material/lot")
@Tag(description = "lot", name = "批次台账")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class MtLotController {

	private final MtLotService mtLotService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param query 查询条件
	 * @return 分页结果
	 */
	@Operation(description = "分页查询", summary = "分页查询")
	@GetMapping("/page")
	@HasPermission("mes_lot_view")
	public R<Page<MtLot>> getPage(@ParameterObject Page<MtLot> page, @ParameterObject MtLot query) {
		return R.ok(mtLotService.page(page, Wrappers.<MtLot>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getLotNo()), MtLot::getLotNo, query.getLotNo())
			.like(StrUtil.isNotBlank(query.getMaterialCode()), MtLot::getMaterialCode, query.getMaterialCode())
			.eq(StrUtil.isNotBlank(query.getLotStatus()), MtLot::getLotStatus, query.getLotStatus())
			.eq(StrUtil.isNotBlank(query.getSourceType()), MtLot::getSourceType, query.getSourceType())
			.orderByDesc(MtLot::getCreateTime)));
	}

	/**
	 * 收货/手工登记批次
	 * @param lot 批次
	 * @return 批次ID
	 */
	@Operation(description = "登记批次", summary = "登记批次")
	@SysLog("登记批次")
	@PostMapping
	@HasPermission("mes_lot_add")
	public R<Long> register(@Valid @RequestBody MtLot lot) {
		return R.ok(mtLotService.registerLot(lot));
	}

	/**
	 * 批次状态变更
	 * @param id 批次ID
	 * @param status AVAILABLE/HOLD/FROZEN
	 * @return R
	 */
	@Operation(description = "批次状态变更", summary = "批次状态变更")
	@SysLog("批次状态变更")
	@PutMapping("/status/{id}")
	@HasPermission("mes_lot_status")
	public R<Boolean> changeStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
		mtLotService.changeStatus(id, status);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 作业上料
	 * @param taskId 作业ID
	 * @param lotNo 批次号
	 * @param qty 数量
	 * @return R
	 */
	@Operation(description = "作业上料", summary = "作业上料")
	@SysLog("作业上料")
	@PostMapping("/feed")
	@HasPermission("mes_lot_feed")
	public R<Boolean> feed(@RequestParam("taskId") Long taskId, @RequestParam("lotNo") String lotNo,
			@RequestParam("qty") BigDecimal qty) {
		mtLotService.feed(taskId, lotNo, qty);
		return R.ok(Boolean.TRUE);
	}

	/**
	 * 查询作业上料记录
	 * @param taskId 作业ID
	 * @return 消耗记录
	 */
	@Operation(description = "查询作业上料记录", summary = "查询作业上料记录")
	@GetMapping("/consumes/{taskId}")
	@HasPermission("mes_lot_view")
	public R<List<MtLotConsume>> listConsumes(@PathVariable("taskId") Long taskId) {
		return R.ok(mtLotService.listConsumes(taskId));
	}

}
