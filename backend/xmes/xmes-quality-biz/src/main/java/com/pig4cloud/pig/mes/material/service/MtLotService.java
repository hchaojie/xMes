package com.pig4cloud.pig.mes.material.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.material.api.entity.MtLot;
import com.pig4cloud.pig.mes.material.api.entity.MtLotConsume;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批次台账与上料服务
 *
 * @author xmes
 */
public interface MtLotService extends IService<MtLot> {

	/**
	 * 收货/手工登记原料批次（批次号空则按规则生成）
	 * @param lot 批次
	 * @return 批次ID
	 */
	Long registerLot(MtLot lot);

	/**
	 * 批次状态变更：隔离/解除隔离/冻结
	 * @param id 批次ID
	 * @param status AVAILABLE/HOLD/FROZEN
	 */
	void changeStatus(Long id, String status);

	/**
	 * 作业上料：校验批次状态、物料属于工单 BOM，扣减批次数量并记录消耗
	 * @param taskId 作业ID
	 * @param lotNo 批次号
	 * @param qty 上料数量
	 */
	void feed(Long taskId, String lotNo, BigDecimal qty);

	/**
	 * 查询作业的上料记录
	 * @param taskId 作业ID
	 * @return 消耗记录
	 */
	List<MtLotConsume> listConsumes(Long taskId);

}
