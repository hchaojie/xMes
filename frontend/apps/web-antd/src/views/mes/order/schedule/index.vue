<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  InputNumber,
  message,
  Popconfirm,
  Select,
  Space,
  Tag,
} from 'ant-design-vue';

import { requestClient } from '#/api/request';

defineOptions({ name: 'MesOrderSchedule' });

/** ===== 类型 ===== */
interface WorkplaceRow {
  id: string;
  code: string;
  name: string;
}
interface WorkCenterRow {
  id: string;
  code: string;
  name: string;
  workplaces: WorkplaceRow[];
}
interface TaskBar {
  id: string;
  orderId: string;
  orderNo?: string;
  materialCode?: string;
  materialName?: string;
  operationNo: string;
  operationName?: string;
  taskStatus: string;
  workCenterId?: string;
  workplaceId?: string;
  planStart?: string;
  planEnd?: string;
  qtyTarget?: number;
  setupTime?: number;
  unitTime?: number;
  orderPriority?: number;
  orderPlanEndDate?: string;
}
interface BoardData {
  workCenters: WorkCenterRow[];
  tasks: TaskBar[];
  backlog: TaskBar[];
  nonWorkDays: string[];
}

/** ===== 看板状态 ===== */
const ROW_H = 40;
const HEADER_H = 36;
const startDate = ref(new Date().toISOString().slice(0, 10));
const days = ref(7);
const hourPx = ref(12);
const workCenterFilter = ref<string | undefined>();
const board = ref<BoardData | null>(null);
const loading = ref(false);
const selectedIds = ref<Set<string>>(new Set());

const STATUS_COLOR: Record<string, string> = {
  SCHEDULED: '#1677ff',
  DISPATCHED: '#13c2c2',
  RUNNING: '#52c41a',
  PAUSED: '#faad14',
};

const rangeStart = computed(() => new Date(`${startDate.value}T00:00:00`));
const totalWidth = computed(() => days.value * 24 * hourPx.value);

/** 资源行扁平化：工作中心分组行 + 工位行 */
const rows = computed(() => {
  const list: Array<
    | { type: 'wc'; wc: WorkCenterRow }
    | { type: 'wp'; wcId: string; wp: WorkplaceRow }
  > = [];
  for (const wc of board.value?.workCenters ?? []) {
    list.push({ type: 'wc', wc });
    for (const wp of wc.workplaces) list.push({ type: 'wp', wcId: wc.id, wp });
  }
  return list;
});

/** 工位ID → 该行的作业条 */
const barsByWorkplace = computed(() => {
  const map = new Map<string, TaskBar[]>();
  for (const t of board.value?.tasks ?? []) {
    if (!t.workplaceId) continue;
    const arr = map.get(t.workplaceId) ?? [];
    arr.push(t);
    map.set(t.workplaceId, arr);
  }
  return map;
});

const dayList = computed(() => {
  const arr: { date: string; isNonWork: boolean; label: string }[] = [];
  const nonWork = new Set(board.value?.nonWorkDays ?? []);
  for (let i = 0; i < days.value; i++) {
    const d = new Date(rangeStart.value.getTime() + i * 86_400_000);
    const iso = d.toISOString().slice(0, 10);
    arr.push({
      date: iso,
      isNonWork: nonWork.has(iso),
      label: `${d.getMonth() + 1}/${d.getDate()} 周${'日一二三四五六'[d.getDay()]}`,
    });
  }
  return arr;
});

function xOf(time: string) {
  return ((new Date(time).getTime() - rangeStart.value.getTime()) / 3_600_000) * hourPx.value;
}

function barStyle(t: TaskBar) {
  const left = xOf(t.planStart!);
  const width = Math.max(xOf(t.planEnd!) - left, 8);
  const late =
    t.orderPlanEndDate && t.planEnd && t.planEnd.slice(0, 10) > t.orderPlanEndDate;
  return {
    left: `${left}px`,
    width: `${width}px`,
    background: STATUS_COLOR[t.taskStatus] ?? '#999',
    outline: selectedIds.value.has(t.id)
      ? '2px solid #722ed1'
      : late
        ? '2px solid #f5222d'
        : 'none',
  };
}

const nowX = computed(() => {
  const x = (Date.now() - rangeStart.value.getTime()) / 3_600_000 * hourPx.value;
  return x >= 0 && x <= totalWidth.value ? x : -1;
});

