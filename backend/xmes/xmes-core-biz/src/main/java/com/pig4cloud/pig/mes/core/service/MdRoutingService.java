package com.pig4cloud.pig.mes.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pig4cloud.pig.mes.core.api.dto.RoutingDTO;
import com.pig4cloud.pig.mes.core.api.entity.MdRouting;

import java.util.List;

/**
 * 工作计划服务（版本化主数据：草稿 → 发布 → 冻结）
 *
 * @author xmes
 */
public interface MdRoutingService extends IService<MdRouting> {

	/**
	 * 分页查询（携带物料编码/名称）
	 * @param page 分页对象
	 * @param materialId 物料ID（可空）
	 * @param status 状态（可空）
	 * @return 分页结果
	 */
	Page<RoutingDTO> pageDetail(Page<MdRouting> page, Long materialId, String status);

	/**
	 * 查询详情（含明细行）
	 * @param id 主键ID
	 * @return 详情
	 */
	RoutingDTO getDetail(Long id);

	/**
	 * 新增草稿（版本号自动取该物料最大版本+1）
	 * @param dto 工作计划
	 */
	void saveDraft(RoutingDTO dto);

	/**
	 * 修改草稿（全量替换明细行，仅草稿可改）
	 * @param dto 工作计划
	 */
	void updateDraft(RoutingDTO dto);

	/**
	 * 发布：草稿 → 已发布；同物料其他已发布版本自动冻结
	 * @param id 主键ID
	 */
	void release(Long id);

	/**
	 * 冻结：已发布 → 已冻结
	 * @param id 主键ID
	 */
	void freeze(Long id);

	/**
	 * 复制为新草稿版本（明细行一并复制）
	 * @param id 源版本ID
	 * @return 新版本ID
	 */
	Long copyVersion(Long id);

	/**
	 * 删除（仅草稿可删，明细行级联删除）
	 * @param ids 主键ID列表
	 */
	void removeDrafts(List<Long> ids);

}
