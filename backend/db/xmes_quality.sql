-- ----------------------------
-- xMes 质量脚本（检验计划 / 检验单）
-- 依赖：先执行 pig.sql、xmes_core.sql、xmes_masterdata.sql、xmes_execution.sql
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `mes_qc_plan`;
CREATE TABLE `mes_qc_plan` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_code` varchar(64) NOT NULL COMMENT '计划编码',
  `plan_name` varchar(128) NOT NULL COMMENT '计划名称',
  `plan_type` varchar(32) DEFAULT 'PROCESS' COMMENT '检验类型：FIRST_ARTICLE/PROCESS/FINAL/IQC/PATROL',
  `material_id` bigint DEFAULT NULL COMMENT '适用物料ID（空=通用）',
  `operation_no` varchar(32) DEFAULT NULL COMMENT '适用工序号（空=不限）',
  `sampling_type` varchar(16) DEFAULT 'FULL' COMMENT '抽样方式：FULL-全检，FIXED-固定数量',
  `sample_size` decimal(18,6) DEFAULT NULL COMMENT '抽样数量（FIXED 时生效）',
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
  UNIQUE KEY `uk_qcplan_code` (`plan_code`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验计划';

DROP TABLE IF EXISTS `mes_qc_characteristic`;
CREATE TABLE `mes_qc_characteristic` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '检验计划ID',
  `char_no` varchar(32) NOT NULL COMMENT '特性号',
  `char_name` varchar(128) NOT NULL COMMENT '特性名称',
  `char_type` varchar(16) DEFAULT 'QUANT' COMMENT '特性类型：QUANT-计量，COUNT-计数，VISUAL-目视',
  `target_value` decimal(18,6) DEFAULT NULL COMMENT '目标值（计量）',
  `upper_limit` decimal(18,6) DEFAULT NULL COMMENT '上限（计量）',
  `lower_limit` decimal(18,6) DEFAULT NULL COMMENT '下限（计量）',
  `unit` varchar(32) DEFAULT NULL COMMENT '单位',
  `critical_flag` char(1) DEFAULT '0' COMMENT '是否关键特性（CTQ）：0-否，1-是',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_qcchar_plan` (`plan_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验特性';

DROP TABLE IF EXISTS `mes_qc_inspection_order`;
CREATE TABLE `mes_qc_inspection_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `inspection_no` varchar(64) NOT NULL COMMENT '检验单号',
  `plan_id` bigint DEFAULT NULL COMMENT '检验计划ID',
  `plan_type` varchar(32) DEFAULT 'PROCESS' COMMENT '检验类型（快照）',
  `wo_order_id` bigint DEFAULT NULL COMMENT '工单ID',
  `task_id` bigint DEFAULT NULL COMMENT '作业ID（质量门触发）',
  `order_no` varchar(64) DEFAULT NULL COMMENT '工单号（快照展示）',
  `material_code` varchar(64) DEFAULT NULL COMMENT '物料编码（快照展示）',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称（快照展示）',
  `operation_no` varchar(32) DEFAULT NULL COMMENT '工序号（快照展示）',
  `inspect_status` varchar(16) DEFAULT 'PENDING' COMMENT '状态：PENDING/INSPECTING/PASSED/CONCESSION/REJECTED',
  `sample_qty` decimal(18,6) DEFAULT NULL COMMENT '抽样数量',
  `inspector` varchar(64) DEFAULT NULL COMMENT '检验员',
  `judge_time` datetime DEFAULT NULL COMMENT '判定时间',
  `judge_remark` varchar(255) DEFAULT NULL COMMENT '判定说明',
  `source_type` varchar(16) DEFAULT 'MANUAL' COMMENT '来源：AUTO_GATE/MANUAL',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_qc_no` (`inspection_no`, `tenant_id`, `del_flag`),
  KEY `idx_qc_task` (`task_id`),
  KEY `idx_qc_status` (`inspect_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验单';

DROP TABLE IF EXISTS `mes_qc_inspection_result`;
CREATE TABLE `mes_qc_inspection_result` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `inspection_order_id` bigint NOT NULL COMMENT '检验单ID',
  `characteristic_id` bigint NOT NULL COMMENT '特性ID',
  `char_name` varchar(128) DEFAULT NULL COMMENT '特性名称（快照）',
  `char_type` varchar(16) DEFAULT NULL COMMENT '特性类型（快照）',
  `target_value` decimal(18,6) DEFAULT NULL COMMENT '目标值（快照）',
  `upper_limit` decimal(18,6) DEFAULT NULL COMMENT '上限（快照）',
  `lower_limit` decimal(18,6) DEFAULT NULL COMMENT '下限（快照）',
  `sample_no` int DEFAULT '1' COMMENT '样本号',
  `measured_value` decimal(18,6) DEFAULT NULL COMMENT '实测值（计量）',
  `judge` varchar(8) DEFAULT NULL COMMENT '判定：OK/NG',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_qcres_order` (`inspection_order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '检验结果';

-- ----------------------------
-- 菜单：质量管理（5200 段）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (5200, '质量管理', NULL, '/mes/quality', NULL, -1, 'iconfont icon-biaodan', '1', 12, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5210, '检验计划', NULL, '/mes/quality/plan/index', NULL, 5200, 'iconfont icon-fuwenben', '1', 1, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5211, '计划查看', 'mes_qcplan_view', NULL, NULL, 5210, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5212, '计划新增', 'mes_qcplan_add', NULL, NULL, 5210, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5213, '计划修改', 'mes_qcplan_edit', NULL, NULL, 5210, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5214, '计划删除', 'mes_qcplan_del', NULL, NULL, 5210, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5220, '检验工作台', NULL, '/mes/quality/inspection/index', NULL, 5200, 'iconfont icon-biaodan', '1', 2, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5221, '检验查看', 'mes_qc_view', NULL, NULL, 5220, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5222, '检验执行', 'mes_qc_inspect', NULL, NULL, 5220, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5223, '检验判定', 'mes_qc_judge', NULL, NULL, 5220, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 5200 AND `menu_id` < 5300
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = `sys_menu`.`menu_id`);

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- 物料批次 / 序列件 / 消耗 / 谱系
-- ----------------------------
DROP TABLE IF EXISTS `mes_mt_lot`;
CREATE TABLE `mes_mt_lot` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `lot_no` varchar(64) NOT NULL COMMENT '批次号',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `material_code` varchar(64) DEFAULT NULL COMMENT '物料编码（快照展示）',
  `material_name` varchar(128) DEFAULT NULL COMMENT '物料名称（快照展示）',
  `qty` decimal(18,6) NOT NULL COMMENT '当前数量',
  `unit` varchar(32) DEFAULT NULL COMMENT '单位',
  `lot_status` varchar(16) DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE/HOLD/FROZEN/CONSUMED',
  `source_type` varchar(16) DEFAULT 'PURCHASE' COMMENT '来源：PURCHASE/PRODUCTION/SPLIT',
  `wo_order_id` bigint DEFAULT NULL COMMENT '产出工单ID',
  `supplier_lot_no` varchar(64) DEFAULT NULL COMMENT '供应商批次号',
  `prod_date` date DEFAULT NULL COMMENT '生产日期',
  `expire_date` date DEFAULT NULL COMMENT '失效日期',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_lot_no` (`lot_no`, `tenant_id`, `del_flag`),
  KEY `idx_lot_material` (`material_id`),
  KEY `idx_lot_wo` (`wo_order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '物料批次';

DROP TABLE IF EXISTS `mes_mt_serial`;
CREATE TABLE `mes_mt_serial` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `serial_no` varchar(64) NOT NULL COMMENT '序列号',
  `material_id` bigint DEFAULT NULL COMMENT '物料ID',
  `material_code` varchar(64) DEFAULT NULL COMMENT '物料编码（快照展示）',
  `lot_id` bigint DEFAULT NULL COMMENT '所属批次ID',
  `wo_order_id` bigint DEFAULT NULL COMMENT '产出工单ID',
  `serial_status` varchar(16) DEFAULT 'OK' COMMENT '状态：OK/SCRAP/HOLD',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_serial_no` (`serial_no`, `tenant_id`, `del_flag`),
  KEY `idx_serial_wo` (`wo_order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '序列件';

DROP TABLE IF EXISTS `mes_mt_lot_consume`;
CREATE TABLE `mes_mt_lot_consume` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '作业ID',
  `order_id` bigint DEFAULT NULL COMMENT '工单ID',
  `lot_id` bigint NOT NULL COMMENT '消耗批次ID',
  `lot_no` varchar(64) DEFAULT NULL COMMENT '批次号（快照展示）',
  `material_id` bigint DEFAULT NULL COMMENT '物料ID（快照）',
  `material_code` varchar(64) DEFAULT NULL COMMENT '物料编码（快照展示）',
  `qty` decimal(18,6) NOT NULL COMMENT '消耗数量',
  `person_name` varchar(64) DEFAULT NULL COMMENT '上料人',
  `feed_time` datetime DEFAULT NULL COMMENT '上料时间',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_cons_task` (`task_id`),
  KEY `idx_cons_order` (`order_id`),
  KEY `idx_cons_lot` (`lot_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '上料消耗记录';

DROP TABLE IF EXISTS `mes_mt_genealogy`;
CREATE TABLE `mes_mt_genealogy` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `parent_lot_id` bigint NOT NULL COMMENT '消耗（投入）批次ID',
  `child_lot_id` bigint DEFAULT NULL COMMENT '产出批次ID',
  `child_serial_id` bigint DEFAULT NULL COMMENT '产出序列件ID',
  `order_id` bigint NOT NULL COMMENT '工单ID',
  `qty` decimal(18,6) DEFAULT NULL COMMENT '消耗数量',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_gen_parent` (`parent_lot_id`),
  KEY `idx_gen_child_lot` (`child_lot_id`),
  KEY `idx_gen_child_serial` (`child_serial_id`),
  KEY `idx_gen_order` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '批次谱系';

-- 菜单：物料与追溯（5300 段）
INSERT INTO `sys_menu` VALUES (5300, '物料管理', NULL, '/mes/material', NULL, -1, 'iconfont icon-fuwenben', '1', 13, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5310, '批次台账', NULL, '/mes/material/lot/index', NULL, 5300, 'iconfont icon-caidan', '1', 1, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5311, '批次查看', 'mes_lot_view', NULL, NULL, 5310, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5312, '批次登记/产出', 'mes_lot_add', NULL, NULL, 5310, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5313, '批次状态', 'mes_lot_status', NULL, NULL, 5310, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5314, '作业上料', 'mes_lot_feed', NULL, NULL, 5310, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5320, '批次追溯', NULL, '/mes/material/trace/index', NULL, 5300, 'iconfont icon-icon-', '1', 2, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5321, '追溯查询', 'mes_trace_view', NULL, NULL, 5320, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 5300 AND `menu_id` < 5400
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = `sys_menu`.`menu_id`);
