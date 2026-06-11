<script setup lang="ts">
import type { BookingQty } from '#/api/mes/order';

import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue';

import {
  Button,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Tag,
} from 'ant-design-vue';

import {
  finishTask,
  pauseTask,
  reportTaskQty,
  resumeTask,
  startTask,
} from '#/api/mes/order';
import { requestClient } from '#/api/request';

defineOptions({ name: 'MesTerminal' });

/** 终端报工来源标识 */
const SOURCE = 'TERMINAL';
const WP_KEY = 'xmes.terminal.workplaceId';

interface QueueTask {
  id: string;
  orderNo?: string;
  materialCode?: string;
  materialName?: string;
  operationNo: string;
  operationName?: string;
  taskStatus: string;
  planStart?: string;
  planEnd?: string;
  qtyTarget?: number;
  qtyGood?: number;
  qtyScrap?: number;
  orderPriority?: number;
}

const STATUS_META: Record<string, { color: string; label: string }> = {
  SCHEDULED: { label: '已排程', color: '#1677ff' },
  DISPATCHED: { label: '待开工', color: '#13c2c2' },
  RUNNING: { label: '进行中', color: '#52c41a' },
  PAUSED: { label: '暂停', color: '#faad14' },
};

/** 工位绑定 */
const workplaceId = ref<string | undefined>(
  localStorage.getItem(WP_KEY) ?? undefined,
);
const workplaceOptions = ref<{ label: string; value: string }[]>([]);
const workplaceLabel = computed(
  () =>
    workplaceOptions.value.find((o) => o.value === workplaceId.value)?.label ??
    '未绑定工位',
);

async function loadWorkplaces() {
  const list = await requestClient.get<any[]>('/mes/modeling/workplace/list');
  workplaceOptions.value = list.map((w) => ({
    value: w.id,
    label: `${w.workplaceCode} ${w.workplaceName}`,
  }));
}

function bindWorkplace(id: any) {
  workplaceId.value = id;
  localStorage.setItem(WP_KEY, id);
  loadQueue();
}

/** 任务队列 */
const queue = ref<QueueTask[]>([]);
const currentId = ref<string | undefined>();
const current = computed(() => queue.value.find((t) => t.id === currentId.value));
const loading = ref(false);
let timer: ReturnType<typeof setInterval> | undefined;

async function loadQueue() {
  if (!workplaceId.value) return;
  loading.value = true;
  try {
    queue.value = await requestClient.get<QueueTask[]>(
      `/mes/order/terminal/queue/${workplaceId.value}`,
    );
    if (!current.value && queue.value.length > 0) {
      currentId.value = queue.value[0]!.id;
    }
  } finally {
    loading.value = false;
  }
}

/** 报工操作（大按钮） */
async function doStart() {
  await startTask(current.value!.id, workplaceId.value, SOURCE);
  message.success('已开工');
  await loadQueue();
}

const pauseOpen = ref(false);
const pauseReason = ref('');
async function doPause() {
  if (!pauseReason.value) {
    message.warning('请选择暂停原因');
    return;
  }
  await pauseTask(current.value!.id, pauseReason.value, SOURCE);
  message.success('已暂停');
  pauseOpen.value = false;
  await loadQueue();
}

async function doResume() {
  await resumeTask(current.value!.id, SOURCE);
  message.success('已恢复');
  await loadQueue();
}

/** 报数/完工 */
const qtyOpen = ref(false);
const qtyMode = ref<'finish' | 'qty'>('qty');
const qtyForm = reactive<BookingQty>({ taskId: '' });

function openQty(mode: 'finish' | 'qty') {
  qtyMode.value = mode;
  Object.assign(qtyForm, {
    taskId: current.value!.id,
    qtyGood: undefined,
    qtyScrap: undefined,
    qtyRework: undefined,
    reasonCode: undefined,
    workplaceId: workplaceId.value,
    source: SOURCE,
  });
  qtyOpen.value = true;
}

async function submitQty() {
  if ((qtyForm.qtyScrap ?? 0) > 0 && !qtyForm.reasonCode) {
    message.warning('报废必须选择原因');
    return;
  }
  await (qtyMode.value === 'qty'
    ? reportTaskQty({ ...qtyForm, source: SOURCE })
    : finishTask({ ...qtyForm, source: SOURCE }));
  message.success(qtyMode.value === 'qty' ? '报数成功' : '已完工');
  qtyOpen.value = false;
  await loadQueue();
}

const clock = ref('');
onMounted(async () => {
  await loadWorkplaces();
  await loadQueue();
  timer = setInterval(() => {
    clock.value = new Date().toLocaleString('zh-CN', { hour12: false });
    // 30s 轮询刷新队列
    if (Date.now() % 30_000 < 1000) loadQueue();
  }, 1000);
});
onBeforeUnmount(() => timer && clearInterval(timer));
</script>

