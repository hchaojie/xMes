<script setup lang="ts">
import type { Material } from '#/api/mes/masterdata';
import type { MtLot } from '#/api/mes/material';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Form,
  FormItem,
  Input,
  InputNumber,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import { getMaterialList } from '#/api/mes/masterdata';
import {
  changeLotStatus,
  getLotPage,
  refreshOutputs,
  registerLot,
} from '#/api/mes/material';

defineOptions({ name: 'MesMaterialLot' });

const STATUS_META: Record<string, { color: string; label: string }> = {
  AVAILABLE: { label: '可用', color: 'success' },
  HOLD: { label: '隔离', color: 'warning' },
  FROZEN: { label: '冻结', color: 'error' },
  CONSUMED: { label: '耗尽', color: 'default' },
};

const SOURCE_META: Record<string, string> = {
  PURCHASE: '采购收货',
  PRODUCTION: '生产产出',
  SPLIT: '拆分',
};

const loading = ref(false);
const dataSource = ref<MtLot[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const query = reactive({
  lotNo: '',
  materialCode: '',
  lotStatus: undefined as string | undefined,
});
const materialOptions = ref<Material[]>([]);

async function searchMaterials(keyword?: string) {
  materialOptions.value = await getMaterialList(keyword);
}

const columns = [
  { title: '批次号', dataIndex: 'lotNo', width: 160 },
  { title: '物料', dataIndex: 'materialCode', width: 200 },
  { title: '数量', dataIndex: 'qty', width: 110 },
  { title: '状态', dataIndex: 'lotStatus', width: 90 },
  { title: '来源', dataIndex: 'sourceType', width: 100 },
  { title: '供应商批次', dataIndex: 'supplierLotNo', width: 120 },
  { title: '失效日期', dataIndex: 'expireDate', width: 110 },
  { title: '操作', key: 'action', width: 180 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getLotPage({
      current: pagination.current,
      size: pagination.pageSize,
      lotNo: query.lotNo || undefined,
      materialCode: query.materialCode || undefined,
      lotStatus: query.lotStatus,
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

/** 收货登记 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const formModel = reactive<MtLot>({ materialId: '', qty: 0 });

function openCreate() {
  Object.assign(formModel, {
    materialId: undefined,
    lotNo: '',
    qty: undefined,
    supplierLotNo: '',
    prodDate: undefined,
    remark: '',
  });
  modalOpen.value = true;
}

async function handleRegister() {
  if (!formModel.materialId || !formModel.qty) {
    message.warning('请选择物料并填写数量');
    return;
  }
  modalSaving.value = true;
  try {
    await registerLot({ ...formModel });
    message.success('批次已登记（批次号留空时按规则自动生成）');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleStatus(row: MtLot, status: string) {
  await changeLotStatus(row.id!, status);
  message.success('状态已变更');
  await loadPage();
}

async function handleRefreshOutput() {
  const n = await refreshOutputs();
  message.success(`扫描完工工单，新生成 ${n} 个工单的产出（批次/序列件+谱系）`);
  await loadPage();
}

onMounted(async () => {
  await Promise.all([loadPage(), searchMaterials()]);
});
</script>

<template>
  <Page title="批次台账" description="原料收货登记、生产产出批次/一物一码序列件（完工扫描生成）、批次状态防错（隔离/冻结批次禁止上料）">
    <Card>
      <template #extra>
        <Space>
          <Input v-model:value="query.lotNo" placeholder="批次号" style="width: 140px" allow-clear @press-enter="loadPage" />
          <Input v-model:value="query.materialCode" placeholder="物料编码" style="width: 120px" allow-clear @press-enter="loadPage" />
          <Select
            v-model:value="query.lotStatus"
            style="width: 100px"
            placeholder="状态"
            allow-clear
            :options="Object.entries(STATUS_META).map(([value, m]) => ({ value, label: m.label }))"
            @change="loadPage"
          />
          <Button @click="loadPage">查询</Button>
          <Button @click="handleRefreshOutput">刷新产出</Button>
          <Button type="primary" @click="openCreate">收货登记</Button>
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
          <template v-else-if="column.dataIndex === 'qty'">
            {{ record.qty }} {{ record.unit }}
          </template>
          <template v-else-if="column.dataIndex === 'lotStatus'">
            <Tag :color="STATUS_META[record.lotStatus]?.color">
              {{ STATUS_META[record.lotStatus]?.label ?? record.lotStatus }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'sourceType'">
            {{ SOURCE_META[record.sourceType] ?? record.sourceType }}
          </template>
          <template v-else-if="column.key === 'action'">
            <Space>
              <Popconfirm
                v-if="record.lotStatus === 'AVAILABLE'"
                title="隔离后禁止上料，确认？"
                @confirm="handleStatus(record as any, 'HOLD')"
              >
                <Button size="small" type="link">隔离</Button>
              </Popconfirm>
              <Popconfirm
                v-if="record.lotStatus === 'HOLD'"
                title="解除隔离？"
                @confirm="handleStatus(record as any, 'AVAILABLE')"
              >
                <Button size="small" type="link">解除</Button>
              </Popconfirm>
              <Popconfirm
                v-if="['AVAILABLE', 'HOLD'].includes(record.lotStatus)"
                title="冻结后禁止一切操作，确认？"
                @confirm="handleStatus(record as any, 'FROZEN')"
              >
                <Button size="small" type="link" danger>冻结</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </Card>

    <Modal
      v-model:open="modalOpen"
      title="收货登记批次"
      :confirm-loading="modalSaving"
      @ok="handleRegister"
    >
      <Form layout="vertical">
        <FormItem label="物料" required>
          <Select
            v-model:value="formModel.materialId as any"
            placeholder="搜索物料"
            show-search
            :filter-option="false"
            :options="
              materialOptions.map((m) => ({
                value: m.id,
                label: `${m.materialCode} ${m.materialName}`,
              }))
            "
            @search="searchMaterials"
          />
        </FormItem>
        <Space>
          <FormItem label="数量" required>
            <InputNumber v-model:value="formModel.qty" :min="0.000001" style="width: 150px" />
          </FormItem>
          <FormItem label="批次号（空=自动生成）">
            <Input v-model:value="formModel.lotNo" placeholder="LOT..." style="width: 180px" />
          </FormItem>
        </Space>
        <Space>
          <FormItem label="供应商批次">
            <Input v-model:value="formModel.supplierLotNo" style="width: 150px" />
          </FormItem>
          <FormItem label="生产日期">
            <Input v-model:value="formModel.prodDate" placeholder="YYYY-MM-DD" style="width: 140px" />
          </FormItem>
        </Space>
        <FormItem label="备注">
          <Input v-model:value="formModel.remark" />
        </FormItem>
      </Form>
    </Modal>
  </Page>
</template>
