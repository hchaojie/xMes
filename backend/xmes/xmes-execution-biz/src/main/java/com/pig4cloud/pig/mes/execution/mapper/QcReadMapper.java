package com.pig4cloud.pig.mes.execution.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.mes.quality.api.entity.QcInspectionOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 质量检验单只读访问（质量门放行校验，boot 单体模式同库直读）。
 * 服务拆分时替换为 Feign 查询 xmes-quality。
 *
 * @author xmes
 */
@Mapper
public interface QcReadMapper extends BaseMapper<QcInspectionOrder> {

}
