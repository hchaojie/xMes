<script setup lang="ts">
import type { Material } from '#/api/mes/masterdata';

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

import {
  getMaterialPage,
  removeMaterials,
  saveMaterial,
  updateMaterial,
} from '#/api/mes/masterdata';

defineOptions({ name: 'MesMasterdataMaterial' });

const TYPE_META: Record<string, { color: string; label: string }> = {
  RAW: { label: '原材料', color: 'blue' },
  SEMI: { label: '半成品', color: 'orange' },
  FINISHED: { label: '成品', color: 'green' },
  AUX: { label: '辅料', color: 'default' },
};

const LOT_META: Record<string, string> = {
  NONE: '不管批',
  LOT: '批次',
  SERIAL: '序列号(一物一码)',
};

const TYPE_OPTIONS = Object.entries(TYPE_META).map(([value, m]) => ({
  value,
  label: m.label,
}));

const LOT_OPTIONS = Object.entries(LOT_META).map(([value, label]) => ({
  value,
  label,
}));

const loading = ref(false);
const dataSource = ref<Material[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const query = reactive({ materialCode: '', materialName: '' });

const columns = [
  { title: '物料编码', dataIndex: 'materialCode', width: 160 },
  { title: '物料名称', dataIndex: 'materialName' },
  { title: '类型', dataIndex: 'materialType', width: 100 },
  { title: '规格型号', dataIndex: 'spec', width: 140 },
  { title: '单位', dataIndex: 'unit', width: 70 },
  { title: '批次策略', dataIndex: 'lotStrategy', width: 150 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 140 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getMaterialPage({
      current: pagination.current,
      size: pagination.pageSize,
      materialCode: query.materialCode || undefined,
      materialName: query.materialName || undefined,
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

const modalOpen = ref(false);
const modalSaving = ref(false);
const isEdit = ref(false);
const formModel = reactive<Material>({
  materialCode: '',
  materialName: '',
  materialType: 'RAW',
  unit: '件',
  lotStrategy: 'LOT',
  status: '0',
});

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    materialCode: '',
    materialName: '',
    materialType: 'RAW',
    spec: '',
    unit: '件',
    lotStrategy: 'LOT',
    shelfLifeDays: undefined,
    status: '0',
    remark: '',
  });
  isEdit.value = false;
  modalOpen.value = true;
}

function openEdit(row: Material) {
  Object.assign(formModel, row);
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.materialCode || !formModel.materialName || !formModel.unit) {
    message.warning('请填写编码、名称与单位');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateMaterial({ ...formModel })
      : saveMaterial({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: Material) {
  await removeMaterials([row.id!]);
  message.success('删除成功');
  await loadPage();
}

onMounted(loadPage);
</script>

<template>
  <Page title="物料管理" description="物料主数据：类型、单位、批次管理策略（决策 Q3：策略按物料可配置，成品可启用一物一码）">
    <Card>
      <template #extra>
        <Space>
          <Input
            v-model:value="query.materialCode"
            placeholder="物料编码"
            style="width: 150px"
            allow-clear
            @press-enter="loadPage"
          />
          <Input
            v-model:value="query.materialName"
            placeholder="物料名称"
            style="width: 150px"
            allow-clear
            @press-enter="loadPage"
          />
          <Button @click="loadPage">查询</Button>
          <Button type="primary" @click="openCreate">新增物料</Button>
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
          <template v-if="column.dataIndex === 'materialType'">
            <Tag :color="TYPE_META[record.materialType]?.color">
              {{ TYPE_META[record.materialType]?.label ?? record.materialType }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'lotStrategy'">
            {{ LOT_META[record.lotStrategy] ?? record.lotStrategy }}
          </template>
          <template v-else-if="column.dataIndex === 'status'">
            <Tag :color="record.status === '0' ? 'success' : 'default'">
              {{ record.status === '0' ? '启用' : '停用' }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="openEdit(record as any)">
                编辑
              </Button>
              <Popconfirm title="确认删除？" @confirm="handleRemove(record as any)">
                <Button size="small" type="link" danger>删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </Card>

    <Modal
      v-model:open="modalOpen"
      :title="isEdit ? '编辑物料' : '新增物料'"
      :confirm-loading="modalSaving"
      @ok="handleSave"
    >
      <Form layout="vertical">
        <FormItem label="物料编码" required>
          <Input
            v-model:value="formModel.materialCode"
            :disabled="isEdit"
            placeholder="如 M-1001"
          />
        </FormItem>
        <FormItem label="物料名称" required>
          <Input v-model:value="formModel.materialName" placeholder="请输入名称" />
        </FormItem>
        <Space>
          <FormItem label="类型">
            <Select
              v-model:value="formModel.materialType"
              style="width: 140px"
              :options="TYPE_OPTIONS"
            />
          </FormItem>
          <FormItem label="单位" required>
            <Input v-model:value="formModel.unit" style="width: 100px" />
          </FormItem>
          <FormItem label="批次策略">
            <Select
              v-model:value="formModel.lotStrategy"
              style="width: 180px"
              :options="LOT_OPTIONS"
            />
          </FormItem>
        </Space>
        <FormItem label="规格型号">
          <Input v-model:value="formModel.spec" placeholder="规格型号" />
        </FormItem>
        <Space>
          <FormItem label="保质期（天）">
            <InputNumber v-model:value="formModel.shelfLifeDays" :min="0" />
          </FormItem>
          <FormItem label="状态">
            <Select
              v-model:value="formModel.status"
              style="width: 100px"
              :options="[
                { label: '启用', value: '0' },
                { label: '停用', value: '1' },
              ]"
            />
          </FormItem>
        </Space>
        <FormItem label="备注">
          <Input v-model:value="formModel.remark" placeholder="备注" />
        </FormItem>
      </Form>
    </Modal>
  </Page>
</template>