/** ===== 数据加载 ===== */
async function load() {
  loading.value = true;
  try {
    const end = new Date(rangeStart.value.getTime() + (days.value - 1) * 86_400_000)
      .toISOString()
      .slice(0, 10);
    board.value = await requestClient.get<BoardData>('/mes/order/schedule/board', {
      params: { startDate: startDate.value, endDate: end, workCenterId: workCenterFilter.value },
    });
    selectedIds.value = new Set();
  } finally {
    loading.value = false;
  }
}

function shiftRange(deltaDays: number) {
  const d = new Date(rangeStart.value.getTime() + deltaDays * 86_400_000);
  startDate.value = d.toISOString().slice(0, 10);
  load();
}

/** ===== 拖拽分派 ===== */
function onDragStart(e: DragEvent, task: TaskBar) {
  e.dataTransfer?.setData('text/task-id', task.id);
  e.dataTransfer!.effectAllowed = 'move';
}

async function onDrop(e: DragEvent, wp: WorkplaceRow) {
  e.preventDefault();
  const taskId = e.dataTransfer?.getData('text/task-id');
  if (!taskId) return;
  const row = e.currentTarget as HTMLElement;
  const x = e.clientX - row.getBoundingClientRect().left;
  // 像素 → 时间，取整到 30 分钟
  const minutes = Math.round(((x / hourPx.value) * 60) / 30) * 30;
  const start = new Date(rangeStart.value.getTime() + minutes * 60_000);
  const pad = (n: number) => String(n).padStart(2, '0');
  // pig 全局 Jackson 时间格式：yyyy-MM-dd HH:mm:ss（空格分隔）
  const planStart = `${start.getFullYear()}-${pad(start.getMonth() + 1)}-${pad(start.getDate())} ${pad(start.getHours())}:${pad(start.getMinutes())}:00`;
  try {
    await requestClient.put('/mes/order/schedule/assign', {
      taskId,
      workplaceId: wp.id,
      planStart,
    });
    message.success(`已排到 ${wp.code} @ ${planStart.slice(0, 16)}`);
  } finally {
    await load();
  }
}

/** ===== 条操作 ===== */
function toggleSelect(t: TaskBar) {
  const next = new Set(selectedIds.value);
  if (next.has(t.id)) {
    next.delete(t.id);
  } else {
    next.add(t.id);
  }
  selectedIds.value = next;
}

async function handleUnassign(t: TaskBar) {
  await requestClient.put(`/mes/order/schedule/unassign/${t.id}`);
  message.success('已取消排程，作业回到待排清单');
  await load();
}

async function handleDispatch() {
  if (selectedIds.value.size === 0) {
    message.warning('请先点击选中要派工的作业条（已排程状态）');
    return;
  }
  await requestClient.put('/mes/order/schedule/dispatch', [...selectedIds.value]);
  message.success(`已派工 ${selectedIds.value.size} 个作业`);
  await load();
}

const workCenterOptions = computed(() =>
  (board.value?.workCenters ?? []).map((c) => ({
    value: c.id,
    label: `${c.code} ${c.name}`,
  })),
);

onMounted(load);
</script>

