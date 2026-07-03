package com.pig4cloud.pig.mes.execution.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pig4cloud.pig.mes.core.api.entity.MdCalendarDay;
import com.pig4cloud.pig.mes.core.api.entity.MdMaterial;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;
import com.pig4cloud.pig.mes.core.api.entity.MdRoutingOperation;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkCenter;
import com.pig4cloud.pig.mes.core.api.entity.MdWorkplace;
import com.pig4cloud.pig.mes.core.api.entity.MntOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 主数据只读访问（boot 单体模式同库直读）。
 * <p>
 * 服务拆分（微服务模式）时，此包下的只读 Mapper 须替换为 xmes-core 的 Feign 接口，
 * 禁止跨库直连——这是 execution 与 core 之间唯一的数据耦合点。
 *
 * @author xmes
 */
public interface MdReadMapper {

	@Mapper
	interface MaterialReadMapper extends BaseMapper<MdMaterial> {

	}

	@Mapper
	interface RoutingReadMapper extends BaseMapper<MdRouting> {

	}

	@Mapper
	interface RoutingOperationReadMapper extends BaseMapper<MdRoutingOperation> {

	}

	@Mapper
	interface WorkCenterReadMapper extends BaseMapper<MdWorkCenter> {

	}

	@Mapper
	interface WorkplaceReadMapper extends BaseMapper<MdWorkplace> {

	}

	@Mapper
	interface CalendarDayReadMapper extends BaseMapper<MdCalendarDay> {

	}

	@Mapper
	interface MntOrderReadMapper extends BaseMapper<MntOrder> {

	}

}
