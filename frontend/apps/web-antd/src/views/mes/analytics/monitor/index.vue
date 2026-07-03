<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Input,
  message,
  Space,
  Statistic,
  Table,
  Tag,
} from 'ant-design-vue';

import { requestClient } from '#/api/request';

defineOptions({ name: 'MesAnalyticsMonitor' });

interface WorkplaceCard {
  workplaceId: string;
  code: string;
  name: string;
  state: 'IDLE' | 'MAINT' | 'PAUSED' | 'RUNNING' | 'WAITING';
  orderNo?: string;
  operationNo?: string;
  operationName?: string;
  materialCode?: string;
  progress?: string;
  todayGood: number;
  todayScrap: number;
}

const STATE_META: Record<string, { bg: string; label: string }> = {
  RUNNING: { label: '运行', bg: '#52c41a' },
  PAUSED: { label: '暂停', bg: '#faad14' },
  WAITING: { label: '待机', bg: '#13c2c2' },
  MAINT: { label: '维修', bg: '#f5222d' },
  IDLE: { label: '空闲', bg: '#bfbfbf' },
};

const board = ref<WorkplaceCard[]>([]);
const kpi = ref<null | Record<string, any>>(null);
const pareto = ref<Record<string, any>[]>([]);
const loading = ref(false);

const today = new Date().toISOString().slice(0, 10);
const range = reactive({ startDate: today, endDate: today });

const kpiColumns = [
  { title: '工位', dataIndex: 'workplace' },
  { title: '良品', dataIndex: 'good', width: 100 },
  { title: '报废', dataIndex: 'scrap', width: 100 },
  { title: '返工', dataIndex: 'rework', width: 100 },
  { title: '一次合格率', dataIndex: 'fpy', width: 110 },
  { title: '生产时长(分)', dataIndex: 'runMinutes', width: 120 },
];

const maxParetoQty = computed(() =>
  Math.max(1, ...pareto.value.map((p) => Number(p.qty))),
);

async function load() {
  loading.value = true;
  try {
    const [b, k, p] = await Promise.all([
      requestClient.get<WorkplaceCard[]>('/mes/order/analytics/workplace-board'),
      requestClient.get<Record<string, any>>('/mes/order/analytics/kpi', {
        params: range,
      }),
      requestClient.get<Record<string, any>[]>(
        '/mes/order/analytics/scrap-pareto',
        { params: range },
      ),
    ]);
    board.value = b;
    kpi.value = k;
    pareto.value = p;
  } finally {
    loading.value = false;
  }
}

let timer: ReturnType<typeof setInterval> | undefined;
onMounted(() => {
  load();
  // 监控页 30s 自动刷新
  timer = setInterval(load, 30_000);
});
onBeforeUnmount(() => timer && clearInterval(timer));

async function applyRange() {
  if (!range.startDate || !range.endDate) {
    message.warning('请填写日期范围');
    return;
  }
  await load();
}
</script>

<template>
  <Page title="生产监控" description="设备监控墙（30 秒自刷新）+ 基础 KPI（事件流口径：产量/一次合格率/生产时长）+ 报废 Pareto">
    <!-- 监控墙 -->
    <Card title="设备监控墙" :loading="loading" class="mb-4">
      <template #extra>
        <Space>
          <Tag v-for="(m, k) in STATE_META" :key="k" :color="m.bg">{{ m.label }}</Tag>
          <Button size="small" @click="load">刷新</Button>
        </Space>
      </template>
      <div class="grid grid-cols-2 gap-3 md:grid-cols-3 lg:grid-cols-4">
        <div
          v-for="wp in board"
          :key="wp.workplaceId"
          class="rounded-lg p-4 text-white shadow"
          :style="{ background: STATE_META[wp.state]?.bg }"
        >
          <div class="flex items-center justify-between">
            <span class="text-lg font-bold">{{ wp.code }}</span>
            <span class="text-sm">{{ STATE_META[wp.state]?.label }}</span>
          </div>
          <div class="text-sm opacity-90">{{ wp.name }}</div>
          <div v-if="wp.orderNo" class="mt-2 text-sm">
            {{ wp.orderNo }} · {{ wp.operationNo }} {{ wp.operationName }}
            <div>{{ wp.materialCode }} · 进度 {{ wp.progress }}</div>
          </div>
          <div v-else class="mt-2 text-sm opacity-80">无任务</div>
          <div class="mt-2 border-t border-white/40 pt-1 text-sm">
            今日 良 {{ wp.todayGood }} / 废 {{ wp.todayScrap }}
          </div>
        </div>
        <div v-if="board.length === 0" class="col-span-4 py-8 text-center text-gray-400">
          暂无启用的工位
        </div>
      </div>
    </Card>

    <!-- KPI -->
    <Card class="mb-4">
      <template #title>
        <Space>
          <span>生产 KPI</span>
          <Input v-model:value="range.startDate" style="width: 130px" placeholder="YYYY-MM-DD" />
          <span>~</span>
          <Input v-model:value="range.endDate" style="width: 130px" placeholder="YYYY-MM-DD" />
          <Button size="small" type="primary" @click="applyRange">查询</Button>
        </Space>
      </template>
      <div v-if="kpi" class="mb-4 grid grid-cols-4 gap-4">
        <Card size="small"><Statistic title="良品总数" :value="kpi.totalGood" /></Card>
        <Card size="small"><Statistic title="报废总数" :value="kpi.totalScrap" /></Card>
        <Card size="small"><Statistic title="一次合格率" :value="kpi.fpy" /></Card>
        <Card size="small">
          <Statistic title="生产时长(分钟)" :value="kpi.totalRunMinutes" />
        </Card>
      </div>
      <Table
        :columns="kpiColumns"
        :data-source="kpi?.rows ?? []"
        :pagination="false"
        row-key="workplace"
        size="small"
      />
    </Card>

    <!-- 报废 Pareto -->
    <Card title="报废原因 Pareto">
      <div v-if="pareto.length === 0" class="py-6 text-center text-gray-400">
        区间内无报废记录
      </div>
      <div v-else class="space-y-2">
        <div v-for="p in pareto" :key="p.reason" class="flex items-center gap-3">
          <span class="w-32 truncate text-right">{{ p.reason }}</span>
          <div class="h-6 rounded bg-red-400" :style="{ width: `${(Number(p.qty) / maxParetoQty) * 70}%`, minWidth: '8px' }"></div>
          <span class="font-bold">{{ p.qty }}</span>
        </div>
      </div>
    </Card>
  </Page>
</template>
