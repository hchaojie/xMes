package com.pig4cloud.pig.mes.execution.controller;

import com.pig4cloud.pig.common.core.util.R;
import com.pig4cloud.pig.common.security.annotation.HasPermission;
import com.pig4cloud.pig.mes.execution.api.dto.ScheduleBoardVO;
import com.pig4cloud.pig.mes.execution.service.TerminalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工位终端
 *
 * @author xmes
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/terminal")
@Tag(description = "terminal", name = "工位终端")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class TerminalController {

	private final TerminalService terminalService;

	/**
	 * 工位任务队列
	 * @param workplaceId 工位ID
	 * @return 任务队列（进行中 > 暂停 > 按排程时间）
	 */
	@Operation(description = "工位任务队列", summary = "工位任务队列")
	@GetMapping("/queue/{workplaceId}")
	@HasPermission("mes_terminal_use")
	public R<List<ScheduleBoardVO.TaskBar>> queue(@PathVariable("workplaceId") Long workplaceId) {
		return R.ok(terminalService.queue(workplaceId));
	}

}
