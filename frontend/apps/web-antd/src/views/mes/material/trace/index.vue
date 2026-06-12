<script setup lang="ts">
import { ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Input,
  message,
  Radio,
  RadioGroup,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import { traceBackward, traceForward } from '#/api/mes/material';

defineOptions({ name: 'MesMaterialTrace' });

const direction = ref<'backward' | 'forward'>('backward');
const queryNo = ref('');
const loading = ref(false);
const result = ref<null | Record<string, any>>(null);

const consumeColumns = [
  { title: '批次号', dataIndex: 'lotNo', width: 160 },
  { title: '物料', dataIndex: 'materialCode', width: 140 },
  { title: '消耗数量', dataIndex: 'qty', width: 100 },
  { title: '上料人', dataIndex: 'person', width: 100 },
];

const inspectionColumns = [
  { title: '检验单号', dataIndex: 'inspectionNo', width: 160 },
  { title: '工序', dataIndex: 'operationNo', width: 80 },
  { title: '判定', dataIndex: 'status', width: 110 },
  { title: '检验员', dataIndex: 'inspector', width: 100 },
];

const outputColumns = [
  { title: '工单号', dataIndex: 'orderNo', width: 160 },
  { title: '产出类型', dataIndex: 'outputType', width: 100 },
  { title: '批次/序列号', dataIndex: 'no', width: 180 },
  { title: '物料', dataIndex: 'materialCode', width: 140 },
];

const QC_STATUS: Record<string, { color: string; label: string }> = {
  PASSED: { label: '合格', color: 'success' },
  CONCESSION: { label: '让步', color: 'warning' },
  REJECTED: { label: '不合格', color: 'error' },
  PENDING: { label: '待检', color: 'default' },
  INSPECTING: { label: '检验中', color: 'processing' },
};

async function handleTrace() {
  if (!queryNo.value) {
    message.warning('请输入批次号或序列号');
    return;
  }
  loading.value = true;
  try {
    result.value =
      direction.value === 'backward'
        ? await traceBackward(queryNo.value)
        : await traceForward(queryNo.value);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <Page title="批次追溯" description="反向：产出批次/序列号 → 工单·工艺版本·消耗批次·检验记录；正向：原料批次 → 产出批次/序列件">
    <Card>
      <Space class="mb-4">
        <RadioGroup v-model:value="direction">
          <Radio value="backward">反向追溯（成品 → 原料）</Radio>
          <Radio value="forward">正向追溯（原料 → 成品）</Radio>
        </RadioGroup>
        <Input
          v-model:value="queryNo"
          style="width: 280px"
          :placeholder="direction === 'backward' ? '产出批次号 / 序列号' : '原料批次号'"
          allow-clear
          @press-enter="handleTrace"
        />
        <Button type="primary" :loading="loading" @click="handleTrace">追溯</Button>
      </Space>

      <template v-if="result">
        <!-- 反向 -->
        <template v-if="direction === 'backward'">
          <Descriptions bordered size="small" :column="4" class="mb-4">
            <DescriptionsItem label="对象类型">
              {{ result.type === 'SERIAL' ? '序列件(一物一码)' : '批次' }}
            </DescriptionsItem>
            <DescriptionsItem label="编号">
              {{ result.serialNo ?? result.lotNo }}
            </DescriptionsItem>
            <DescriptionsItem label="物料">{{ result.materialCode }}</DescriptionsItem>
            <DescriptionsItem label="状态">{{ result.status }}</DescriptionsItem>
            <template v-if="result.production">
              <DescriptionsItem label="生产工单">
                {{ result.production.orderNo }}
              </DescriptionsItem>
              <DescriptionsItem label="工艺版本">
                V{{ result.production.routingVersion }}
              </DescriptionsItem>
              <DescriptionsItem label="良品">{{ result.production.qtyGood }}</DescriptionsItem>
              <DescriptionsItem label="报废">{{ result.production.qtyScrap }}</DescriptionsItem>
            </template>
          </Descriptions>

          <h3 class="mb-2 font-bold">消耗批次（投入料）</h3>
          <Table
            :columns="consumeColumns"
            :data-source="result.consumedLots ?? []"
            :pagination="false"
            row-key="lotNo"
            size="small"
            class="mb-4"
          />

          <h3 class="mb-2 font-bold">检验记录</h3>
          <Table
            :columns="inspectionColumns"
            :data-source="result.inspections ?? []"
            :pagination="false"
            row-key="inspectionNo"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'status'">
                <Tag :color="QC_STATUS[record.status]?.color">
                  {{ QC_STATUS[record.status]?.label ?? record.status }}
                </Tag>
              </template>
            </template>
          </Table>
        </template>

        <!-- 正向 -->
        <template v-else>
          <Descriptions bordered size="small" :column="3" class="mb-4">
            <DescriptionsItem label="原料批次">{{ result.lotNo }}</DescriptionsItem>
            <DescriptionsItem label="物料">{{ result.materialCode }}</DescriptionsItem>
            <DescriptionsItem label="流向产出数">{{ result.outputCount }}</DescriptionsItem>
          </Descriptions>
          <Table
            :columns="outputColumns"
            :data-source="result.outputs ?? []"
            :pagination="false"
            row-key="no"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.dataIndex === 'outputType'">
                <Tag :color="record.outputType === 'SERIAL' ? 'purple' : 'blue'">
                  {{ record.outputType === 'SERIAL' ? '序列件' : '批次' }}
                </Tag>
              </template>
            </template>
          </Table>
        </template>
      </template>
    </Card>
  </Page>
</template>
