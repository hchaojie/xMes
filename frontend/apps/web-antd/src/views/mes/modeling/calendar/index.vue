<script setup lang="ts">
import type {
  CalendarDay,
  FactoryCalendar,
  ShiftModel,
} from '#/api/mes/modeling';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Checkbox,
  CheckboxGroup,
  Drawer,
  Form,
  FormItem,
  Input,
  message,
  Modal,
  Popconfirm,
  RangePicker,
  Select,
  Space,
  Table,
  Tag,
} from 'ant-design-vue';

import {
  batchSetCalendarDays,
  getCalendarDays,
  getCalendarPage,
  getShiftModelList,
  removeCalendars,
  saveCalendar,
  updateCalendar,
} from '#/api/mes/modeling';

defineOptions({ name: 'MesModelingCalendar' });

const DAY_TYPE_META: Record<string, { color: string; label: string }> = {
  WORK: { label: '工作日', color: 'success' },
  REST: { label: '休息日', color: 'default' },
  HOLIDAY: { label: '节假日', color: 'error' },
};

const WEEKDAY_OPTIONS = [
  { label: '周一', value: 1 },
  { label: '周二', value: 2 },
  { label: '周三', value: 3 },
  { label: '周四', value: 4 },
  { label: '周五', value: 5 },
  { label: '周六', value: 6 },
  { label: '周日', value: 7 },
];

