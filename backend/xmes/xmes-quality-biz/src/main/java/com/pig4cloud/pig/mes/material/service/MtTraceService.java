package com.pig4cloud.pig.mes.material.service;

import java.util.Map;

/**
 * 产出生成与追溯服务
 *
 * @author xmes
 */
public interface MtTraceService {

	/**
	 * 扫描已完工工单生成产出（按物料批次策略：LOT-产出批次，SERIAL-逐件序列件，NONE-跳过），
	 * 并把该工单消耗的批次写入谱系。pull 模式，服务拆分后改为订阅工单完工事件。
	 * @return 新生成的产出工单数
	 */
	int refreshOutputs();

	/**
	 * 反向追溯：按产出批次号或序列号查 工单/工艺版本/消耗批次/检验单
	 * @param no 批次号或序列号
	 * @return 追溯树
	 */
	Map<String, Object> traceBackward(String no);

	/**
	 * 正向追溯：按原料批次号查 被消耗的作业/工单 → 产出批次/序列件
	 * @param lotNo 原料批次号
	 * @return 追溯树
	 */
	Map<String, Object> traceForward(String lotNo);

}
