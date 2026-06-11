-- ----------------------------
-- xMes 制造主数据脚本（物料 / BOM / 工作计划）
-- 依赖：先执行 pig.sql 与 xmes_core.sql
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 物料
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_material`;
CREATE TABLE `mes_md_material` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `material_code` varchar(64) NOT NULL COMMENT '物料编码',
  `material_name` varchar(128) NOT NULL COMMENT '物料名称',
  `material_type` varchar(32) DEFAULT 'RAW' COMMENT '物料类型：RAW-原材料，SEMI-半成品，FINISHED-成品，AUX-辅料',
  `spec` varchar(255) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(32) NOT NULL COMMENT '计量单位',
  `lot_strategy` varchar(16) DEFAULT 'LOT' COMMENT '批次管理策略：NONE-不管批，LOT-批次，SERIAL-序列号（一物一码）',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期（天）',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `status` char(1) DEFAULT '0' COMMENT '状态：0-启用，1-停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mat_code` (`material_code`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '物料';

-- ----------------------------
-- BOM 头（按 父物料+版本 唯一，状态：草稿/已发布/已冻结）
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_bom`;
CREATE TABLE `mes_md_bom` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '父物料ID',
  `version` int NOT NULL COMMENT '版本号',
  `bom_status` varchar(16) DEFAULT 'DRAFT' COMMENT 'BOM状态：DRAFT-草稿，RELEASED-已发布，FROZEN-已冻结',
  `effective_from` date DEFAULT NULL COMMENT '生效日期',
  `effective_to` date DEFAULT NULL COMMENT '失效日期',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `status` char(1) DEFAULT '0' COMMENT '状态：0-启用，1-停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_bom_mat_ver` (`material_id`, `version`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'BOM头';

-- ----------------------------
-- BOM 行
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_bom_line`;
CREATE TABLE `mes_md_bom_line` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `bom_id` bigint NOT NULL COMMENT 'BOM头ID',
  `child_material_id` bigint NOT NULL COMMENT '子物料ID',
  `quantity` decimal(18,6) NOT NULL COMMENT '单位用量',
  `loss_rate` decimal(8,4) DEFAULT '0.0000' COMMENT '损耗率（%）',
  `consume_operation_no` varchar(32) DEFAULT NULL COMMENT '消耗工序号（空表示末道工序倒冲）',
  `key_flag` char(1) DEFAULT '0' COMMENT '是否关键件（需扫码绑定）：0-否，1-是',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_bl_bom` (`bom_id`),
  KEY `idx_bl_child` (`child_material_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'BOM行';

-- ----------------------------
-- 工作计划头（按 物料+版本 唯一）
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_routing`;
CREATE TABLE `mes_md_routing` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `version` int NOT NULL COMMENT '版本号',
  `routing_name` varchar(128) DEFAULT NULL COMMENT '工作计划名称',
  `routing_status` varchar(16) DEFAULT 'DRAFT' COMMENT '状态：DRAFT-草稿，RELEASED-已发布，FROZEN-已冻结',
  `effective_from` date DEFAULT NULL COMMENT '生效日期',
  `effective_to` date DEFAULT NULL COMMENT '失效日期',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `status` char(1) DEFAULT '0' COMMENT '状态：0-启用，1-停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_route_mat_ver` (`material_id`, `version`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工作计划头';

-- ----------------------------
-- 工序
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_routing_operation`;
CREATE TABLE `mes_md_routing_operation` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `routing_id` bigint NOT NULL COMMENT '工作计划ID',
  `operation_no` varchar(32) NOT NULL COMMENT '工序号（如 0010）',
  `operation_name` varchar(128) NOT NULL COMMENT '工序名称',
  `operation_type` varchar(32) DEFAULT 'PROCESS' COMMENT '工序类型：PROCESS-加工，ASSEMBLY-装配，INSPECT-检验，OUTSOURCE-外协，REWORK-返修',
  `work_center_id` bigint DEFAULT NULL COMMENT '默认工作中心ID',
  `setup_time` decimal(12,2) DEFAULT '0.00' COMMENT '准备时间（分钟）',
  `unit_time` decimal(12,4) DEFAULT '0.0000' COMMENT '单件时间（分钟）',
  `wait_time` decimal(12,2) DEFAULT '0.00' COMMENT '等待时间（分钟）',
  `report_mode` varchar(16) DEFAULT 'PIECE' COMMENT '报工方式：PIECE-按件，TIME-按时，MILESTONE-里程碑',
  `quality_gate` char(1) DEFAULT '0' COMMENT '是否质量门：0-否，1-是',
  `transfer_qty` decimal(18,6) DEFAULT '0.000000' COMMENT '转移批量（0 表示整批转移）',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_op_routing` (`routing_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工序';

-- ----------------------------
-- 菜单：制造主数据（4100 段，挂在 4000 车间建模 根菜单下）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (4060, '物料管理', NULL, '/mes/masterdata/material/index', NULL, 4000, 'iconfont icon-fuwenben', '1', 4, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4061, '物料查看', 'mes_mat_view', NULL, NULL, 4060, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4062, '物料新增', 'mes_mat_add', NULL, NULL, 4060, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4063, '物料修改', 'mes_mat_edit', NULL, NULL, 4060, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4064, '物料删除', 'mes_mat_del', NULL, NULL, 4060, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_menu` VALUES (4070, 'BOM管理', NULL, '/mes/masterdata/bom/index', NULL, 4000, 'iconfont icon-caidan', '1', 5, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4071, 'BOM查看', 'mes_bom_view', NULL, NULL, 4070, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4072, 'BOM新增', 'mes_bom_add', NULL, NULL, 4070, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4073, 'BOM修改', 'mes_bom_edit', NULL, NULL, 4070, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4074, 'BOM删除', 'mes_bom_del', NULL, NULL, 4070, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4075, 'BOM发布/冻结', 'mes_bom_release', NULL, NULL, 4070, NULL, '1', 5, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_menu` VALUES (4080, '工作计划', NULL, '/mes/masterdata/routing/index', NULL, 4000, 'iconfont icon-gongju', '1', 6, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4081, '工作计划查看', 'mes_route_view', NULL, NULL, 4080, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4082, '工作计划新增', 'mes_route_add', NULL, NULL, 4080, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4083, '工作计划修改', 'mes_route_edit', NULL, NULL, 4080, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4084, '工作计划删除', 'mes_route_del', NULL, NULL, 4080, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (4085, '工作计划发布/冻结', 'mes_route_release', NULL, NULL, 4080, NULL, '1', 5, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

-- 授权给管理员角色（role_id = 1）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 4060 AND `menu_id` < 4100
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = `sys_menu`.`menu_id`);

SET FOREIGN_KEY_CHECKS = 1;
