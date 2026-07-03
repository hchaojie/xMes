-- ----------------------------
-- xMes 设备维护脚本（维护计划 / 维护工单 / 点检）—— Phase 2 M4
-- 对应 FR-RES-31~33、35、36
-- 依赖：先执行 pig.sql、xmes_core.sql
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 维护计划（FR-RES-31：按周期定义保养/点检计划与标准作业项）
-- ----------------------------
DROP TABLE IF EXISTS `mes_mnt_plan`;
CREATE TABLE `mes_mnt_plan` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_code` varchar(64) NOT NULL COMMENT '计划编码',
  `plan_name` varchar(128) NOT NULL COMMENT '计划名称',
  `plan_type` varchar(16) DEFAULT 'PM' COMMENT '类型：PM-保养计划，SPOT-点检计划',
  `workplace_id` bigint NOT NULL COMMENT '设备（工位）ID',
  `cycle_type` varchar(16) DEFAULT 'DAY' COMMENT '周期类型：DAY-日历天，RUNTIME-运行时长(小时)，OUTPUT-产量计数',
  `cycle_value` int NOT NULL COMMENT '周期值（天/小时/件）',
  `next_due_date` date DEFAULT NULL COMMENT '下次到期日（DAY 周期自动滚动；RUNTIME/OUTPUT 周期由物联网数据触发，当前手工维护）',
  `last_order_date` date DEFAULT NULL COMMENT '最近生成工单日期',
  `status` char(1) DEFAULT '0' COMMENT '状态：0-启用，1-停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mnt_plan_code` (`plan_code`, `tenant_id`, `del_flag`),
  KEY `idx_mnt_plan_wp` (`workplace_id`),
  KEY `idx_mnt_plan_due` (`next_due_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '维护计划';

-- ----------------------------
-- 维护计划标准作业项（检查表）
-- ----------------------------
DROP TABLE IF EXISTS `mes_mnt_plan_item`;
CREATE TABLE `mes_mnt_plan_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `plan_id` bigint NOT NULL COMMENT '维护计划ID',
  `item_no` varchar(32) NOT NULL COMMENT '项目号',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `standard_desc` varchar(255) DEFAULT NULL COMMENT '标准/要求',
  `method_desc` varchar(255) DEFAULT NULL COMMENT '方法/工具',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_mnt_pitem_plan` (`plan_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '维护计划标准作业项';

-- ----------------------------
-- 维护工单（FR-RES-32：计划到期自动生成 / 故障报修；状态流 待接单→维修中→待验证→关闭）
-- ----------------------------
DROP TABLE IF EXISTS `mes_mnt_order`;
CREATE TABLE `mes_mnt_order` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_no` varchar(64) NOT NULL COMMENT '维护工单号',
  `order_type` varchar(16) DEFAULT 'REPAIR' COMMENT '类型：PM-保养，REPAIR-维修，SPOT-点检',
  `plan_id` bigint DEFAULT NULL COMMENT '来源维护计划ID（计划生成时）',
  `source_order_id` bigint DEFAULT NULL COMMENT '来源维护工单ID（点检 NG 自动报修时）',
  `workplace_id` bigint NOT NULL COMMENT '设备（工位）ID',
  `order_status` varchar(16) DEFAULT 'PENDING' COMMENT '状态：PENDING-待接单，IN_PROGRESS-维修中，REVIEW-待验证，CLOSED-已关闭，CANCELLED-已取消',
  `due_date` date DEFAULT NULL COMMENT '要求完成日期',
  `fault_desc` varchar(500) DEFAULT NULL COMMENT '故障现象（报修描述）',
  `fault_cause` varchar(500) DEFAULT NULL COMMENT '故障原因',
  `action_taken` varchar(500) DEFAULT NULL COMMENT '处理措施',
  `spare_parts` varchar(500) DEFAULT NULL COMMENT '备件消耗（自由文本，备件台账列 Phase 3）',
  `report_by` varchar(64) DEFAULT NULL COMMENT '报修人',
  `report_time` datetime DEFAULT NULL COMMENT '报修/生成时间',
  `accept_by` varchar(64) DEFAULT NULL COMMENT '接单人（维修工）',
  `accept_time` datetime DEFAULT NULL COMMENT '接单时间（维修开始，设备进入 MAINT）',
  `finish_time` datetime DEFAULT NULL COMMENT '维修完成时间（设备退出 MAINT）',
  `close_by` varchar(64) DEFAULT NULL COMMENT '验证关闭人',
  `close_time` datetime DEFAULT NULL COMMENT '关闭时间',
  `downtime_minutes` int DEFAULT NULL COMMENT '停机时长（分钟，接单→完成）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mnt_order_no` (`order_no`, `tenant_id`, `del_flag`),
  KEY `idx_mnt_order_wp` (`workplace_id`),
  KEY `idx_mnt_order_status` (`order_status`),
  KEY `idx_mnt_order_plan` (`plan_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '维护工单';

-- ----------------------------
-- 维护工单检查项结果（保养/点检执行记录；点检 NG 自动报修 FR-RES-36）
-- ----------------------------
DROP TABLE IF EXISTS `mes_mnt_order_item`;
CREATE TABLE `mes_mnt_order_item` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '维护工单ID',
  `item_no` varchar(32) NOT NULL COMMENT '项目号',
  `item_name` varchar(128) NOT NULL COMMENT '项目名称',
  `standard_desc` varchar(255) DEFAULT NULL COMMENT '标准/要求',
  `method_desc` varchar(255) DEFAULT NULL COMMENT '方法/工具',
  `check_result` varchar(8) DEFAULT NULL COMMENT '结果：OK/NG',
  `result_remark` varchar(255) DEFAULT NULL COMMENT '结果说明',
  `repair_order_id` bigint DEFAULT NULL COMMENT 'NG 自动报修生成的维修工单ID',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_mnt_oitem_order` (`order_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '维护工单检查项结果';

-- 菜单：设备维护（5400 段）
INSERT INTO `sys_menu` VALUES (5400, '设备维护', NULL, '/mes/maintenance', NULL, -1, 'iconfont icon-shezhi', '1', 14, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5410, '维护计划', NULL, '/mes/maintenance/plan/index', NULL, 5400, 'iconfont icon-fuwenben', '1', 1, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5411, '计划查看', 'mes_mntplan_view', NULL, NULL, 5410, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5412, '计划新增', 'mes_mntplan_add', NULL, NULL, 5410, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5413, '计划修改', 'mes_mntplan_edit', NULL, NULL, 5410, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5414, '计划删除', 'mes_mntplan_del', NULL, NULL, 5410, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5415, '生成到期工单', 'mes_mntplan_gen', NULL, NULL, 5410, NULL, '1', 5, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5420, '维护工单', NULL, '/mes/maintenance/order/index', NULL, 5400, 'iconfont icon-biaodan', '1', 2, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5421, '工单查看', 'mes_mnt_view', NULL, NULL, 5420, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5422, '故障报修', 'mes_mnt_report', NULL, NULL, 5420, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5423, '工单处理', 'mes_mnt_handle', NULL, NULL, 5420, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5424, '验证关闭', 'mes_mnt_close', NULL, NULL, 5420, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5430, '维护分析', NULL, '/mes/maintenance/analysis/index', NULL, 5400, 'iconfont icon-icon-', '1', 3, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5431, '分析查看', 'mes_mnt_analysis', NULL, NULL, 5430, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 5400 AND `menu_id` < 5500
  AND NOT EXISTS (SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = `sys_menu`.`menu_id`);
