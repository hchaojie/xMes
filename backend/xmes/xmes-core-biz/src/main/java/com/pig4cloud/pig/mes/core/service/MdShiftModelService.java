package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.dto.ShiftModelDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdShiftModel;

import java.util.List;

/**
 * 班次模型服务
 *
 * @author xmes
 */
public interface MdShiftModelService extends IService<MdShiftModel> {

	/**
	 * 查询班次模型详情（含班段）
	 * @param id 班次模型ID
	 * @return 班次模型详情
	 */
	ShiftModelDTO getDetail(Long id);

	/**
	 * 新增班次模型（含班段）
	 * @param dto 班次模型
	 */
	void saveWithSegments(ShiftModelDTO dto);

	/**
	 * 修改班次模型（全量替换班段）
	 * @param dto 班次模型
	 */
	void updateWithSegments(ShiftModelDTO dto);

	/**
	 * 删除班次模型及其班段（被日历引用时拒绝）
	 * @param ids 班次模型ID列表
	 */
	void removeWithSegments(List<Long> ids);

}
