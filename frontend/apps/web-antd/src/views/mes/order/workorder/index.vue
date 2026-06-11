<script setup lang="ts">
import type { Material } from '#/api/mes/masterdata';
import type { BookingQty, WoBooking, WoOrder, WoTask } from '#/api/mes/order';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Drawer,
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
  cancelWorkOrder,
  createWorkOrder,
  finishTask,
  getTaskBookings,
  getWorkOrderDetail,
  getWorkOrderPage,
  holdWorkOrder,
  pauseTask,
  releaseWorkOrders,
  removeWorkOrders,
  reportTaskQty,
  resumeTask,
  resumeWorkOrder,
  reverseBooking,
  startTask,
} from '#/api/mes/order';

defineOptions({ name: 'MesOrderWorkorder' });

const STATUS_META: Record<string, { color: string; label: string }> = {
  CREATED: { label: '已创建', color: 'default' },
  RELEASED: { label: '已下达', color: 'processing' },
  IN_PROGRESS: { label: '执行中', color: 'blue' },
  HOLD: { label: '挂起', color: 'warning' },
  COMPLETED: { label: '已完工', color: 'success' },
  CLOSED: { label: '已关闭', color: 'success' },
  CANCELLED: { label: '已取消', color: 'error' },
};

const TASK_STATUS_META: Record<string, string> = {
  PENDING: '待排',
  SCHEDULED: '已排程',
  DISPATCHED: '已派工',
  RUNNING: '进行中',
  PAUSED: '暂停',
  COMPLETED: '完工',
};

