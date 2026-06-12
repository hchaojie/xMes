package com.pig4cloud.pig.mes.core.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.core.api.entity.IntDeviceEvent;
import com.pig4cloud.pig.mes.core.mapper.IntDeviceEventMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 设备事件接入（FR-INT-20：后期物联网模块的对接契约）。
 * <p>
 * 事件信封：eventId(幂等) / equipmentCode / eventType / 值 / sourceTime。
 * 容忍乱序与重复推送：按 eventId 去重，重复返回 duplicated 计数。
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/modeling/device")
@Tag(description = "deviceEvent", name = "设备事件接入")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class DeviceEventController {

	private final IntDeviceEventMapper eventMapper;

	/**
	 * 批量接收设备事件（幂等：eventId 重复跳过）
	 * @param events 事件列表
	 * @return accepted/duplicated 计数
	 */
	@Operation(description = "批量接收设备事件", summary = "批量接收设备事件")
	@PostMapping("/events")
	@HasPermission("mes_integration")
	public R<Map<String, Integer>> receive(@Valid @RequestBody List<IntDeviceEvent> events) {
		int accepted = 0;
		int duplicated = 0;
		for (IntDeviceEvent event : events) {
			Long exists = eventMapper
				.selectCount(Wrappers.<IntDeviceEvent>lambdaQuery().eq(IntDeviceEvent::getEventId, event.getEventId()));
			if (exists > 0) {
				duplicated++;
				continue;
			}
			event.setId(null);
			try {
				eventMapper.insert(event);
				accepted++;
			}
			catch (org.springframework.dao.DuplicateKeyException ex) {
				// 并发重复推送由唯一索引兜底
				duplicated++;
			}
		}
		Map<String, Integer> result = new LinkedHashMap<>();
		result.put("accepted", accepted);
		result.put("duplicated", duplicated);
		return R.ok(result);
	}

}
