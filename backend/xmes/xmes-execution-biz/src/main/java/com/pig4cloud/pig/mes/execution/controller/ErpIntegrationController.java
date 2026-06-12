package com.pig4cloud.pig.mes.execution.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.log.annotation.SysLog;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.dto.ErpWorkOrderDTO;
import com.pig4cloud.pig.mes.execution.api.dto.WoOrderDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.mapper.MdReadMapper;
import com.pig4cloud.pig.mes.execution.service.WoOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ERP 集成（FR-INT-12/13 契约实现）。
 * <p>
 * 鉴权：生产环境应为 ERP 配置专用集成账号/OAuth2 客户端并仅授予 mes_integration 权限。
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/erp")
@Tag(description = "erp", name = "ERP 集成")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ErpIntegrationController {

	private final WoOrderService woOrderService;

	private final MdReadMapper.MaterialReadMapper materialReadMapper;

	/**
	 * 接收 ERP 工单（按 erpOrderNo 幂等：重复推送返回已有工单号）
	 * @param dto ERP 工单报文
	 * @return MES 工单号
	 */
	@Operation(description = "接收 ERP 工单", summary = "接收 ERP 工单")
	@SysLog("接收 ERP 工单")
	@PostMapping("/work-order")
	@HasPermission("mes_integration")
	public R<String> receiveWorkOrder(@Valid @RequestBody ErpWorkOrderDTO dto) {
		// 幂等
		WoOrder existing = woOrderService
			.getOne(Wrappers.<WoOrder>lambdaQuery().eq(WoOrder::getErpOrderNo, dto.getErpOrderNo()), false);
		if (existing != null) {
			return R.ok(existing.getOrderNo(), "重复推送，返回已有工单");
		}
		MdMaterial material = materialReadMapper
			.selectOne(Wrappers.<MdMaterial>lambdaQuery().eq(MdMaterial::getMaterialCode, dto.getMaterialCode()), false);
		Assert.notNull(material, "物料编码不存在：" + dto.getMaterialCode());

		WoOrderDTO order = BeanUtil.copyProperties(dto, WoOrderDTO.class);
		order.setMaterialId(material.getId());
		order.setSourceType("ERP");
		Long id = woOrderService.createFromPlan(order);
		return R.ok(woOrderService.getById(id).getOrderNo());
	}

	/**
	 * 完工回传（拉取式）：ERP 按 since 增量拉取已完工/已关闭工单的数量结果
	 * @param since 起始时间（取工单最后更新时间）
	 * @return 完工回传列表
	 */
	@Operation(description = "完工回传查询", summary = "完工回传查询")
	@GetMapping("/completions")
	@HasPermission("mes_integration")
	public R<List<Map<String, Object>>> completions(
			@RequestParam("since") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
		List<WoOrder> orders = woOrderService.list(Wrappers.<WoOrder>lambdaQuery()
			.in(WoOrder::getOrderStatus, List.of("COMPLETED", "CLOSED"))
			.ge(WoOrder::getUpdateTime, since));
		return R.ok(orders.stream().map(o -> Map.<String, Object>of(
				"orderNo", o.getOrderNo(),
				"erpOrderNo", o.getErpOrderNo() == null ? "" : o.getErpOrderNo(),
				"materialId", o.getMaterialId(),
				"qtyGood", o.getQtyGood(),
				"qtyScrap", o.getQtyScrap(),
				"qtyRework", o.getQtyRework(),
				"status", o.getOrderStatus(),
				"completedTime", String.valueOf(o.getUpdateTime()))).collect(Collectors.toList()));
	}

}
