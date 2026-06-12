package com.pig4cloud.pig.mes.quality.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.execution.api.entity.WoOrder;
import com.pig4cloud.pig.mes.execution.api.entity.WoTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 执行域/主数据只读访问（boot 单体模式同库直读）。
 * <p>
 * 服务拆分时此处须替换为事件订阅（质量门完工事件）与 Feign 查询，
 * 禁止跨库直连——这是 quality 与 execution/core 之间唯一的数据耦合点。
 *
 * @author xmes
 */
public interface ExecReadMapper {

	@Mapper
	interface TaskReadMapper extends BaseMapper<WoTask> {

	}

	@Mapper
	interface OrderReadMapper extends BaseMapper<WoOrder> {

	}

	@Mapper
	interface MaterialReadMapper extends BaseMapper<MdMaterial> {

	}

}