<template>
  <div class="flex h-screen flex-col bg-slate-100">
    <!-- 顶栏 -->
    <header class="flex items-center justify-between bg-slate-800 px-6 py-3 text-white">
      <div class="text-2xl font-bold">xMes 工位终端</div>
      <div class="flex items-center gap-4">
        <span class="text-lg">{{ workplaceLabel }}</span>
        <Select
          :value="workplaceId"
          style="width: 220px"
          placeholder="选择/切换工位"
          show-search
          option-filter-prop="label"
          :options="workplaceOptions"
          @change="bindWorkplace"
        />
        <span class="text-lg tabular-nums">{{ clock }}</span>
      </div>
    </header>

    <main v-if="workplaceId" class="flex flex-1 gap-4 overflow-hidden p-4">
      <!-- 左：任务队列 -->
      <section class="w-2/5 overflow-y-auto">
        <div class="mb-2 flex items-center justify-between">
          <h2 class="text-xl font-bold">任务队列（{{ queue.length }}）</h2>
          <Button size="large" :loading="loading" @click="loadQueue">刷新</Button>
        </div>
        <div
          v-for="t in queue"
          :key="t.id"
          class="mb-3 cursor-pointer rounded-lg border-l-8 bg-white p-4 shadow"
          :style="{
            borderLeftColor: STATUS_META[t.taskStatus]?.color ?? '#999',
            outline: t.id === currentId ? '3px solid #1677ff' : 'none',
          }"
          @click="currentId = t.id"
        >
          <div class="flex items-center justify-between">
            <span class="text-lg font-bold">{{ t.orderNo }}</span>
            <Tag :color="STATUS_META[t.taskStatus]?.color">
              {{ STATUS_META[t.taskStatus]?.label ?? t.taskStatus }}
            </Tag>
          </div>
          <div class="mt-1 text-base">
            工序 {{ t.operationNo }} {{ t.operationName }} ·
            {{ t.materialCode }} {{ t.materialName }}
          </div>
          <div class="mt-1 text-sm text-gray-500">
            进度 {{ t.qtyGood ?? 0 }}/{{ t.qtyTarget }}
            <span v-if="t.planStart"> · 计划 {{ t.planStart.slice(5, 16) }}</span>
          </div>
        </div>
        <div v-if="queue.length === 0" class="mt-12 text-center text-xl text-gray-400">
          暂无派工任务
        </div>
      </section>

      <!-- 右：当前作业操作面板 -->
      <section class="flex-1 rounded-lg bg-white p-6 shadow">
        <template v-if="current">
          <div class="mb-2 text-3xl font-bold">
            {{ current.orderNo }} · 工序 {{ current.operationNo }}
            {{ current.operationName }}
          </div>
          <div class="mb-6 text-xl text-gray-600">
            {{ current.materialCode }} {{ current.materialName }} ·
            应做 {{ current.qtyTarget }} · 已报良品 {{ current.qtyGood ?? 0 }}
            · 报废 {{ current.qtyScrap ?? 0 }}
          </div>

          <div class="grid grid-cols-2 gap-4">
            <Button
              v-if="['DISPATCHED', 'SCHEDULED'].includes(current.taskStatus)"
              type="primary"
              style="height: 90px; font-size: 28px"
              @click="doStart"
            >
              ▶ 开工
            </Button>
            <Button
              v-if="current.taskStatus === 'RUNNING'"
              style="height: 90px; font-size: 28px"
              @click="pauseOpen = true"
            >
              ⏸ 暂停
            </Button>
            <Button
              v-if="current.taskStatus === 'PAUSED'"
              type="primary"
              style="height: 90px; font-size: 28px"
              @click="doResume"
            >
              ▶ 恢复
            </Button>
            <Button
              v-if="current.taskStatus === 'RUNNING'"
              type="primary"
              ghost
              style="height: 90px; font-size: 28px"
              @click="openQty('qty')"
            >
              ✎ 报数
            </Button>
            <Button
              v-if="['PAUSED', 'RUNNING'].includes(current.taskStatus)"
              danger
              style="height: 90px; font-size: 28px"
              @click="openQty('finish')"
            >
              ✔ 完工
            </Button>
          </div>
        </template>
        <div v-else class="mt-24 text-center text-2xl text-gray-400">
          请在左侧选择任务
        </div>
      </section>
    </main>

    <main v-else class="flex flex-1 items-center justify-center">
      <div class="text-center">
        <div class="mb-4 text-3xl">请先绑定本终端的工位</div>
        <Select
          style="width: 320px"
          size="large"
          placeholder="选择工位"
          show-search
          option-filter-prop="label"
          :options="workplaceOptions"
          @change="bindWorkplace"
        />
      </div>
    </main>

    <!-- 暂停原因 -->
    <Modal v-model:open="pauseOpen" title="选择暂停原因" :footer="null">
      <div class="grid grid-cols-2 gap-3 p-2">
        <Button
          v-for="r in ['缺料', '换刀/换模', '设备异常', '质检等待', '换班', '其他']"
          :key="r"
          style="height: 60px; font-size: 20px"
          @click="
            pauseReason = r;
            doPause();
          "
        >
          {{ r }}
        </Button>
      </div>
    </Modal>

    <!-- 报数/完工 -->
    <Modal
      v-model:open="qtyOpen"
      :title="qtyMode === 'qty' ? '报数' : '完工（可带最后一笔数量）'"
      width="560px"
      @ok="submitQty"
    >
      <div class="space-y-4 py-2 text-lg">
        <div class="flex items-center gap-3">
          <span class="w-24">良品</span>
          <InputNumber v-model:value="qtyForm.qtyGood" :min="0" size="large" style="width: 200px" />
        </div>
        <div class="flex items-center gap-3">
          <span class="w-24">报废</span>
          <InputNumber v-model:value="qtyForm.qtyScrap" :min="0" size="large" style="width: 200px" />
          <Select
            v-model:value="qtyForm.reasonCode"
            size="large"
            style="width: 180px"
            placeholder="报废原因"
            allow-clear
            :options="
              ['尺寸超差', '表面划伤', '材料缺陷', '装配不良', '其他'].map((r) => ({
                value: r,
                label: r,
              }))
            "
          />
        </div>
        <div class="flex items-center gap-3">
          <span class="w-24">返工</span>
          <InputNumber v-model:value="qtyForm.qtyRework" :min="0" size="large" style="width: 200px" />
        </div>
        <div class="flex items-center gap-3">
          <span class="w-24">备注</span>
          <Input v-model:value="qtyForm.remark" size="large" placeholder="备注" />
        </div>
      </div>
    </Modal>
  </div>
</template>
