package com.pig4cloud.pig.mes.core.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.pig4cloud.pig.mes.core.api.dto.MntPlanDTO;
import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import com.pig4cloud.pig.mes.core.api.entity.MntOrderItem;
import com.pig4cloud.pig.mes.core.api.entity.MntPlan;
import com.pig4cloud.pig.mes.core.api.entity.MntPlanItem;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.core.mapper.MntOrderItemMapper;
import com.pig4cloud.pig.mes.core.mapper.MntPlanItemMapper;
import com.pig4cloud.pig.mes.core.mapper.MntPlanMapper;
import com.pig4cloud.pig.mes.core.service.MdWorkplaceService;
import com.pig4cloud.pig.mes.core.service.MntOrderService;
import com.pig4cloud.pig.mes.core.service.MntPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 维护计划服务实现
 *
 * @author xmes
 */
@Service
@RequiredArgsConstructor
public class MntPlanServiceImpl extends ServiceImpl<MntPlanMapper, MntPlan> implements MntPlanService {

	private final MntPlanItemMapper planItemMapper;

	private final MntOrderItemMapper orderItemMapper;

	private final MntOrderService mntOrderService;

	private final MdWorkplaceService workplaceService;

	@Override
	public MntPlanDTO getDetail(Long id) {
		MntPlan plan = this.getById(id);
		Assert.notNull(plan, "维护计划不存在");
		MntPlanDTO dto = BeanUtil.copyProperties(plan, MntPlanDTO.class);
		dto.setItems(planItemMapper.selectList(Wrappers.<MntPlanItem>lambdaQuery()
			.eq(MntPlanItem::getPlanId, id)
			.orderByAsc(MntPlanItem::getSortOrder)));
		MdWorkplace wp = workplaceService.getById(plan.getWorkplaceId());
		if (wp != null) {
			dto.setWorkplaceCode(wp.getWorkplaceCode());
			dto.setWorkplaceName(wp.getWorkplaceName());
		}
		return dto;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void saveWithItems(MntPlanDTO dto) {
		Assert.notEmpty(dto.getItems(), "标准作业项不能为空");
		// DAY 周期未填到期日时按周期顺延；RUNTIME/OUTPUT 周期到期日由手工/后期物联网数据维护
		if (dto.getNextDueDate() == null && "DAY".equals(dto.getCycleType()) && dto.getCycleValue() != null) {
			dto.setNextDueDate(LocalDate.now().plusDays(dto.getCycleValue()));
		}
		this.save(dto);
		insertItems(dto.getId(), dto.getItems());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateWithItems(MntPlanDTO dto) {
		Assert.notEmpty(dto.getItems(), "标准作业项不能为空");
		this.updateById(dto);
		planItemMapper.delete(Wrappers.<MntPlanItem>lambdaQuery().eq(MntPlanItem::getPlanId, dto.getId()));
		insertItems(dto.getId(), dto.getItems());
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void removeWithItems(List<Long> ids) {
		Long ref = mntOrderService.count(Wrappers.<MntOrder>lambdaQuery().in(MntOrder::getPlanId, ids));
		Assert.isTrue(ref == 0, "维护计划已被维护工单引用，无法删除（可停用）");
		this.removeBatchByIds(ids);
		planItemMapper.delete(Wrappers.<MntPlanItem>lambdaQuery().in(MntPlanItem::getPlanId, ids));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public int generateDueOrders() {
		LocalDate today = LocalDate.now();
		List<MntPlan> duePlans = this.list(Wrappers.<MntPlan>lambdaQuery()
			.eq(MntPlan::getStatus, "0")
			.isNotNull(MntPlan::getNextDueDate)
			.le(MntPlan::getNextDueDate, today));
		int generated = 0;
		for (MntPlan plan : duePlans) {
			// 同一计划存在未关闭工单时不重复生成
			Long open = mntOrderService.count(Wrappers.<MntOrder>lambdaQuery()
				.eq(MntOrder::getPlanId, plan.getId())
				.in(MntOrder::getOrderStatus, List.of("PENDING", "IN_PROGRESS", "REVIEW")));
			if (open > 0) {
				continue;
			}
			MntOrder order = mntOrderService.createFromPlan(plan);
			// 复制检查表
			List<MntPlanItem> items = planItemMapper.selectList(Wrappers.<MntPlanItem>lambdaQuery()
				.eq(MntPlanItem::getPlanId, plan.getId())
				.orderByAsc(MntPlanItem::getSortOrder));
			for (MntPlanItem pi : items) {
				MntOrderItem oi = new MntOrderItem();
				oi.setOrderId(order.getId());
				oi.setItemNo(pi.getItemNo());
				oi.setItemName(pi.getItemName());
				oi.setStandardDesc(pi.getStandardDesc());
				oi.setMethodDesc(pi.getMethodDesc());
				oi.setSortOrder(pi.getSortOrder());
				orderItemMapper.insert(oi);
			}
			// 滚动下次到期日（DAY 周期；RUNTIME/OUTPUT 周期到期日由手工/后期物联网数据维护）
			plan.setLastOrderDate(today);
			if ("DAY".equals(plan.getCycleType())) {
				plan.setNextDueDate(today.plusDays(plan.getCycleValue()));
			}
			else {
				plan.setNextDueDate(null);
			}
			this.updateById(plan);
			generated++;
		}
		return generated;
	}

	private void insertItems(Long planId, List<MntPlanItem> items) {
		if (CollectionUtils.isEmpty(items)) {
			return;
		}
		int sort = 0;
		for (MntPlanItem item : items) {
			item.setId(null);
			item.setPlanId(planId);
			item.setSortOrder(sort++);
			planItemMapper.insert(item);
		}
	}

}
