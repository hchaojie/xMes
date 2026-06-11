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
];

export default routes;
