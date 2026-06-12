<script setup lang="ts">
import type { ShiftModel, ShiftSegment } from '#/api/mes/modeling';

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
  Switch,
  Table,
} from 'ant-design-vue';

import {
  getShiftModelDetail,
  getShiftModelPage,
  removeShiftModels,
  saveShiftModel,
  updateShiftModel,
} from '#/api/mes/modeling';

defineOptions({ name: 'MesModelingShift' });

const loading = ref(false);
const dataSource = ref<ShiftModel[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const searchName = ref('');

const columns = [
  { title: '编码', dataIndex: 'modelCode', width: 160 },
  { title: '名称', dataIndex: 'modelName' },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', key: 'action', width: 160 },
];

const segmentColumns = [
  { title: '班段编码', dataIndex: 'segmentCode', width: 100 },
  { title: '班段名称', dataIndex: 'segmentName', width: 120 },
  { title: '开始时间', dataIndex: 'startTime', width: 120 },
  { title: '结束时间', dataIndex: 'endTime', width: 120 },
  { title: '跨天', dataIndex: 'crossDay', width: 70 },
  { title: '休息(分)', dataIndex: 'breakMinutes', width: 100 },
  { title: '操作', key: 'action', width: 70 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getShiftModelPage({
      current: pagination.current,
      size: pagination.pageSize,
      modelName: searchName.value || undefined,
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

/** 编辑弹窗 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const isEdit = ref(false);
const formModel = reactive<ShiftModel>({
  modelCode: '',
  modelName: '',
  status: '0',
  remark: '',
  segments: [],
});

function emptySegment(): ShiftSegment {
  return {
    segmentCode: '',
    segmentName: '',
    startTime: '08:00:00',
    endTime: '16:00:00',
    crossDay: '0',
    breakMinutes: 0,
  };
}

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    modelCode: '',
    modelName: '',
    status: '0',
    remark: '',
    segments: [emptySegment()],
  });
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: ShiftModel) {
  const detail = await getShiftModelDetail(row.id!);
  Object.assign(formModel, detail);
  if (!formModel.segments?.length) {
    formModel.segments = [emptySegment()];
  }
  isEdit.value = true;
  modalOpen.value = true;
}

function addSegment() {
  formModel.segments?.push(emptySegment());
}

function removeSegment(index: number) {
  formModel.segments?.splice(index, 1);
}

async function handleSave() {
  if (!formModel.modelCode || !formModel.modelName) {
    message.warning('请填写编码与名称');
    return;
  }
  if (!formModel.segments?.length) {
    message.warning('至少需要一个班段');
    return;
  }
  for (const seg of formModel.segments) {
    if (!seg.segmentCode || !seg.segmentName || !seg.startTime || !seg.endTime) {
      message.warning('请完整填写班段信息');
      return;
    }
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateShiftModel({ ...formModel })
      : saveShiftModel({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: ShiftModel) {
  await removeShiftModels([row.id!]);
  message.success('删除成功');
  await loadPage();
}

onMounted(loadPage);
</script>

<template>
  <Page title="班次模型" description="定义班段（早/中/晚班，支持跨天），供工厂日历引用">
    <Card>
      <template #extra>
        <Space>
          <Input
            v-model:value="searchName"
            placeholder="按名称搜索"
            style="width: 200px"
            allow-clear
            @press-enter="loadPage"
          />
          <Button @click="loadPage">查询</Button>
          <Button type="primary" @click="openCreate">新增班次模型</Button>
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
          <template v-if="column.dataIndex === 'status'">
            {{ record.status === '0' ? '启用' : '停用' }}
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
      :title="isEdit ? '编辑班次模型' : '新增班次模型'"
      :confirm-loading="modalSaving"
      width="860px"
      @ok="handleSave"
    >
      <Form layout="inline" class="mb-4">
        <FormItem label="编码" required>
          <Input
            v-model:value="formModel.modelCode"
            :disabled="isEdit"
            placeholder="如 SHIFT-2"
          />
        </FormItem>
        <FormItem label="名称" required>
          <Input v-model:value="formModel.modelName" placeholder="如 两班倒" />
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
        <FormItem label="备注">
          <Input v-model:value="formModel.remark" placeholder="备注" />
        </FormItem>
      </Form>

      <Table
        :columns="segmentColumns"
        :data-source="formModel.segments"
        :pagination="false"
        row-key="segmentCode"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'segmentCode'">
            <Input v-model:value="record.segmentCode" placeholder="D" />
          </template>
          <template v-else-if="column.dataIndex === 'segmentName'">
            <Input v-model:value="record.segmentName" placeholder="早班" />
          </template>
          <template v-else-if="column.dataIndex === 'startTime'">
            <Input v-model:value="record.startTime" placeholder="08:00:00" />
          </template>
          <template v-else-if="column.dataIndex === 'endTime'">
            <Input v-model:value="record.endTime" placeholder="16:00:00" />
          </template>
          <template v-else-if="column.dataIndex === 'crossDay'">
            <Switch
              :checked="record.crossDay === '1'"
              @change="(v: any) => (record.crossDay = v ? '1' : '0')"
            />
          </template>
          <template v-else-if="column.dataIndex === 'breakMinutes'">
            <InputNumber v-model:value="record.breakMinutes" :min="0" />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button size="small" type="link" danger @click="removeSegment(index)">
              删除
            </Button>
          </template>
        </template>
      </Table>
      <Button class="mt-2" block type="dashed" @click="addSegment">
        + 添加班段
      </Button>
    </Modal>
  </Page>
</template>
