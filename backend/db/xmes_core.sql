-- ----------------------------
-- xMes 平台与建模服务（xmes-core）数据库脚本
-- 依赖：先执行 pig.sql（系统表与菜单基础数据）
-- 车间建模切片：工厂结构（工厂/车间/工作中心/工位）、班次模型、工厂日历
-- ----------------------------

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 工厂（站点）
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_site`;
CREATE TABLE `mes_md_site` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `site_code` varchar(64) NOT NULL COMMENT '工厂编码',
  `site_name` varchar(128) NOT NULL COMMENT '工厂名称',
  `address` varchar(255) DEFAULT NULL COMMENT '地址',
  `time_zone` varchar(64) DEFAULT 'Asia/Shanghai' COMMENT '时区',
  `calendar_id` bigint DEFAULT NULL COMMENT '默认工厂日历ID',
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
  UNIQUE KEY `uk_site_code` (`site_code`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工厂（站点）';

-- ----------------------------
-- 车间（区域）
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_area`;
CREATE TABLE `mes_md_area` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `area_code` varchar(64) NOT NULL COMMENT '车间编码',
  `area_name` varchar(128) NOT NULL COMMENT '车间名称',
  `site_id` bigint NOT NULL COMMENT '所属工厂ID',
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
  UNIQUE KEY `uk_area_code` (`area_code`, `tenant_id`, `del_flag`),
  KEY `idx_area_site` (`site_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '车间（区域）';

-- ----------------------------
-- 工作中心
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_work_center`;
CREATE TABLE `mes_md_work_center` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `center_code` varchar(64) NOT NULL COMMENT '工作中心编码',
  `center_name` varchar(128) NOT NULL COMMENT '工作中心名称',
  `area_id` bigint NOT NULL COMMENT '所属车间ID',
  `center_type` varchar(32) DEFAULT 'LINE' COMMENT '类型：LINE-产线，CELL-设备组，TEAM-班组，OUTSOURCE-外协',
  `calendar_id` bigint DEFAULT NULL COMMENT '工厂日历ID（覆盖工厂默认）',
  `parallel_count` int DEFAULT '1' COMMENT '并行机台数（产能模型）',
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
  UNIQUE KEY `uk_wc_code` (`center_code`, `tenant_id`, `del_flag`),
  KEY `idx_wc_area` (`area_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工作中心';

-- ----------------------------
-- 工位（机台）
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_workplace`;
CREATE TABLE `mes_md_workplace` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `workplace_code` varchar(64) NOT NULL COMMENT '工位编码',
  `workplace_name` varchar(128) NOT NULL COMMENT '工位名称',
  `work_center_id` bigint NOT NULL COMMENT '所属工作中心ID',
  `workplace_type` varchar(32) DEFAULT 'MACHINE' COMMENT '类型：MACHINE-机加，ASSEMBLY-装配，INSPECT-检验，REWORK-返修，VIRTUAL-外协虚拟',
  `login_required` char(1) DEFAULT '1' COMMENT '是否需要上岗登录：0-否，1-是',
  `allow_multi_person` char(1) DEFAULT '0' COMMENT '是否允许多人同时上岗：0-否，1-是',
  `allow_parallel_task` char(1) DEFAULT '0' COMMENT '是否允许并行作业：0-否，1-是',
  `terminal_code` varchar(64) DEFAULT NULL COMMENT '绑定终端编码',
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
  UNIQUE KEY `uk_wp_code` (`workplace_code`, `tenant_id`, `del_flag`),
  KEY `idx_wp_center` (`work_center_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工位（机台）';

-- ----------------------------
-- 班次模型
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_shift_model`;
CREATE TABLE `mes_md_shift_model` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `model_code` varchar(64) NOT NULL COMMENT '班次模型编码',
  `model_name` varchar(128) NOT NULL COMMENT '班次模型名称',
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
  UNIQUE KEY `uk_shift_code` (`model_code`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班次模型';

-- ----------------------------
-- 班段
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_shift_segment`;
CREATE TABLE `mes_md_shift_segment` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `shift_model_id` bigint NOT NULL COMMENT '班次模型ID',
  `segment_code` varchar(32) NOT NULL COMMENT '班段编码（如 D/N）',
  `segment_name` varchar(64) NOT NULL COMMENT '班段名称（如 早班/夜班）',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `cross_day` char(1) DEFAULT '0' COMMENT '结束时间是否跨天：0-否，1-是',
  `break_minutes` int DEFAULT '0' COMMENT '休息时长（分钟）',
  `sort_order` int DEFAULT '0' COMMENT '排序值',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_seg_model` (`shift_model_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '班段';

-- ----------------------------
-- 工厂日历
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_calendar`;
CREATE TABLE `mes_md_calendar` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `calendar_code` varchar(64) NOT NULL COMMENT '日历编码',
  `calendar_name` varchar(128) NOT NULL COMMENT '日历名称',
  `site_id` bigint DEFAULT NULL COMMENT '所属工厂ID',
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
  UNIQUE KEY `uk_cal_code` (`calendar_code`, `tenant_id`, `del_flag`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工厂日历';

-- ----------------------------
-- 工厂日历日明细
-- ----------------------------
DROP TABLE IF EXISTS `mes_md_calendar_day`;
CREATE TABLE `mes_md_calendar_day` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `calendar_id` bigint NOT NULL COMMENT '工厂日历ID',
  `cal_date` date NOT NULL COMMENT '日期',
  `day_type` varchar(16) NOT NULL DEFAULT 'WORK' COMMENT '日类型：WORK-工作日，REST-休息日，HOLIDAY-节假日',
  `shift_model_id` bigint DEFAULT NULL COMMENT '班次模型ID（工作日必填）',
  `create_by` varchar(64) DEFAULT ' ' COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT ' ' COMMENT '修改人',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志：0-未删除，1-已删除',
  `tenant_id` bigint DEFAULT '1' COMMENT '租户ID',
  PRIMARY KEY (`id`),
  KEY `idx_day_cal_date` (`calendar_id`, `cal_date`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '工厂日历日明细';

-- ----------------------------
-- 菜单：车间建模（菜单ID 4000 段）
-- 字段：menu_id, name, permission, path, component, parent_id, icon, visible, sort_order, keep_alive, embedded, menu_type, create_by, create_time, update_by, update_time, del_flag
-- ----------------------------
INSERT INTO `sys_menu` VALUES (5000, '车间建模', NULL, '/mes/modeling', NULL, -1, 'iconfont icon-shouye', '1', 10, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_menu` VALUES (5010, '工厂结构', NULL, '/mes/modeling/factory/index', NULL, 5000, 'iconfont icon-icon-', '1', 1, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5011, '工厂查看', 'mes_site_view', NULL, NULL, 5010, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5012, '工厂新增', 'mes_site_add', NULL, NULL, 5010, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5013, '工厂修改', 'mes_site_edit', NULL, NULL, 5010, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5014, '工厂删除', 'mes_site_del', NULL, NULL, 5010, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5015, '车间查看', 'mes_area_view', NULL, NULL, 5010, NULL, '1', 5, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5016, '车间新增', 'mes_area_add', NULL, NULL, 5010, NULL, '1', 6, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5017, '车间修改', 'mes_area_edit', NULL, NULL, 5010, NULL, '1', 7, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5018, '车间删除', 'mes_area_del', NULL, NULL, 5010, NULL, '1', 8, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5019, '工作中心查看', 'mes_wc_view', NULL, NULL, 5010, NULL, '1', 9, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5020, '工作中心新增', 'mes_wc_add', NULL, NULL, 5010, NULL, '1', 10, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5021, '工作中心修改', 'mes_wc_edit', NULL, NULL, 5010, NULL, '1', 11, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5022, '工作中心删除', 'mes_wc_del', NULL, NULL, 5010, NULL, '1', 12, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5023, '工位查看', 'mes_wp_view', NULL, NULL, 5010, NULL, '1', 13, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5024, '工位新增', 'mes_wp_add', NULL, NULL, 5010, NULL, '1', 14, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5025, '工位修改', 'mes_wp_edit', NULL, NULL, 5010, NULL, '1', 15, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5026, '工位删除', 'mes_wp_del', NULL, NULL, 5010, NULL, '1', 16, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_menu` VALUES (5040, '班次模型', NULL, '/mes/modeling/shift/index', NULL, 5000, 'iconfont icon-rizhi', '1', 2, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5041, '班次查看', 'mes_shift_view', NULL, NULL, 5040, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5042, '班次新增', 'mes_shift_add', NULL, NULL, 5040, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5043, '班次修改', 'mes_shift_edit', NULL, NULL, 5040, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5044, '班次删除', 'mes_shift_del', NULL, NULL, 5040, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

INSERT INTO `sys_menu` VALUES (5050, '工厂日历', NULL, '/mes/modeling/calendar/index', NULL, 5000, 'iconfont icon-rili', '1', 3, '0', '0', '0', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5051, '日历查看', 'mes_cal_view', NULL, NULL, 5050, NULL, '1', 1, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5052, '日历新增', 'mes_cal_add', NULL, NULL, 5050, NULL, '1', 2, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5053, '日历修改', 'mes_cal_edit', NULL, NULL, 5050, NULL, '1', 3, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');
INSERT INTO `sys_menu` VALUES (5054, '日历删除', 'mes_cal_del', NULL, NULL, 5050, NULL, '1', 4, '0', '0', '1', ' ', NOW(), ' ', NOW(), '0');

-- 授权给管理员角色（role_id = 1）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, `menu_id` FROM `sys_menu` WHERE `menu_id` >= 5000 AND `menu_id` < 6000;

SET FOREIGN_KEY_CHECKS = 1;
