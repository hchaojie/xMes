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
];

export default routes;
