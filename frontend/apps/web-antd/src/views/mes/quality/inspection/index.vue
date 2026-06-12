<script setup lang="ts">
import type {
  QcInspectionOrder,
  QcInspectionResult,
} from '#/api/mes/quality';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Drawer,
  Input,
  InputNumber,
  message,
  Modal,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  getInspectionDetail,
  getInspectionPage,
  judgeInspection,
  receiveInspection,
  recordResults,
  refreshGateOrders,
} from '#/api/mes/quality';

defineOptions({ name: 'MesQualityInspection' });

const STATUS_META: Record<string, { color: string; label: string }> = {
  PENDING: { label: '待检', color: 'default' },
  INSPECTING: { label: '检验中', color: 'processing' },
  PASSED: { label: '合格', color: 'success' },
  CONCESSION: { label: '让步接收', color: 'warning' },
  REJECTED: { label: '不合格', color: 'error' },
};

const loading = ref(false);
const dataSource = ref<QcInspectionOrder[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const statusFilter = ref<string | undefined>();

const columns = [
  { title: '检验单号', dataIndex: 'inspectionNo', width: 140 },
  { title: '工单号', dataIndex: 'orderNo', width: 140 },
  { title: '物料', dataIndex: 'materialCode', width: 180 },
  { title: '工序', dataIndex: 'operationNo', width: 70 },
  { title: '抽样数', dataIndex: 'sampleQty', width: 80 },
  { title: '状态', dataIndex: 'inspectStatus', width: 100 },
  { title: '检验员', dataIndex: 'inspector', width: 90 },
  { title: '操作', key: 'action', width: 100 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getInspectionPage({
      current: pagination.current,
      size: pagination.pageSize,
      status: statusFilter.value,
    });
    dataSource.value = res.records;
    pagination.total = Number(res.total);
  } finally {
    loading.value = false;
  }
}

function onTableChange(p: any) {
  pagination.current = p.current;
  pagination.pageSize = p.pageSize;
  loadPage();
}

async function handleRefreshGate() {
  const created = await refreshGateOrders();
  message.success(`扫描完成，新生成 ${created} 张质量门检验单`);
  await loadPage();
}

/** 详情抽屉与录入 */
const drawerOpen = ref(false);
const detail = ref<null | QcInspectionOrder>(null);

/** 录入行：每特性一行（单样本模式，多样本可重复录入） */
const entryRows = ref<QcInspectionResult[]>([]);

const resultColumns = [
  { title: '特性', dataIndex: 'charName', width: 130 },
  { title: '规格', key: 'spec', width: 150 },
  { title: '实测值', dataIndex: 'measuredValue', width: 130 },
  { title: '判定', dataIndex: 'judge', width: 110 },
];

const recordedColumns = [
  { title: '特性', dataIndex: 'charName', width: 130 },
  { title: '样本', dataIndex: 'sampleNo', width: 60 },
  { title: '实测值', dataIndex: 'measuredValue', width: 100 },
  { title: '判定', dataIndex: 'judge', width: 70 },
];

const canInspect = computed(() => detail.value?.inspectStatus === 'INSPECTING');

async function openDetail(row: QcInspectionOrder) {
  detail.value = await getInspectionDetail(row.id);
  const nextSample =
    Math.max(0, ...(detail.value.results ?? []).map((r) => r.sampleNo ?? 0)) + 1;
  entryRows.value = (detail.value.characteristics ?? []).map((c) => ({
    inspectionOrderId: detail.value!.id,
    characteristicId: c.id!,
    charName: c.charName,
    charType: c.charType,
    targetValue: c.targetValue,
    upperLimit: c.upperLimit,
    lowerLimit: c.lowerLimit,
    sampleNo: nextSample,
    measuredValue: undefined,
    judge: undefined,
  }));
  drawerOpen.value = true;
}

async function handleReceive() {
  await receiveInspection(detail.value!.id);
  message.success('已领取，开始检验');
  await openDetail(detail.value!);
}

async function handleRecord() {
  const rows = entryRows.value.filter(
    (r) =>
      (r.charType === 'QUANT' && r.measuredValue !== undefined) ||
      (r.charType !== 'QUANT' && r.judge),
  );
  if (rows.length === 0) {
    message.warning('请至少录入一项结果（计量填实测值，目视/计数选判定）');
    return;
  }
  await recordResults(rows);
  message.success(`已录入 ${rows.length} 项（计量项按公差自动判定）`);
  await openDetail(detail.value!);
}

const judgeRemark = ref('');
async function handleJudge(judge: string) {
  if (judge !== 'PASSED' && !judgeRemark.value) {
    message.warning('让步/不合格判定必须填写说明');
    return;
  }
  await judgeInspection(detail.value!.id, judge, judgeRemark.value || undefined);
  message.success('判定完成');
  drawerOpen.value = false;
  await loadPage();
}

onMounted(loadPage);
</script>

<template>
  <Page title="检验工作台" description="质量门完工自动生成检验单；领取 → 录入（计量项自动判定）→ 用途判定（合格/让步/不合格）；未放行时后道工序被拦截">
    <Card>
      <template #extra>
        <Space>
          <Select
            v-model:value="statusFilter"
            style="width: 130px"
            placeholder="状态"
            allow-clear
            :options="
              Object.entries(STATUS_META).map(([value, m]) => ({
                value,
                label: m.label,
              }))
            "
            @change="loadPage"
          />
          <Button @click="loadPage">查询</Button>
          <Button type="primary" @click="handleRefreshGate">刷新质量门检验单</Button>
        </Space>
      </template>
      <Table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        size="middle"
        @change="onTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'materialCode'">
            {{ record.materialCode }} {{ record.materialName }}
          </template>
          <template v-else-if="column.dataIndex === 'inspectStatus'">
            <Tag :color="STATUS_META[record.inspectStatus]?.color">
              {{ STATUS_META[record.inspectStatus]?.label ?? record.inspectStatus }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Button size="small" type="link" @click="openDetail(record as any)">
              {{ ['INSPECTING', 'PENDING'].includes(record.inspectStatus) ? '检验' : '查看' }}
            </Button>
          </template>
        </template>
      </Table>
    </Card>

    <Drawer
      v-model:open="drawerOpen"
      :title="`检验单 ${detail?.inspectionNo ?? ''}`"
      width="880"
    >
      <Descriptions v-if="detail" bordered size="small" :column="3" class="mb-4">
        <DescriptionsItem label="工单">{{ detail.orderNo }}</DescriptionsItem>
        <DescriptionsItem label="物料">
          {{ detail.materialCode }} {{ detail.materialName }}
        </DescriptionsItem>
        <DescriptionsItem label="工序">{{ detail.operationNo }}</DescriptionsItem>
        <DescriptionsItem label="状态">
          <Tag :color="STATUS_META[detail.inspectStatus]?.color">
            {{ STATUS_META[detail.inspectStatus]?.label }}
          </Tag>
        </DescriptionsItem>
        <DescriptionsItem label="抽样数">{{ detail.sampleQty }}</DescriptionsItem>
        <DescriptionsItem label="检验员">{{ detail.inspector ?? '-' }}</DescriptionsItem>
        <DescriptionsItem v-if="detail.judgeRemark" label="判定说明" :span="3">
          {{ detail.judgeRemark }}
        </DescriptionsItem>
      </Descriptions>

      <Button
        v-if="detail?.inspectStatus === 'PENDING'"
        type="primary"
        block
        class="mb-4"
        @click="handleReceive"
      >
        领取检验单（开始检验）
      </Button>

      <template v-if="canInspect">
        <h3 class="mb-2 font-bold">结果录入（样本 #{{ entryRows[0]?.sampleNo }}）</h3>
        <Table
          :columns="resultColumns"
          :data-source="entryRows"
          :pagination="false"
          row-key="characteristicId"
          size="small"
          class="mb-2"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'spec'">
              <span v-if="record.charType === 'QUANT'">
                {{ record.lowerLimit ?? '-' }} ~ {{ record.upperLimit ?? '-' }}
                (目标 {{ record.targetValue ?? '-' }})
              </span>
              <span v-else>{{ record.charType === 'VISUAL' ? '目视' : '计数' }}</span>
            </template>
            <template v-else-if="column.dataIndex === 'measuredValue'">
              <InputNumber
                v-if="record.charType === 'QUANT'"
                v-model:value="record.measuredValue"
                style="width: 110px"
              />
              <span v-else>-</span>
            </template>
            <template v-else-if="column.dataIndex === 'judge'">
              <span v-if="record.charType === 'QUANT'" class="text-gray-400">自动判定</span>
              <Select
                v-else
                v-model:value="record.judge"
                style="width: 90px"
                placeholder="OK/NG"
                :options="[
                  { label: 'OK', value: 'OK' },
                  { label: 'NG', value: 'NG' },
                ]"
              />
            </template>
          </template>
        </Table>
        <Button type="primary" class="mb-4" @click="handleRecord">提交本样本结果</Button>
      </template>

      <h3 class="mb-2 font-bold">已录结果（{{ detail?.results?.length ?? 0 }}）</h3>
      <Table
        :columns="recordedColumns"
        :data-source="detail?.results ?? []"
        :pagination="false"
        row-key="id"
        size="small"
        class="mb-4"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'judge'">
            <Tag :color="record.judge === 'OK' ? 'success' : 'error'">{{ record.judge }}</Tag>
          </template>
        </template>
      </Table>

      <template v-if="canInspect">
        <Input
          v-model:value="judgeRemark"
          class="mb-2"
          placeholder="判定说明（让步/不合格必填）"
        />
        <Space>
          <Button type="primary" @click="handleJudge('PASSED')">判定合格（放行）</Button>
          <Button @click="handleJudge('CONCESSION')">让步接收（放行）</Button>
          <Button danger @click="handleJudge('REJECTED')">判定不合格（拦截后道）</Button>
        </Space>
      </template>
    </Drawer>

    <Modal v-if="false" />
  </Page>
</template>
