import type { RouteRecordRaw } from 'vue-router';

const routes: RouteRecordRaw[] = [
  {
    meta: {
      icon: 'lucide:factory',
      order: 10,
      title: '车间建模',
    },
    name: 'MesModeling',
    path: '/mes/modeling',
    children: [
      {
        name: 'MesModelingFactory',
        path: 'factory',
        component: () => import('#/views/mes/modeling/factory/index.vue'),
        meta: {
          icon: 'lucide:network',
          title: '工厂结构',
        },
      },
      {
        name: 'MesModelingShift',
        path: 'shift',
        component: () => import('#/views/mes/modeling/shift/index.vue'),
        meta: {
          icon: 'lucide:clock',
          title: '班次模型',
        },
      },
      {
        name: 'MesModelingCalendar',
        path: 'calendar',
        component: () => import('#/views/mes/modeling/calendar/index.vue'),
        meta: {
          icon: 'lucide:calendar-days',
          title: '工厂日历',
        },
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:database',
      order: 11,
      title: '制造主数据',
    },
    name: 'MesMasterdata',
    path: '/mes/masterdata',
    children: [
      {
        name: 'MesMasterdataMaterial',
        path: 'material',
        component: () => import('#/views/mes/masterdata/material/index.vue'),
        meta: {
          icon: 'lucide:package',
          title: '物料管理',
        },
      },
      {
        name: 'MesMasterdataBom',
        path: 'bom',
        component: () => import('#/views/mes/masterdata/bom/index.vue'),
        meta: {
          icon: 'lucide:list-tree',
          title: 'BOM 管理',
        },
      },
      {
        name: 'MesMasterdataRouting',
        path: 'routing',
        component: () => import('#/views/mes/masterdata/routing/index.vue'),
        meta: {
          icon: 'lucide:route',
          title: '工作计划',
        },
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:clipboard-list',
      order: 12,
      title: '生产执行',
    },
    name: 'MesOrder',
    path: '/mes/order',
    children: [
      {
        name: 'MesOrderWorkorder',
        path: 'workorder',
        component: () => import('#/views/mes/order/workorder/index.vue'),
        meta: {
          icon: 'lucide:file-cog',
          title: '工单管理',
        },
      },
      {
        name: 'MesOrderSchedule',
        path: 'schedule',
        component: () => import('#/views/mes/order/schedule/index.vue'),
        meta: {
          icon: 'lucide:gantt-chart',
          title: '作业排程',
        },
      },
      {
        name: 'MesAnalyticsMonitor',
        path: 'monitor',
        component: () => import('#/views/mes/analytics/monitor/index.vue'),
        meta: {
          icon: 'lucide:monitor-dot',
          title: '生产监控',
        },
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:badge-check',
      order: 13,
      title: '质量管理',
    },
    name: 'MesQuality',
    path: '/mes/quality',
    children: [
      {
        name: 'MesQualityPlan',
        path: 'plan',
        component: () => import('#/views/mes/quality/plan/index.vue'),
        meta: {
          icon: 'lucide:clipboard-check',
          title: '检验计划',
        },
      },
      {
        name: 'MesQualityInspection',
        path: 'inspection',
        component: () => import('#/views/mes/quality/inspection/index.vue'),
        meta: {
          icon: 'lucide:search-check',
          title: '检验工作台',
        },
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:boxes',
      order: 14,
      title: '物料管理',
    },
    name: 'MesMaterial',
    path: '/mes/material',
    children: [
      {
        name: 'MesMaterialLot',
        path: 'lot',
        component: () => import('#/views/mes/material/lot/index.vue'),
        meta: {
          icon: 'lucide:package-search',
          title: '批次台账',
        },
      },
      {
        name: 'MesMaterialTrace',
        path: 'trace',
        component: () => import('#/views/mes/material/trace/index.vue'),
        meta: {
          icon: 'lucide:git-branch',
          title: '批次追溯',
        },
      },
    ],
  },
  {
    meta: {
      icon: 'lucide:wrench',
      order: 15,
      title: '设备维护',
    },
    name: 'MesMaintenance',
    path: '/mes/maintenance',
    children: [
      {
        name: 'MesMaintenancePlan',
        path: 'plan',
        component: () => import('#/views/mes/maintenance/plan/index.vue'),
        meta: {
          icon: 'lucide:calendar-check',
          title: '维护计划',
        },
      },
      {
        name: 'MesMaintenanceOrder',
        path: 'order',
        component: () => import('#/views/mes/maintenance/order/index.vue'),
        meta: {
          icon: 'lucide:hammer',
          title: '维护工单',
        },
      },
      {
        name: 'MesMaintenanceAnalysis',
        path: 'analysis',
        component: () => import('#/views/mes/maintenance/analysis/index.vue'),
        meta: {
          icon: 'lucide:activity',
          title: '维护分析',
        },
      },
    ],
  },
];

export default routes;
