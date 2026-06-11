package com.pig4cloud.pig.mes.execution.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.api.entity.MdRoutingOperation;
import com.pig4cloud.pig.mes.execution.api.dto.WoOrderDTO;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import com.pig4cloud.pig.mes.execution.mapper.MdReadMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoOrderMapper;
import com.pig4cloud.pig.mes.execution.mapper.WoTaskMapper;
import com.pig4cloud.pig.mes.execution.service.WoOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 工单服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class WoOrderServiceImpl extends ServiceImpl<WoOrderMapper, WoOrder> implements WoOrderService {

	private static final String CREATED = "CREATED";

	private static final String RELEASED = "RELEASED";

	private static final String IN_PROGRESS = "IN_PROGRESS";

	private static final String HOLD = "HOLD";

	private static final String CANCELLED = "CANCELLED";

	private final WoTaskMapper taskMapper;

	private final MdReadMapper.MaterialReadMapper materialReadMapper;

	private final MdReadMapper.RoutingReadMapper routingReadMapper;

	private final MdReadMapper.RoutingOperationReadMapper operationReadMapper;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Long createFromPlan(WoOrderDTO dto) {
		MdMaterial material = materialReadMapper.selectById(dto.getMaterialId());
		Assert.notNull(material, "物料不存在");
		Assert.isTrue(dto.getQuantity() != null && dto.getQuantity().signum() > 0, "计划数量必须大于 0");

		// 工艺快照来源：指定版本或该物料当前已发布版本
		MdRouting routing = dto.getRoutingId() != null ? routingReadMapper.selectById(dto.getRoutingId())
				: routingReadMapper.selectOne(Wrappers.<MdRouting>lambdaQuery()
					.eq(MdRouting::getMaterialId, dto.getMaterialId())
					.eq(MdRouting::getRoutingStatus, "RELEASED")
					.last("limit 1"));
		Assert.notNull(routing, "该物料没有已发布的工作计划，无法生成工单");
		List<MdRoutingOperation> operations = operationReadMapper
			.selectList(Wrappers.<MdRoutingOperation>lambdaQuery()
				.eq(MdRoutingOperation::getRoutingId, routing.getId())
				.orderByAsc(MdRoutingOperation::getSortOrder));
		Assert.notEmpty(operations, "工作计划没有工序，无法生成工单");

		WoOrder order = BeanUtil.copyProperties(dto, WoOrder.class);
		order.setId(null);
		if (StrUtil.isBlank(order.getOrderNo())) {
			order.setOrderNo(nextOrderNo());
		}
		order.setOrderType(StrUtil.blankToDefault(order.getOrderType(), "PRODUCTION"));
		order.setSourceType(StrUtil.blankToDefault(order.getSourceType(), "PLAN"));
		order.setUnit(material.getUnit());
		order.setOrderStatus(CREATED);
		order.setRoutingId(routing.getId());
		order.setRoutingVersion(routing.getVersion());
		order.setQtyGood(BigDecimal.ZERO);
		order.setQtyScrap(BigDecimal.ZERO);
		order.setQtyRework(BigDecimal.ZERO);
		this.save(order);

		// 工序快照 → 作业
		int sort = 0;
		for (MdRoutingOperation op : operations) {
			WoTask task = new WoTask();
			task.setOrderId(order.getId());
			task.setOperationNo(op.getOperationNo());
			task.setOperationName(op.getOperationName());
			task.setOperationType(op.getOperationType());
			task.setTaskStatus("PENDING");
			task.setWorkCenterId(op.getWorkCenterId());
			task.setQtyTarget(order.getQuantity());
			task.setQtyGood(BigDecimal.ZERO);
			task.setQtyScrap(BigDecimal.ZERO);
			task.setQtyRework(BigDecimal.ZERO);
			task.setSetupTime(op.getSetupTime());
			task.setUnitTime(op.getUnitTime());
			task.setWaitTime(op.getWaitTime());
			task.setReportMode(op.getReportMode());
			task.setQualityGate(op.getQualityGate());
			task.setTransferQty(op.getTransferQty());
			task.setSplitSeq(0);
			task.setSortOrder(sort++);
			taskMapper.insert(task);
		}
		return order.getId();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void release(List<Long> ids) {
		List<WoOrder> orders = this.listByIds(ids);
		Assert.isTrue(orders.size() == ids.size(), "存在无效的工单ID");
		for (WoOrder order : orders) {
			Assert.isTrue(CREATED.equals(order.getOrderStatus()),
					StrUtil.format("工单 {} 非已创建状态，无法下达", order.getOrderNo()));
			order.setOrderStatus(RELEASED);
		}
		this.updateBatchById(orders);
	}

	@Override
	public void hold(Long id, String reason) {
		Assert.hasText(reason, "挂起原因不能为空");
		WoOrder order = this.getById(id);
		Assert.notNull(order, "工单不存在");
		Assert.isTrue(RELEASED.equals(order.getOrderStatus()) || IN_PROGRESS.equals(order.getOrderStatus()),
				"仅已下达/执行中的工单可挂起");
		order.setOrderStatus(HOLD);
		order.setHoldReason(reason);
		this.updateById(order);
	}

	@Override
	public void resume(Long id) {
		WoOrder order = this.getById(id);
		Assert.notNull(order, "工单不存在");
		Assert.isTrue(HOLD.equals(order.getOrderStatus()), "仅挂起状态可恢复");
		order.setOrderStatus(RELEASED);
		order.setHoldReason(null);
		this.updateById(order);
	}

	@Override
	public void cancel(Long id) {
		WoOrder order = this.getById(id);
		Assert.notNull(order, "工单不存在");
		Assert.isTrue(CREATED.equals(order.getOrderStatus()) || RELEASED.equals(order.getOrderStatus()),
				"仅已创建/已下达状态可取消");
		boolean noBooking = order.getQtyGood().signum() == 0 && order.getQtyScrap().signum() == 0
				&& order.getQtyRework().signum() == 0;
		Assert.isTrue(noBooking, "工单已有报工数量，请先冲销后再取消");
		order.setOrderStatus(CANCELLED);
		this.updateById(order);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeCreated(List<Long> ids) {
		Long nonCreated = this.baseMapper.selectCount(
				Wrappers.<WoOrder>lambdaQuery().in(WoOrder::getId, ids).ne(WoOrder::getOrderStatus, CREATED));
		Assert.isTrue(nonCreated == 0, "仅已创建状态的工单可删除");
		this.removeBatchByIds(ids);
		taskMapper.delete(Wrappers.<WoTask>lambdaQuery().in(WoTask::getOrderId, ids));
	}

	@Override
	public Page<WoOrderDTO> pageDetail(Page<WoOrder> page, WoOrder query) {
		Page<WoOrder> result = this.page(page, Wrappers.<WoOrder>lambdaQuery()
			.like(StrUtil.isNotBlank(query.getOrderNo()), WoOrder::getOrderNo, query.getOrderNo())
			.eq(StrUtil.isNotBlank(query.getOrderStatus()), WoOrder::getOrderStatus, query.getOrderStatus())
			.eq(query.getMaterialId() != null, WoOrder::getMaterialId, query.getMaterialId())
			.orderByDesc(WoOrder::getCreateTime));

		List<Long> materialIds = result.getRecords()
			.stream()
			.map(WoOrder::getMaterialId)
			.filter(Objects::nonNull)
			.distinct()
			.collect(Collectors.toList());
		Map<Long, MdMaterial> materialMap = materialIds.isEmpty() ? Map.of()
				: materialReadMapper.selectBatchIds(materialIds)
					.stream()
					.collect(Collectors.toMap(MdMaterial::getId, Function.identity()));

		Page<WoOrderDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
		dtoPage.setRecords(result.getRecords().stream().map(order -> {
			WoOrderDTO dto = BeanUtil.copyProperties(order, WoOrderDTO.class);
			MdMaterial material = materialMap.get(order.getMaterialId());
			if (material != null) {
				dto.setMaterialCode(material.getMaterialCode());
				dto.setMaterialName(material.getMaterialName());
			}
			return dto;
		}).collect(Collectors.toList()));
		return dtoPage;
	}

	@Override
	public WoOrderDTO getDetail(Long id) {
		WoOrder order = this.getById(id);
		Assert.notNull(order, "工单不存在");
		WoOrderDTO dto = BeanUtil.copyProperties(order, WoOrderDTO.class);
		dto.setTasks(taskMapper.selectList(
				Wrappers.<WoTask>lambdaQuery().eq(WoTask::getOrderId, id).orderByAsc(WoTask::getSortOrder)));
		MdMaterial material = materialReadMapper.selectById(order.getMaterialId());
		if (material != null) {
			dto.setMaterialCode(material.getMaterialCode());
			dto.setMaterialName(material.getMaterialName());
		}
		return dto;
	}

	/**
	 * 工单号规则：WO + yyyyMMdd + 4 位当日序列（唯一索引兜底并发冲突）
	 */
	private String nextOrderNo() {
		String prefix = "WO" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
		WoOrder last = this.getOne(Wrappers.<WoOrder>lambdaQuery()
			.likeRight(WoOrder::getOrderNo, prefix)
			.orderByDesc(WoOrder::getOrderNo)
			.last("limit 1"), false);
		int seq = 1;
		if (last != null) {
			seq = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
		}
		return prefix + String.format("%04d", seq);
	}

}
