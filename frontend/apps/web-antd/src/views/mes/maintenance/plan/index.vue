<script setup lang="ts">
import type { MntPlan, MntPlanItem } from '#/api/mes/maintenance';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  DatePicker,
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
  generateDueOrders,
  getMntPlanDetail,
  getMntPlanPage,
  removeMntPlans,
  saveMntPlan,
  updateMntPlan,
} from '#/api/mes/maintenance';
import { requestClient } from '#/api/request';

defineOptions({ name: 'MesMaintenancePlan' });

const TYPE_META: Record<string, string> = {
  PM: '保养',
  SPOT: '点检',
};

const CYCLE_META: Record<string, string> = {
  DAY: '日历天',
  RUNTIME: '运行时长(h)',
  OUTPUT: '产量(件)',
};

const loading = ref(false);
const generating = ref(false);
const dataSource = ref<MntPlan[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const workplaceOptions = ref<any[]>([]);

const columns = [
  { title: '编码', dataIndex: 'planCode', width: 130 },
  { title: '名称', dataIndex: 'planName' },
  { title: '类型', dataIndex: 'planType', width: 80 },
  { title: '设备', dataIndex: 'workplaceId', width: 160 },
  { title: '周期', dataIndex: 'cycleType', width: 130 },
  { title: '下次到期', dataIndex: 'nextDueDate', width: 110 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 140 },
];

const itemColumns = [
  { title: '项目号', dataIndex: 'itemNo', width: 90 },
  { title: '项目名称', dataIndex: 'itemName', width: 160 },
  { title: '标准/要求', dataIndex: 'standardDesc' },
  { title: '方法/工具', dataIndex: 'methodDesc', width: 160 },
  { title: '操作', key: 'action', width: 60 },
];

function workplaceLabel(id?: string) {
  const wp = workplaceOptions.value.find((w) => String(w.id) === String(id));
  return wp ? `${wp.workplaceCode} ${wp.workplaceName}` : id;
}

async function loadPage() {
  loading.value = true;
  try {
    const res = await getMntPlanPage({
      current: pagination.current,
      size: pagination.pageSize,
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
const formModel = reactive<MntPlan>({ planCode: '', planName: '', items: [] });

function emptyItem(no: string): MntPlanItem {
  return { itemNo: no, itemName: '' };
}

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    planCode: '',
    planName: '',
    planType: 'PM',
    workplaceId: undefined,
    cycleType: 'DAY',
    cycleValue: 30,
    nextDueDate: undefined,
    status: '0',
    remark: '',
    items: [emptyItem('01')],
  });
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: MntPlan) {
  const detail = await getMntPlanDetail(row.id!);
  Object.assign(formModel, detail);
  if (!formModel.items?.length) {
    formModel.items = [emptyItem('01')];
  }
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.planCode || !formModel.planName || !formModel.workplaceId) {
    message.warning('请填写编码、名称与设备');
    return;
  }
  if (
    !formModel.items?.length ||
    formModel.items.some((i) => !i.itemNo || !i.itemName)
  ) {
    message.warning('请完整填写标准作业项（项目号与名称）');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateMntPlan({ ...formModel })
      : saveMntPlan({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: MntPlan) {
  await removeMntPlans([row.id!]);
  message.success('删除成功');
  await loadPage();
}

async function handleGenerate() {
  generating.value = true;
  try {
    const count = await generateDueOrders();
    message.success(`已生成 ${count} 张到期维护工单`);
    await loadPage();
  } finally {
    generating.value = false;
  }
}

onMounted(async () => {
  const [, wps] = await Promise.all([
    loadPage(),
    requestClient.get<any[]>('/mes/modeling/workplace/list'),
  ]);
  workplaceOptions.value = wps;
});
</script>

<template>
  <Page
    title="维护计划"
    description="按周期（日历天/运行时长/产量）定义保养与点检计划及检查表；到期自动生成维护工单（运行时长/产量周期待物联网模块接入，当前手工维护到期日）"
  >
    <Card>
      <template #extra>
        <Space>
          <Button :loading="generating" @click="handleGenerate">
            生成到期工单
          </Button>
          <Button type="primary" @click="openCreate">新增维护计划</Button>
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
          <template v-if="column.dataIndex === 'planType'">
            <Tag :color="record.planType === 'SPOT' ? 'blue' : 'purple'">
              {{ TYPE_META[record.planType] ?? record.planType }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'workplaceId'">
            {{ workplaceLabel(record.workplaceId) }}
          </template>
          <template v-else-if="column.dataIndex === 'cycleType'">
            {{ CYCLE_META[record.cycleType] ?? record.cycleType }} ×
            {{ record.cycleValue }}
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
      :title="isEdit ? '编辑维护计划' : '新增维护计划'"
      :confirm-loading="modalSaving"
      width="1000px"
      @ok="handleSave"
    >
      <Form layout="inline" class="mb-4">
        <FormItem label="编码" required>
          <Input v-model:value="formModel.planCode" :disabled="isEdit" placeholder="MP-001" />
        </FormItem>
        <FormItem label="名称" required>
          <Input v-model:value="formModel.planName" placeholder="月度保养" />
        </FormItem>
        <FormItem label="类型">
          <Select
            v-model:value="formModel.planType"
            style="width: 100px"
            :options="Object.entries(TYPE_META).map(([value, label]) => ({ value, label }))"
          />
        </FormItem>
        <FormItem label="设备" required>
          <Select
            v-model:value="formModel.workplaceId as any"
            style="width: 220px"
            show-search
            option-filter-prop="label"
            :options="
              workplaceOptions.map((w) => ({
                value: w.id,
                label: `${w.workplaceCode} ${w.workplaceName}`,
              }))
            "
          />
        </FormItem>
        <FormItem label="周期">
          <Select
            v-model:value="formModel.cycleType"
            style="width: 130px"
            :options="Object.entries(CYCLE_META).map(([value, label]) => ({ value, label }))"
          />
        </FormItem>
        <FormItem label="周期值" required>
          <InputNumber v-model:value="formModel.cycleValue" :min="1" />
        </FormItem>
        <FormItem label="下次到期">
          <DatePicker v-model:value="formModel.nextDueDate as any" value-format="YYYY-MM-DD" />
        </FormItem>
      </Form>

      <Table
        :columns="itemColumns"
        :data-source="formModel.items"
        :pagination="false"
        row-key="itemNo"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'itemNo'">
            <Input v-model:value="record.itemNo" />
          </template>
          <template v-else-if="column.dataIndex === 'itemName'">
            <Input v-model:value="record.itemName" placeholder="如 检查油位" />
          </template>
          <template v-else-if="column.dataIndex === 'standardDesc'">
            <Input v-model:value="record.standardDesc" placeholder="如 油位在刻度线之间" />
          </template>
          <template v-else-if="column.dataIndex === 'methodDesc'">
            <Input v-model:value="record.methodDesc" placeholder="如 目视" />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button
              size="small"
              type="link"
              danger
              @click="formModel.items?.splice(index, 1)"
            >
              删
            </Button>
          </template>
        </template>
      </Table>
      <Button
        class="mt-2"
        block
        type="dashed"
        @click="
          formModel.items?.push(
            emptyItem(String((formModel.items?.length ?? 0) + 1).padStart(2, '0')),
          )
        "
      >
        + 添加作业项
      </Button>
    </Modal>
  </Page>
</template>
