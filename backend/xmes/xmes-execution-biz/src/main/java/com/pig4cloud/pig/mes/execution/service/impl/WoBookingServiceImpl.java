package com.pig4cloud.pig.mes.execution.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.common.security.util.SecurityUtils;
import com.pig4cloud.pig.mes.execution.api.dto.BookingQtyDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoBooking;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.execution.mapper.WoBookingMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoOrderMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoTaskMapper;
import com.pig4cloud.pig.mes.execution.service.WoBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报工服务实现。
 * <p>
 * 状态与数量校验在事务内基于最新行数据执行；高并发多终端场景下需要
 * 对作业行加悲观锁（select ... for update）或乐观版本号，列入终端切片的加固项。
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class WoBookingServiceImpl extends ServiceImpl<WoBookingMapper, WoBooking> implements WoBookingService {

	/** 超产容差（首道作业累计数量 ≤ 计划数 × 容差），后续做成配置项 */
	private static final BigDecimal OVER_PRODUCE_TOLERANCE = BigDecimal.ONE;

	private final WoTaskMapper taskMapper;

	private final WoOrderMapper orderMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void start(Long taskId, Long workplaceId, String source) {
		WoTask task = mustGetTask(taskId);
		WoOrder order = mustGetOrder(task.getOrderId());
		Assert.isTrue("RELEASED".equals(order.getOrderStatus()) || "IN_PROGRESS".equals(order.getOrderStatus()),
				StrUtil.format("工单 {} 当前状态不允许开工（须已下达且未挂起）", order.getOrderNo()));
		Assert.isTrue(List.of("PENDING", "SCHEDULED", "DISPATCHED").contains(task.getTaskStatus()),
				"作业当前状态不允许开工");

		// 工序串行约束（R5-2）：上道完工，或满足转移批量（上道良品 ≥ 转移批量）
		WoTask previous = previousTask(task);
		if (previous != null) {
			boolean prevFinished = "COMPLETED".equals(previous.getTaskStatus());
			boolean transferReady = task.getTransferQty() != null && task.getTransferQty().signum() > 0
					&& previous.getQtyGood().compareTo(task.getTransferQty()) >= 0;
			Assert.isTrue(prevFinished || transferReady,
					StrUtil.format("上道工序 {} 未完工（转移批量条件也未满足），不允许开工", previous.getOperationNo()));
		}

		task.setTaskStatus("RUNNING");
		if (workplaceId != null) {
			task.setWorkplaceId(workplaceId);
		}
		taskMapper.updateById(task);

		// 首个开工事件驱动工单进入执行中
		if ("RELEASED".equals(order.getOrderStatus())) {
			order.setOrderStatus("IN_PROGRESS");
			orderMapper.updateById(order);
		}
		insertEvent(task, "START", null, null, null, null, workplaceId, null, null, source);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void pause(Long taskId, String reasonCode, String source) {
		Assert.hasText(reasonCode, "暂停原因不能为空");
		WoTask task = mustGetTask(taskId);
		Assert.isTrue("RUNNING".equals(task.getTaskStatus()), "仅进行中的作业可暂停");
		task.setTaskStatus("PAUSED");
		taskMapper.updateById(task);
		insertEvent(task, "PAUSE", null, null, null, reasonCode, null, null, null, source);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void resume(Long taskId, String source) {
		WoTask task = mustGetTask(taskId);
		Assert.isTrue("PAUSED".equals(task.getTaskStatus()), "仅暂停状态可恢复");
		task.setTaskStatus("RUNNING");
		taskMapper.updateById(task);
		insertEvent(task, "RESUME", null, null, null, null, null, null, null, source);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void reportQty(BookingQtyDTO dto) {
		WoTask task = mustGetTask(dto.getTaskId());
		Assert.isTrue("RUNNING".equals(task.getTaskStatus()), "仅进行中的作业可报数");
		applyQty(task, dto);
		taskMapper.updateById(task);
		insertEvent(task, "QTY", nz(dto.getQtyGood()), nz(dto.getQtyScrap()), nz(dto.getQtyRework()),
				dto.getReasonCode(), dto.getWorkplaceId(), dto.getRemark(), null, dto.getSource());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void finish(BookingQtyDTO dto) {
		WoTask task = mustGetTask(dto.getTaskId());
		Assert.isTrue("RUNNING".equals(task.getTaskStatus()) || "PAUSED".equals(task.getTaskStatus()),
				"仅进行中/暂停的作业可完工");
		boolean hasQty = nz(dto.getQtyGood()).signum() != 0 || nz(dto.getQtyScrap()).signum() != 0
				|| nz(dto.getQtyRework()).signum() != 0;
		if (hasQty) {
			applyQty(task, dto);
		}
		task.setTaskStatus("COMPLETED");
		taskMapper.updateById(task);
		insertEvent(task, "FINISH", nz(dto.getQtyGood()), nz(dto.getQtyScrap()), nz(dto.getQtyRework()),
				dto.getReasonCode(), dto.getWorkplaceId(), dto.getRemark(), null, dto.getSource());

		// 末道完工：同步工单数量账户，尝试完工工单
		if (isLastTask(task)) {
			WoOrder order = mustGetOrder(task.getOrderId());
			order.setQtyGood(task.getQtyGood());
			order.setQtyScrap(task.getQtyScrap());
			order.setQtyRework(task.getQtyRework());
			boolean reached = task.getQtyGood().add(task.getQtyScrap()).compareTo(order.getQuantity()) >= 0;
			if (reached) {
				order.setOrderStatus("COMPLETED");
			}
			orderMapper.updateById(order);
		}
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void reverse(Long bookingId, String remark) {
		WoBooking origin = this.getById(bookingId);
		Assert.notNull(origin, "报工事件不存在");
		Assert.isTrue("QTY".equals(origin.getBookingType()), "仅数量报工事件可冲销");
		Long reversed = this.baseMapper
			.selectCount(Wrappers.<WoBooking>lambdaQuery().eq(WoBooking::getReverseOfId, bookingId));
		Assert.isTrue(reversed == 0, "该事件已被冲销");

		WoTask task = mustGetTask(origin.getTaskId());
		Assert.isTrue(!"COMPLETED".equals(task.getTaskStatus()), "作业已完工，请先处理完工状态再冲销");

		BigDecimal good = task.getQtyGood().subtract(nz(origin.getQtyGood()));
		BigDecimal scrap = task.getQtyScrap().subtract(nz(origin.getQtyScrap()));
		BigDecimal rework = task.getQtyRework().subtract(nz(origin.getQtyRework()));
		Assert.isTrue(good.signum() >= 0 && scrap.signum() >= 0 && rework.signum() >= 0, "冲销后数量为负，无法冲销");
		task.setQtyGood(good);
		task.setQtyScrap(scrap);
		task.setQtyRework(rework);
		taskMapper.updateById(task);

		insertEvent(task, "REVERSE", nz(origin.getQtyGood()).negate(), nz(origin.getQtyScrap()).negate(),
				nz(origin.getQtyRework()).negate(), origin.getReasonCode(), origin.getWorkplaceId(), remark,
				bookingId, null);
	}

	@Override
	public List<WoBooking> listByTask(Long taskId) {
		return this.list(Wrappers.<WoBooking>lambdaQuery()
			.eq(WoBooking::getTaskId, taskId)
			.orderByAsc(WoBooking::getBookingTime, WoBooking::getId));
	}

	/**
	 * 数量累加与守恒校验（R5-1）：累计 良+废+返 ≤ 上道完工良品；首道 ≤ 工单计划数 × 超产容差
	 */
	private void applyQty(WoTask task, BookingQtyDTO dto) {
		BigDecimal good = nz(dto.getQtyGood());
		BigDecimal scrap = nz(dto.getQtyScrap());
		BigDecimal rework = nz(dto.getQtyRework());
		Assert.isTrue(good.signum() >= 0 && scrap.signum() >= 0 && rework.signum() >= 0, "报工数量不能为负");
		Assert.isTrue(good.add(scrap).add(rework).signum() > 0, "报工数量不能全为 0");
		Assert.isTrue(scrap.signum() == 0 || StrUtil.isNotBlank(dto.getReasonCode()), "报废数量必须填写报废原因");

		BigDecimal newTotal = task.getQtyGood().add(task.getQtyScrap()).add(task.getQtyRework())
				.add(good).add(scrap).add(rework);
		WoTask previous = previousTask(task);
		BigDecimal upperLimit;
		String limitDesc;
		if (previous == null) {
			WoOrder order = mustGetOrder(task.getOrderId());
			upperLimit = order.getQuantity().multiply(OVER_PRODUCE_TOLERANCE);
			limitDesc = "工单计划数";
		}
		else {
			upperLimit = previous.getQtyGood();
			limitDesc = StrUtil.format("上道工序 {} 完工良品数", previous.getOperationNo());
		}
		Assert.isTrue(newTotal.compareTo(upperLimit) <= 0,
				StrUtil.format("数量守恒校验失败：累计报工 {} 超过{} {}", newTotal, limitDesc, upperLimit));

		task.setQtyGood(task.getQtyGood().add(good));
		task.setQtyScrap(task.getQtyScrap().add(scrap));
		task.setQtyRework(task.getQtyRework().add(rework));
	}

	private WoTask previousTask(WoTask task) {
		return taskMapper.selectOne(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getOrderId, task.getOrderId())
			.lt(WoTask::getSortOrder, task.getSortOrder())
			.orderByDesc(WoTask::getSortOrder)
			.last("limit 1"));
	}

	private boolean isLastTask(WoTask task) {
		Long after = taskMapper.selectCount(Wrappers.<WoTask>lambdaQuery()
			.eq(WoTask::getOrderId, task.getOrderId())
			.gt(WoTask::getSortOrder, task.getSortOrder()));
		return after == 0;
	}

	private WoTask mustGetTask(Long taskId) {
		WoTask task = taskMapper.selectById(taskId);
		Assert.notNull(task, "作业不存在");
		return task;
	}

	private WoOrder mustGetOrder(Long orderId) {
		WoOrder order = orderMapper.selectById(orderId);
		Assert.notNull(order, "工单不存在");
		return order;
	}

	private void insertEvent(WoTask task, String type, BigDecimal good, BigDecimal scrap, BigDecimal rework,
			String reasonCode, Long workplaceId, String remark, Long reverseOfId, String source) {
		WoBooking event = new WoBooking();
		event.setTaskId(task.getId());
		event.setOrderId(task.getOrderId());
		event.setBookingType(type);
		event.setQtyGood(good);
		event.setQtyScrap(scrap);
		event.setQtyRework(rework);
		event.setReasonCode(reasonCode);
		event.setWorkplaceId(workplaceId != null ? workplaceId : task.getWorkplaceId());
		event.setPersonName(SecurityUtils.getUser() != null ? SecurityUtils.getUser().getUsername() : null);
		event.setBookingTime(LocalDateTime.now());
		event.setSource(StrUtil.blankToDefault(source, "MANUAL"));
		event.setReverseOfId(reverseOfId);
		event.setRemark(remark);
		this.save(event);
	}

	private BigDecimal nz(BigDecimal v) {
		return v == null ? BigDecimal.ZERO : v;
	}

}
