# xMes — 制造执行系统（MES）

基于 B/S 架构的车间制造执行系统（Manufacturing Execution System）。

- **后端**：[pig-mesh/pig](https://github.com/pig-mesh/pig)（Spring Cloud / Spring Boot 3.x / Spring Authorization Server / MyBatis-Plus / Nacos）
- **前端**：[vbenjs/vue-vben-admin](https://github.com/vbenjs/vue-vben-admin)（Vue 3 / Vite / Ant Design Vue / TypeScript）
- **产品参照**：MPDV 产品设计（依据用户提供的《MPDV Product Specification》——HYDRA X mApps / FEDRA 2 APS / MIP 平台逐项规格，及《MIP 交流》平台介绍）；车间对象模型参照 MIP ViPR/MBO 思想与 IEC 62264（ISA-95）标准

## 当前状态

✅ **Phase 1（M1~M3）全部完成**（需求文档评审冻结、决策项 Q1~Q7 确认、各切片均经端到端联调验证）

🚧 **Phase 2 进行中**——首个切片 M4（设备维护）已交付代码，待联调

| 里程碑 | 内容 | 状态 |
| --- | --- | --- |
| M1 | 平台骨架（pig/vben）+ 车间建模 + 主数据 | ✅ 完成（已通过端到端联调：登录→权限→建模/主数据全链路 CRUD 与状态机） |
| M2 | 工单 + 排程 + 报工 + 工位终端 | ✅ 完成（工单状态机 / 自研甘特排程（有限产能）/ 不可变报工事件 / 全屏工位终端，均通过 E2E 联调） |
| M3 | 质量 + 追溯 + 看板 + ERP 联调 | ✅ 完成（质量门闭环 / 批次追溯 / 监控墙与 KPI / ERP 工单接收·完工回传·设备事件接入契约，均通过 E2E 联调） |
| M4 | 维护保养与点检（FR-RES-31~33、35、36） | 🚧 代码完成：维护计划（周期+检查表）/ 维护工单状态机（待接单→维修中→待验证→关闭）/ 故障报修 / 点检 NG 自动报修 / 监控墙 MAINT 联动 / MTBF·MTTR·故障 Pareto·保养达成率 |

## 仓库结构

```
xMes/
├── docs/        需求文档（00~12）
├── backend/     后端：pig 4.0 骨架（Spring Boot 4 / Spring Cloud 2025）+ xmes 业务模块
│   ├── pig-*            pig 原生服务（auth/gateway/upms/register/boot...）
│   ├── xmes/
│   │   ├── xmes-core-api        实体与对外 API 定义
│   │   ├── xmes-core-biz        平台/建模/主数据/设备维护服务（端口 4100）
│   │   ├── xmes-execution-*     生产执行服务（工单/排程/报工/终端/分析/ERP）
│   │   └── xmes-quality-*       质量与物料服务（检验/批次/追溯）
│   └── db/
│       ├── pig.sql                  pig 系统表与基础数据（先执行）
│       ├── xmes_core.sql            车间建模表 + 菜单权限
│       ├── xmes_masterdata.sql      物料/BOM/工作计划表 + 菜单权限
│       ├── xmes_execution.sql       工单/作业/报工表 + 菜单权限
│       ├── xmes_quality.sql         质量/批次/追溯表 + 菜单权限
│       └── xmes_maintenance.sql     维护计划/维护工单表 + 菜单权限（Phase 2 M4）
└── frontend/    前端：vue-vben-admin v5 monorepo（保留 apps/web-antd 作为管理端）
    └── apps/web-antd/src/{api,views,router/routes/modules}/mes/...
```

## 本地开发

后端（两种启动方式）：

```bash
cd backend
mvn install -DskipTests -Dspring-javaformat.skip=true
# 方式一：单体模式（开发推荐，已聚合 xmes-core）——先启动 pig-register(Nacos)，再启动 pig-boot
# 方式二：微服务模式——依次启动 pig-register / pig-gateway / pig-auth / pig-upms-biz / xmes-core-biz
# 数据库：执行 db/pig.sql 与 db/xmes_core.sql；Nacos 配置执行 db/pig_config.sql
```

前端：

```bash
cd frontend
pnpm install
pnpm dev:antd        # 管理端，默认 http://localhost:5666
```

> 网关路由：xmes-core 注册名为 `xmes-core-biz`，需在网关路由规则中将 `/mes/**` 转发到该服务（单体模式无需配置）。

### 联调注意事项（已在本仓库验证通过）

- **单体（boot）模式**：`mvn install -Pboot -DskipTests` 后直接 `java -jar pig-boot/target/pig-boot.jar`，无需 Nacos；上下文路径为 `/admin`，MES 接口位于 `/admin/modeling/**`、`/admin/masterdata/**`，前端 vite 代理已按此配置（`/api/mes/** → /admin/**`）
- **MySQL 大小写**：若服务器 `lower_case_table_names=0`（Linux 默认），需将 `qrtz_*` 表重命名为大写 `QRTZ_*`，或启动 MySQL 时加 `--lower-case-table-names=1`（pig 官方 docker-compose 的做法）
- **登录**：开发期前端使用 `test:test` OAuth2 客户端（免图形验证码），密码 AES-128-CFB 加密（密钥=后端 `security.encodeKey`）；生产切换 `pig` 客户端并接入验证码
- **MES 菜单**：占用 `sys_menu` 5000~5499 段（pig 自身占用 4000~4014；5000 建模/主数据、5100 生产执行、5200 质量、5300 物料、5400 设备维护），按钮权限码前缀 `mes_*`

## 需求文档目录

| 编号 | 文档 | 内容 |
| --- | --- | --- |
| 00 | [产品概述](docs/00-产品概述.md) | 背景、目标、用户角色、产品范围、术语表 |
| 01 | [总体架构](docs/01-总体架构.md) | 技术架构、微服务划分、前端工程结构、部署形态 |
| 02 | [车间对象模型](docs/02-车间对象模型.md) | 核心领域模型（工厂/人员/设备/物料/工艺/订单/质量），状态机定义 |
| 03 | [功能需求 · 订单管理](docs/03-功能需求-订单管理.md) | 工单管理、作业排程、生产报工、工单管控 |
| 04 | [功能需求 · 资源管理](docs/04-功能需求-资源管理.md) | 设备管理、数据采集、维护保养、工装模具、NC 程序、能源 |
| 05 | [功能需求 · 物料管理](docs/05-功能需求-物料管理.md) | 车间物流、WIP、批次/序列号、追溯 |
| 06 | [功能需求 · 质量管理](docs/06-功能需求-质量管理.md) | 检验计划与执行、SPC、不合格品、检具管理、投诉管理 |
| 07 | [功能需求 · 人员管理](docs/07-功能需求-人员管理.md) | 工时采集、排班、资质技能 |
| 08 | [功能需求 · 装配与现场执行](docs/08-功能需求-装配与现场执行.md) | 工位终端、作业指导、防错、安灯 |
| 09 | [功能需求 · 信息与分析](docs/09-功能需求-信息与分析.md) | 驾驶舱、看板、KPI/OEE、升级管理、措施管理、文档管理 |
| 10 | [集成需求](docs/10-集成需求.md) | ERP 集成、设备联网（OPC UA/MQTT）、开放 API |
| 11 | [非功能需求](docs/11-非功能需求.md) | 性能、可用性、安全、审计、国际化 |
| 12 | [实施路线图](docs/12-实施路线图.md) | 分期交付计划与里程碑 |

## 阅读建议

1. 先读 **00 产品概述** 了解整体范围；
2. 再读 **02 车间对象模型** —— 它是所有功能模块的共同基础，评审时请重点确认；
3. 各功能需求文档可按业务关注点选读，功能项均有唯一编号（`FR-模块-序号`），便于评审批注；
4. 需求优先级标注：**P0**（首期必须）/ **P1**(二期) / **P2**（远期）。