<template>
  <Page title="作业排程" description="自研甘特看板：从待排清单拖拽作业到工位行分派（有限产能，时间重叠拒绝）；点击作业条选中后批量派工">
    <Card :loading="loading">
      <template #extra>
        <Space>
          <Button size="small" @click="shiftRange(-7)">◀ 前一周</Button>
          <Button size="small" @click="shiftRange(7)">后一周 ▶</Button>
          <Select
            v-model:value="workCenterFilter"
            style="width: 180px"
            size="small"
            placeholder="工作中心过滤"
            allow-clear
            :options="workCenterOptions"
            @change="load"
          />
          <span>缩放</span>
          <InputNumber v-model:value="hourPx" :min="4" :max="60" size="small" style="width: 70px" @change="load" />
          <Button size="small" @click="load">刷新</Button>
          <Button size="small" type="primary" @click="handleDispatch">
            派工选中({{ selectedIds.size }})
          </Button>
        </Space>
      </template>

      <!-- 甘特主体 -->
      <div class="flex border" style="max-height: 520px; overflow: auto">
        <!-- 左侧资源列 -->
        <div class="shrink-0 border-r bg-gray-50" style="width: 190px">
          <div
            class="flex items-center border-b px-2 font-bold"
            :style="{ height: `${HEADER_H}px`, position: 'sticky', top: 0, background: '#fafafa', zIndex: 3 }"
          >
            资源（工作中心/工位）
          </div>
          <div
            v-for="row in rows"
            :key="row.type === 'wc' ? `wc-${row.wc.id}` : `wp-${row.wp.id}`"
            class="flex items-center border-b px-2"
            :style="{ height: `${ROW_H}px` }"
          >
            <strong v-if="row.type === 'wc'">{{ row.wc.code }} {{ row.wc.name }}</strong>
            <span v-else class="pl-4 text-sm">{{ row.wp.code }} {{ row.wp.name }}</span>
          </div>
        </div>

        <!-- 右侧时间网格 -->
        <div class="relative" :style="{ width: `${totalWidth}px`, flexShrink: 0 }">
          <!-- 日期表头 -->
          <div
            class="flex border-b"
            :style="{ height: `${HEADER_H}px`, position: 'sticky', top: 0, background: '#fff', zIndex: 2 }"
          >
            <div
              v-for="d in dayList"
              :key="d.date"
              class="border-r text-center text-xs leading-9"
              :style="{ width: `${24 * hourPx}px`, background: d.isNonWork ? '#f0f0f0' : undefined }"
            >
              {{ d.label }}{{ d.isNonWork ? '（休）' : '' }}
            </div>
          </div>

          <!-- 行 -->
          <div class="relative">
            <!-- 非工作日底色（覆盖所有行） -->
            <div
              v-for="(d, i) in dayList"
              :key="`bg-${d.date}`"
              v-show="d.isNonWork"
              class="pointer-events-none absolute top-0 h-full"
              :style="{ left: `${i * 24 * hourPx}px`, width: `${24 * hourPx}px`, background: 'rgba(0,0,0,0.05)' }"
            ></div>
            <!-- 当前时间线 -->
            <div
              v-if="nowX >= 0"
              class="pointer-events-none absolute top-0 h-full"
              :style="{ left: `${nowX}px`, width: '2px', background: '#f5222d', zIndex: 1 }"
            ></div>

            <div
              v-for="row in rows"
              :key="row.type === 'wc' ? `gwc-${row.wc.id}` : `gwp-${row.wp.id}`"
              class="relative border-b"
              :style="{
                height: `${ROW_H}px`,
                background: row.type === 'wc' ? '#fafafa' : undefined,
              }"
              @dragover.prevent
              @drop="row.type === 'wp' && onDrop($event, row.wp)"
            >
              <template v-if="row.type === 'wp'">
                <div
                  v-for="t in barsByWorkplace.get(row.wp.id) ?? []"
                  :key="t.id"
                  class="absolute cursor-pointer truncate rounded px-1 text-xs text-white"
                  :style="{ ...barStyle(t), top: '6px', height: '28px', lineHeight: '28px' }"
                  :draggable="['DISPATCHED', 'SCHEDULED'].includes(t.taskStatus)"
                  :title="`${t.orderNo} ${t.materialCode ?? ''} 工序${t.operationNo} ${t.operationName ?? ''}\n${t.planStart} ~ ${t.planEnd}\n状态:${t.taskStatus} 数量:${t.qtyTarget}`"
                  @dragstart="onDragStart($event, t)"
                  @click="toggleSelect(t)"
                  @dblclick.prevent="handleUnassign(t)"
                >
                  {{ t.orderNo }}·{{ t.operationNo }} {{ t.operationName }}
                </div>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- 图例 -->
      <Space class="mt-2 text-xs">
        <Tag color="#1677ff">已排程</Tag>
        <Tag color="#13c2c2">已派工</Tag>
        <Tag color="#52c41a">进行中</Tag>
        <Tag color="#faad14">暂停</Tag>
        <span>红边=超工单交期 · 紫边=选中 · 双击作业条=取消排程 · 拖动条=改期/换工位</span>
      </Space>
    </Card>

    <!-- 待排清单 -->
    <Card class="mt-4" :title="`待排清单（${board?.backlog?.length ?? 0}）——拖拽到上方工位行`" size="small">
      <div class="flex flex-wrap gap-2">
        <div
          v-for="t in board?.backlog ?? []"
          :key="t.id"
          class="cursor-grab rounded border border-dashed px-2 py-1 text-xs"
          draggable="true"
          :title="`准备${t.setupTime}分 + ${t.qtyTarget}×${t.unitTime}分/件`"
          @dragstart="onDragStart($event, t)"
        >
          <strong>{{ t.orderNo }}</strong> · {{ t.operationNo }} {{ t.operationName }}
          <Tag v-if="(t.orderPriority ?? 0) > 0" color="orange" class="ml-1">P{{ t.orderPriority }}</Tag>
          <div class="text-gray-500">{{ t.materialCode }} ×{{ t.qtyTarget }}</div>
        </div>
        <span v-if="!board?.backlog?.length" class="text-gray-400">无待排作业（工单下达后其作业出现在此处）</span>
      </div>
    </Card>

    <Popconfirm v-if="false" title="" />
  </Page>
</template>