const loading = ref(false);
const dataSource = ref<FactoryCalendar[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const shiftModels = ref<ShiftModel[]>([]);

const columns = [
  { title: '编码', dataIndex: 'calendarCode', width: 160 },
  { title: '名称', dataIndex: 'calendarName' },
  { title: '状态', dataIndex: 'status', width: 90 },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', key: 'action', width: 220 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getCalendarPage({
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

/** 日历基本信息编辑 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const isEdit = ref(false);
const formModel = reactive<FactoryCalendar>({
  calendarCode: '',
  calendarName: '',
  status: '0',
  remark: '',
});

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    calendarCode: '',
    calendarName: '',
    status: '0',
    remark: '',
  });
  isEdit.value = false;
  modalOpen.value = true;
}

function openEdit(row: FactoryCalendar) {
  Object.assign(formModel, row);
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.calendarCode || !formModel.calendarName) {
    message.warning('请填写编码与名称');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateCalendar({ ...formModel })
      : saveCalendar({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: FactoryCalendar) {
  await removeCalendars([row.id!]);
  message.success('删除成功');
  await loadPage();
}

/** 日明细抽屉 */
const drawerOpen = ref(false);
const currentCalendar = ref<FactoryCalendar | null>(null);
const days = ref<CalendarDay[]>([]);
const daysLoading = ref(false);
const dayRange = ref<[string, string] | undefined>();

const dayColumns = [
  { title: '日期', dataIndex: 'calDate', width: 130 },
  { title: '日类型', dataIndex: 'dayType', width: 100 },
  { title: '班次模型', dataIndex: 'shiftModelId' },
];

const batchForm = reactive({
  range: undefined as [string, string] | undefined,
  weekdays: [] as number[],
  dayType: 'WORK',
  shiftModelId: undefined as string | undefined,
});

async function openDays(row: FactoryCalendar) {
  currentCalendar.value = row;
  drawerOpen.value = true;
  await loadDays();
}

async function loadDays() {
  if (!currentCalendar.value) return;
  daysLoading.value = true;
  try {
    days.value = await getCalendarDays(
      currentCalendar.value.id!,
      dayRange.value?.[0],
      dayRange.value?.[1],
    );
  } finally {
    daysLoading.value = false;
  }
}

function shiftModelName(id?: string) {
  const m = shiftModels.value.find((s) => s.id === id);
  return m ? `${m.modelCode} ${m.modelName}` : '';
}

async function handleBatchSet() {
  if (!currentCalendar.value || !batchForm.range) {
    message.warning('请选择日期区间');
    return;
  }
  if (batchForm.dayType === 'WORK' && !batchForm.shiftModelId) {
    message.warning('工作日必须指定班次模型');
    return;
  }
  const affected = await batchSetCalendarDays({
    calendarId: currentCalendar.value.id!,
    startDate: batchForm.range[0],
    endDate: batchForm.range[1],
    weekdays: batchForm.weekdays.length > 0 ? batchForm.weekdays : undefined,
    dayType: batchForm.dayType,
    shiftModelId: batchForm.shiftModelId,
  });
  message.success(`已设置 ${affected} 天`);
  await loadDays();
}

onMounted(async () => {
  await loadPage();
  shiftModels.value = await getShiftModelList();
});
</script>

<template>
  <Page title="工厂日历" description="按日期区间批量设置工作日/休息日/节假日与班次模型">
    <Card>
      <template #extra>
        <Button type="primary" @click="openCreate">新增日历</Button>
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
              <Button size="small" type="link" @click="openDays(record as any)">
                日明细
              </Button>
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
      :title="isEdit ? '编辑日历' : '新增日历'"
      :confirm-loading="modalSaving"
      @ok="handleSave"
    >
      <Form layout="vertical">
        <FormItem label="编码" required>
          <Input
            v-model:value="formModel.calendarCode"
            :disabled="isEdit"
            placeholder="如 CAL-2026"
          />
        </FormItem>
        <FormItem label="名称" required>
          <Input
            v-model:value="formModel.calendarName"
            placeholder="如 2026年标准日历"
          />
        </FormItem>
        <FormItem label="状态">
          <Select
            v-model:value="formModel.status"
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
    </Modal>

    <Drawer
      v-model:open="drawerOpen"
      :title="`日明细 - ${currentCalendar?.calendarName ?? ''}`"
      width="720"
    >
      <Card size="small" title="批量设置" class="mb-4">
        <Form layout="vertical">
          <FormItem label="日期区间" required>
            <RangePicker
              v-model:value="batchForm.range as any"
              value-format="YYYY-MM-DD"
            />
          </FormItem>
          <FormItem label="适用星期（不选则全部）">
            <CheckboxGroup v-model:value="batchForm.weekdays">
              <Checkbox
                v-for="opt in WEEKDAY_OPTIONS"
                :key="opt.value"
                :value="opt.value"
              >
                {{ opt.label }}
              </Checkbox>
            </CheckboxGroup>
          </FormItem>
          <Space>
            <FormItem label="日类型">
              <Select
                v-model:value="batchForm.dayType"
                style="width: 120px"
                :options="
                  Object.entries(DAY_TYPE_META).map(([value, m]) => ({
                    value,
                    label: m.label,
                  }))
                "
              />
            </FormItem>
            <FormItem label="班次模型（工作日必填）">
              <Select
                v-model:value="batchForm.shiftModelId"
                style="width: 220px"
                allow-clear
                :options="
                  shiftModels.map((s) => ({
                    value: s.id,
                    label: `${s.modelCode} ${s.modelName}`,
                  }))
                "
              />
            </FormItem>
          </Space>
          <Button type="primary" @click="handleBatchSet">应用设置</Button>
        </Form>
      </Card>

      <Space class="mb-2">
        <RangePicker
          v-model:value="dayRange as any"
          value-format="YYYY-MM-DD"
        />
        <Button @click="loadDays">查询</Button>
      </Space>
      <Table
        :columns="dayColumns"
        :data-source="days"
        :loading="daysLoading"
        :pagination="{ pageSize: 31 }"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'dayType'">
            <Tag :color="DAY_TYPE_META[record.dayType]?.color">
              {{ DAY_TYPE_META[record.dayType]?.label ?? record.dayType }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'shiftModelId'">
            {{ shiftModelName(record.shiftModelId) }}
          </template>
        </template>
      </Table>
    </Drawer>
  </Page>
</template>