const loading = ref(false);
const dataSource = ref<WoOrder[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const query = reactive({ orderNo: '', orderStatus: undefined as string | undefined });
const materialOptions = ref<Material[]>([]);

async function searchMaterials(keyword?: string) {
  materialOptions.value = await getMaterialList(keyword);
}

const columns = [
  { title: '工单号', dataIndex: 'orderNo', width: 150 },
  { title: '物料', dataIndex: 'materialCode', width: 200 },
  { title: '数量', dataIndex: 'quantity', width: 100 },
  { title: '工艺版本', dataIndex: 'routingVersion', width: 90 },
  { title: '计划开始', dataIndex: 'planStartDate', width: 110 },
  { title: '计划完工', dataIndex: 'planEndDate', width: 110 },
  { title: '状态', dataIndex: 'orderStatus', width: 90 },
  { title: '操作', key: 'action', width: 280 },
];

const taskColumns = [
  { title: '工序号', dataIndex: 'operationNo', width: 80 },
  { title: '工序名称', dataIndex: 'operationName', width: 120 },
  { title: '状态', dataIndex: 'taskStatus', width: 80 },
  { title: '应做', dataIndex: 'qtyTarget', width: 70 },
  { title: '良品', dataIndex: 'qtyGood', width: 70 },
  { title: '报废', dataIndex: 'qtyScrap', width: 70 },
  { title: '质量门', dataIndex: 'qualityGate', width: 65 },
  { title: '报工操作', key: 'booking', width: 250 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getWorkOrderPage({
      current: pagination.current,
      size: pagination.pageSize,
      orderNo: query.orderNo || undefined,
      orderStatus: query.orderStatus,
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

/** 新建工单 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const formModel = reactive<WoOrder>({ materialId: '', quantity: 1 });

function openCreate() {
  Object.assign(formModel, {
    materialId: undefined,
    quantity: 1,
    planStartDate: undefined,
    planEndDate: undefined,
    priority: 0,
    remark: '',
  });
  modalOpen.value = true;
}

async function handleCreate() {
  if (!formModel.materialId || !formModel.quantity) {
    message.warning('请选择物料并填写数量');
    return;
  }
  modalSaving.value = true;
  try {
    await createWorkOrder({ ...formModel });
    message.success('工单已创建（工艺快照取该物料当前已发布工作计划）');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

/** 状态操作 */
async function handleRelease(row: WoOrder) {
  await releaseWorkOrders([row.id!]);
  message.success('已下达');
  await loadPage();
}

const holdModalOpen = ref(false);
const holdReason = ref('');
const holdTarget = ref<null | WoOrder>(null);

function openHold(row: WoOrder) {
  holdTarget.value = row;
  holdReason.value = '';
  holdModalOpen.value = true;
}

async function handleHold() {
  if (!holdReason.value) {
    message.warning('请填写挂起原因');
    return;
  }
  await holdWorkOrder(holdTarget.value!.id!, holdReason.value);
  message.success('已挂起');
  holdModalOpen.value = false;
  await loadPage();
}

async function handleResume(row: WoOrder) {
  await resumeWorkOrder(row.id!);
  message.success('已恢复为已下达');
  await loadPage();
}

async function handleCancel(row: WoOrder) {
  await cancelWorkOrder(row.id!);
  message.success('已取消');
  await loadPage();
}

async function handleRemove(row: WoOrder) {
  await removeWorkOrders([row.id!]);
  message.success('删除成功');
  await loadPage();
}

/** 详情抽屉 */
const drawerOpen = ref(false);
const detail = ref<null | WoOrder>(null);

async function openDetail(row: WoOrder) {
  detail.value = await getWorkOrderDetail(row.id!);
  drawerOpen.value = true;
}

async function refreshDetail() {
  if (detail.value?.id) {
    detail.value = await getWorkOrderDetail(detail.value.id);
    await loadPage();
  }
}

/** 报工操作 */
async function handleStart(task: WoTask) {
  await startTask(task.id!);
  message.success(`工序 ${task.operationNo} 已开工`);
  await refreshDetail();
}

async function handlePauseTask(task: WoTask) {
  await pauseTask(task.id!, 'MANUAL');
  message.success('已暂停');
  await refreshDetail();
}

async function handleResumeTask(task: WoTask) {
  await resumeTask(task.id!);
  message.success('已恢复');
  await refreshDetail();
}

/** 报数/完工弹窗（共用） */
const qtyModalOpen = ref(false);
const qtyMode = ref<'finish' | 'qty'>('qty');
const qtyForm = reactive<BookingQty>({ taskId: '' });

function openQty(task: WoTask, mode: 'finish' | 'qty') {
  qtyMode.value = mode;
  Object.assign(qtyForm, {
    taskId: task.id,
    qtyGood: undefined,
    qtyScrap: undefined,
    qtyRework: undefined,
    reasonCode: undefined,
    remark: undefined,
  });
  qtyModalOpen.value = true;
}

async function handleQtySubmit() {
  if ((qtyForm.qtyScrap ?? 0) > 0 && !qtyForm.reasonCode) {
    message.warning('报废数量必须填写报废原因');
    return;
  }
  await (qtyMode.value === 'qty'
    ? reportTaskQty({ ...qtyForm })
    : finishTask({ ...qtyForm }));
  message.success(qtyMode.value === 'qty' ? '报数成功' : '作业已完工');
  qtyModalOpen.value = false;
  await refreshDetail();
}

/** 事件流抽屉 */
const bookingDrawerOpen = ref(false);
const bookings = ref<WoBooking[]>([]);
const bookingTask = ref<null | WoTask>(null);

const BOOKING_TYPE_META: Record<string, string> = {
  START: '开工',
  PAUSE: '暂停',
  RESUME: '恢复',
  QTY: '报数',
  FINISH: '完工',
  REVERSE: '冲销',
};

const bookingColumns = [
  { title: '时间', dataIndex: 'bookingTime', width: 160 },
  { title: '类型', dataIndex: 'bookingType', width: 70 },
  { title: '良品', dataIndex: 'qtyGood', width: 70 },
  { title: '报废', dataIndex: 'qtyScrap', width: 70 },
  { title: '原因', dataIndex: 'reasonCode', width: 90 },
  { title: '报工人', dataIndex: 'personName', width: 90 },
  { title: '操作', key: 'action', width: 70 },
];

async function openBookings(task: WoTask) {
  bookingTask.value = task;
  bookings.value = await getTaskBookings(task.id!);
  bookingDrawerOpen.value = true;
}

async function handleReverse(row: WoBooking) {
  await reverseBooking(row.id, '管理端冲销');
  message.success('已冲销');
  bookings.value = await getTaskBookings(bookingTask.value!.id!);
  await refreshDetail();
}

onMounted(async () => {
  await Promise.all([loadPage(), searchMaterials()]);
});
</script>

<template>
  <Page title="工单管理" description="从工作计划生成工单（工艺快照+作业列表）；状态机：已创建 → 已下达 → 执行中 → 完工 → 关闭，支持挂起/取消">
    <Card>
      <template #extra>
        <Space>
          <Input
            v-model:value="query.orderNo"
            placeholder="工单号"
            style="width: 160px"
            allow-clear
            @press-enter="loadPage"
          />
          <Select
            v-model:value="query.orderStatus"
            style="width: 120px"
            placeholder="状态"
            allow-clear
            :options="
              Object.entries(STATUS_META).map(([value, m]) => ({
                value,
                label: m.label,
              }))
            "
          />
          <Button @click="loadPage">查询</Button>
          <Button type="primary" @click="openCreate">新建工单</Button>
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
          <template v-else-if="column.dataIndex === 'quantity'">
            {{ record.quantity }} {{ record.unit }}
          </template>
          <template v-else-if="column.dataIndex === 'routingVersion'">
            V{{ record.routingVersion }}
          </template>
          <template v-else-if="column.dataIndex === 'orderStatus'">
            <Tag :color="STATUS_META[record.orderStatus]?.color">
              {{ STATUS_META[record.orderStatus]?.label ?? record.orderStatus }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="openDetail(record as any)">
                详情
              </Button>
              <Popconfirm
                v-if="record.orderStatus === 'CREATED'"
                title="确认下达？"
                @confirm="handleRelease(record as any)"
              >
                <Button size="small" type="link">下达</Button>
              </Popconfirm>
              <Button
                v-if="['IN_PROGRESS', 'RELEASED'].includes(record.orderStatus)"
                size="small"
                type="link"
                @click="openHold(record as any)"
              >
                挂起
              </Button>
              <Popconfirm
                v-if="record.orderStatus === 'HOLD'"
                title="恢复为已下达？"
                @confirm="handleResume(record as any)"
              >
                <Button size="small" type="link">恢复</Button>
              </Popconfirm>
              <Popconfirm
                v-if="['CREATED', 'RELEASED'].includes(record.orderStatus)"
                title="确认取消该工单？"
                @confirm="handleCancel(record as any)"
              >
                <Button size="small" type="link" danger>取消</Button>
              </Popconfirm>
              <Popconfirm
                v-if="record.orderStatus === 'CREATED'"
                title="确认删除？"
                @confirm="handleRemove(record as any)"
              >
                <Button size="small" type="link" danger>删除</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </Card>

    <Modal
      v-model:open="modalOpen"
      title="新建工单（从工作计划生成）"
      :confirm-loading="modalSaving"
      @ok="handleCreate"
    >
      <Form layout="vertical">
        <FormItem label="物料（须有已发布的工作计划）" required>
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
          <FormItem label="计划数量" required>
            <InputNumber v-model:value="formModel.quantity" :min="0.000001" style="width: 140px" />
          </FormItem>
          <FormItem label="优先级">
            <InputNumber v-model:value="formModel.priority" :min="0" style="width: 100px" />
          </FormItem>
        </Space>
        <Space>
          <FormItem label="计划开始">
            <Input v-model:value="formModel.planStartDate" placeholder="YYYY-MM-DD" style="width: 140px" />
          </FormItem>
          <FormItem label="计划完工">
            <Input v-model:value="formModel.planEndDate" placeholder="YYYY-MM-DD" style="width: 140px" />
          </FormItem>
        </Space>
        <FormItem label="备注">
          <Input v-model:value="formModel.remark" placeholder="备注" />
        </FormItem>
      </Form>
    </Modal>

    <Modal v-model:open="holdModalOpen" title="挂起工单" @ok="handleHold">
      <Form layout="vertical">
        <FormItem label="挂起原因" required>
          <Input v-model:value="holdReason" placeholder="如：质量问题待评审 / 缺料" />
        </FormItem>
      </Form>
    </Modal>

    <Drawer v-model:open="drawerOpen" :title="`工单 ${detail?.orderNo ?? ''}`" width="860">
      <Descriptions v-if="detail" bordered size="small" :column="3" class="mb-4">
        <DescriptionsItem label="物料">
          {{ detail.materialCode }} {{ detail.materialName }}
        </DescriptionsItem>
        <DescriptionsItem label="数量">{{ detail.quantity }} {{ detail.unit }}</DescriptionsItem>
        <DescriptionsItem label="状态">
          <Tag :color="STATUS_META[detail.orderStatus!]?.color">
            {{ STATUS_META[detail.orderStatus!]?.label }}
          </Tag>
        </DescriptionsItem>
        <DescriptionsItem label="工艺版本">V{{ detail.routingVersion }}</DescriptionsItem>
        <DescriptionsItem label="良品">{{ detail.qtyGood }}</DescriptionsItem>
        <DescriptionsItem label="报废">{{ detail.qtyScrap }}</DescriptionsItem>
        <DescriptionsItem v-if="detail.holdReason" label="挂起原因" :span="3">
          {{ detail.holdReason }}
        </DescriptionsItem>
      </Descriptions>

      <Table
        :columns="taskColumns"
        :data-source="detail?.tasks ?? []"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'taskStatus'">
            {{ TASK_STATUS_META[record.taskStatus] ?? record.taskStatus }}
          </template>
          <template v-else-if="column.dataIndex === 'qualityGate'">
            <Tag v-if="record.qualityGate === '1'" color="warning">是</Tag>
            <span v-else>-</span>
          </template>
          <template v-else-if="column.key === 'booking'">
            <Space>
              <Button
                v-if="['DISPATCHED', 'PENDING', 'SCHEDULED'].includes(record.taskStatus)"
                size="small"
                type="link"
                @click="handleStart(record as any)"
              >
                开工
              </Button>
              <Button
                v-if="record.taskStatus === 'RUNNING'"
                size="small"
                type="link"
                @click="handlePauseTask(record as any)"
              >
                暂停
              </Button>
              <Button
                v-if="record.taskStatus === 'PAUSED'"
                size="small"
                type="link"
                @click="handleResumeTask(record as any)"
              >
                恢复
              </Button>
              <Button
                v-if="record.taskStatus === 'RUNNING'"
                size="small"
                type="link"
                @click="openQty(record as any, 'qty')"
              >
                报数
              </Button>
              <Button
                v-if="['PAUSED', 'RUNNING'].includes(record.taskStatus)"
                size="small"
                type="link"
                @click="openQty(record as any, 'finish')"
              >
                完工
              </Button>
              <Button size="small" type="link" @click="openBookings(record as any)">
                事件
              </Button>
            </Space>
          </template>
        </template>
      </Table>
    </Drawer>

    <Modal
      v-model:open="qtyModalOpen"
      :title="qtyMode === 'qty' ? '作业报数' : '作业完工（可携带最后一笔数量）'"
      @ok="handleQtySubmit"
    >
      <Form layout="vertical">
        <Space>
          <FormItem label="良品数量">
            <InputNumber v-model:value="qtyForm.qtyGood" :min="0" style="width: 120px" />
          </FormItem>
          <FormItem label="报废数量">
            <InputNumber v-model:value="qtyForm.qtyScrap" :min="0" style="width: 120px" />
          </FormItem>
          <FormItem label="返工数量">
            <InputNumber v-model:value="qtyForm.qtyRework" :min="0" style="width: 120px" />
          </FormItem>
        </Space>
        <FormItem label="报废原因（报废>0 必填）">
          <Input v-model:value="qtyForm.reasonCode" placeholder="如 表面划伤 / 尺寸超差" />
        </FormItem>
        <FormItem label="备注">
          <Input v-model:value="qtyForm.remark" placeholder="备注" />
        </FormItem>
      </Form>
    </Modal>

    <Drawer
      v-model:open="bookingDrawerOpen"
      :title="`事件流 - 工序 ${bookingTask?.operationNo ?? ''} ${bookingTask?.operationName ?? ''}`"
      width="720"
    >
      <Table
        :columns="bookingColumns"
        :data-source="bookings"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'bookingType'">
            <Tag :color="record.bookingType === 'REVERSE' ? 'error' : 'blue'">
              {{ BOOKING_TYPE_META[record.bookingType] ?? record.bookingType }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Popconfirm
              v-if="record.bookingType === 'QTY'"
              title="生成负向冲销事件并回滚数量？"
              @confirm="handleReverse(record as any)"
            >
              <Button size="small" type="link" danger>冲销</Button>
            </Popconfirm>
          </template>
        </template>
      </Table>
    </Drawer>
  </Page>
</template>
