-- ----------------------------
-- xMes 生产执行脚本（工单 / 作业）
-- 依赖：先执行 pig.sql、xmes_core.sql、xmes_masterdata.sql
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 工单
-- ----------------------------
DROP TABLE IF EXISTS `mes_wo_order`;
CREATE TABLE `mes_wo_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '工单号',
  `order_type` varchar(32) DEFAULT 'PRODUCTION' COMMENT '订单类型：PRODUCTION-生产，REWORK-返工，OVERHEAD-间接费用',
  `source_type` varchar(16) DEFAULT 'PLAN' COMMENT '来源：PLAN-工作计划生成，ERP-接口，MANUAL-手工',
  `material_id` bigint NOT NULL COMMENT '物料ID',
  `quantity` decimal(18,6) NOT NULL COMMENT '计划数量',
  `unit` varchar(32) DEFAULT NULL COMMENT '单位',
  `plan_start_date` date DEFAULT NULL COMMENT '计划开始日期',
  `plan_end_date` date DEFAULT NULL COMMENT '计划完工日期',
  `priority` int DEFAULT '0' COMMENT '优先级（数值越大越优先）',
  `order_status` varchar(16) DEFAULT 'CREATED' COMMENT '工单状态：CREATED/RELEASED/IN_PROGRESS/HOLD/COMPLETED/CLOSED/CANCELLED',
  `routing_id` bigint DEFAULT NULL COMMENT '工艺快照来源：工作计划ID',
  `routing_version` int DEFAULT NULL COMMENT '工艺快照来源：工作计划版本',
  `erp_order_no` varchar(64) DEFAULT NULL COMMENT 'ERP 单据号',
  `qty_good` decimal(18,6) DEFAULT '0.000000' COMMENT '良品数量',
  `qty_scrap` decimal(18,6) DEFAULT '0.000000' COMMENT '报废数量',
  `qty_rework` decimal(18,6) DEFAULT '0.000000' COMMENT '返工数量',
  `hold_reason` varchar(255) DEFAULT NULL COMMENT '挂起原因',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_wo_no` (`order_no`, `tenant_id`, `del_flag`),
  KEY `idx_wo_material` (`material_id`),
  KEY `idx_wo_status` (`order_status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工单';

-- ----------------------------
-- 作业（工单×工序执行实例，含工序快照）
-- ----------------------------
DROP TABLE IF EXISTS `mes_wo_task`;
CREATE TABLE `mes_wo_task` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '工单ID',
  `operation_no` varchar(32) NOT NULL COMMENT '工序号（快照）',
  `operation_name` varchar(128) DEFAULT NULL COMMENT '工序名称（快照）',
  `operation_type` varchar(32) DEFAULT 'PROCESS' COMMENT '工序类型（快照）',
  `task_status` varchar(16) DEFAULT 'PENDING' COMMENT '作业状态：PENDING/SCHEDULED/DISPATCHED/RUNNING/PAUSED/COMPLETED',
  `work_center_id` bigint DEFAULT NULL COMMENT '工作中心ID（快照，可排程调整）',
  `workplace_id` bigint DEFAULT NULL COMMENT '排程工位ID',
  `plan_start` datetime DEFAULT NULL COMMENT '排程开始时间',
  `plan_end` datetime DEFAULT NULL COMMENT '排程结束时间',
  `qty_target` decimal(18,6) DEFAULT NULL COMMENT '应做数量',
  `qty_good` decimal(18,6) DEFAULT '0.000000' COMMENT '良品数量',
  `qty_scrap` decimal(18,6) DEFAULT '0.000000' COMMENT '报废数量',
  `qty_rework` decimal(18,6) DEFAULT '0.000000' COMMENT '返工数量',
  `setup_time` decimal(12,2) DEFAULT '0.00' COMMENT '准备时间（分钟，快照）',
  `unit_time` decimal(12,4) DEFAULT '0.0000' COMMENT '单件时间（分钟，快照）',
  `wait_time` decimal(12,2) DEFAULT '0.00' COMMENT '等待时间（分钟，快照）',
  `report_mode` varchar(16) DEFAULT 'PIECE' COMMENT '报工方式（快照）',
  `quality_gate` char(1) DEFAULT '0' COMMENT '是否质量门（快照）：0-否，1-是',
  `transfer_qty` decimal(18,6) DEFAULT '0.000000' COMMENT '转移批量（快照）',
  `split_seq` int DEFAULT '0' COMMENT '拆分序号',
  `sort_order` int DEFAULT '0' COMMENT '排序值（工序顺序）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_task_order` (`order_id`),
  KEY `idx_task_status` (`task_status`),
  KEY `idx_task_wc` (`work_center_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '作业';

-- ----------------------------
-- 菜单：生产执行（5100 段）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (5100, '生产执行', NULL, '/mes/order', NULL, -1, 'iconfont icon-gongju', '1', 11, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5110, '工单管理', NULL, '/mes/order/workorder/index', NULL, 5100, 'iconfont icon-caidan', '1', 1, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5111, '工单查看', 'mes_wo_view', NULL, NULL, 5110, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5112, '工单新增', 'mes_wo_add', NULL, NULL, 5110, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5113, '工单下达', 'mes_wo_release', NULL, NULL, 5110, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5114, '工单挂起/恢复', 'mes_wo_hold', NULL, NULL, 5110, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5115, '工单取消', 'mes_wo_cancel', NULL, NULL, 5110, NULL, '1', 5, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5116, '工单删除', 'mes_wo_del', NULL, NULL, 5110, NULL, '1', 6, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

-- 授权给管理员角色（role_id = 1）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 5100 AND `menu_id` < 5200
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = `sys_menu`.`menu_id`);

SET FOREIGN_KEY_CHECKS = 1;
