<script setup lang="ts">
import type { MntOrder } from '#/api/mes/maintenance';

import { onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Descriptions,
  DescriptionsItem,
  Form,
  FormItem,
  Input,
  message,
  Modal,
  Popconfirm,
  RadioButton,
  RadioGroup,
  Select,
  Space,
  Table,
  Tag,
  Textarea,
} from 'ant-design-vue';

import {
  acceptMntOrder,
  cancelMntOrder,
  closeMntOrder,
  finishMntOrder,
  getMntOrderDetail,
  getMntOrderPage,
  reportRepair,
} from '#/api/mes/maintenance';
import { requestClient } from '#/api/request';

defineOptions({ name: 'MesMaintenanceOrder' });

const TYPE_META: Record<string, { color: string; label: string }> = {
  PM: { label: '保养', color: 'purple' },
  REPAIR: { label: '维修', color: 'red' },
  SPOT: { label: '点检', color: 'blue' },
};

const STATUS_META: Record<string, { color: string; label: string }> = {
  PENDING: { label: '待接单', color: 'orange' },
  IN_PROGRESS: { label: '维修中', color: 'processing' },
  REVIEW: { label: '待验证', color: 'cyan' },
  CLOSED: { label: '已关闭', color: 'default' },
  CANCELLED: { label: '已取消', color: 'default' },
};

const loading = ref(false);
const dataSource = ref<MntOrder[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const filter = reactive<{
  orderStatus?: string;
  orderType?: MntOrder['orderType'];
}>({});
const workplaceOptions = ref<any[]>([]);

const columns = [
  { title: '工单号', dataIndex: 'orderNo', width: 150 },
  { title: '类型', dataIndex: 'orderType', width: 70 },
  { title: '设备', dataIndex: 'workplace', width: 160 },
  { title: '故障/内容', dataIndex: 'faultDesc' },
  { title: '状态', dataIndex: 'orderStatus', width: 90 },
  { title: '报修/生成', dataIndex: 'reportTime', width: 150 },
  { title: '停机(分)', dataIndex: 'downtimeMinutes', width: 90 },
  { title: '操作', key: 'action', width: 200 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getMntOrderPage({
      current: pagination.current,
      size: pagination.pageSize,
      ...filter,
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

/** 故障报修 */
const reportOpen = ref(false);
const reportSaving = ref(false);
const reportModel = reactive<MntOrder>({});

function openReport() {
  Object.assign(reportModel, {
    workplaceId: undefined,
    faultDesc: '',
    remark: '',
  });
  reportOpen.value = true;
}

async function handleReport() {
  if (!reportModel.workplaceId || !reportModel.faultDesc) {
    message.warning('请选择设备并填写故障现象');
    return;
  }
  reportSaving.value = true;
  try {
    await reportRepair({ ...reportModel });
    message.success('报修成功');
    reportOpen.value = false;
    await loadPage();
  } finally {
    reportSaving.value = false;
  }
}

/** 接单 / 取消 / 关闭 */
async function handleAccept(row: MntOrder) {
  await acceptMntOrder(row.id!);
  message.success('已接单，设备进入维修（MAINT）');
  await loadPage();
}

async function handleCancel(row: MntOrder) {
  await cancelMntOrder(row.id!);
  message.success('已取消');
  await loadPage();
}

async function handleClose(row: MntOrder) {
  await closeMntOrder(row.id!);
  message.success('已验证关闭');
  await loadPage();
}

/** 完工（含检查项结果录入） */
const finishOpen = ref(false);
const finishSaving = ref(false);
const finishModel = ref<MntOrder>({});

const finishItemColumns = [
  { title: '项目号', dataIndex: 'itemNo', width: 80 },
  { title: '项目名称', dataIndex: 'itemName', width: 160 },
  { title: '标准/要求', dataIndex: 'standardDesc' },
  { title: '结果', dataIndex: 'checkResult', width: 130 },
  { title: '说明', dataIndex: 'resultRemark', width: 180 },
];

async function openFinish(row: MntOrder) {
  finishModel.value = await getMntOrderDetail(row.id!);
  finishOpen.value = true;
}

async function handleFinish() {
  const items = finishModel.value.items ?? [];
  if (items.some((i) => !i.checkResult)) {
    message.warning('请录入所有检查项结果（OK/NG）');
    return;
  }
  finishSaving.value = true;
  try {
    await finishMntOrder({
      id: finishModel.value.id,
      faultCause: finishModel.value.faultCause,
      actionTaken: finishModel.value.actionTaken,
      spareParts: finishModel.value.spareParts,
      items,
    });
    message.success('完工提交成功（NG 项已自动报修）');
    finishOpen.value = false;
    await loadPage();
  } finally {
    finishSaving.value = false;
  }
}

/** 详情 */
const detailOpen = ref(false);
const detail = ref<MntOrder>({});

async function openDetail(row: MntOrder) {
  detail.value = await getMntOrderDetail(row.id!);
  detailOpen.value = true;
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
    title="维护工单"
    description="计划到期自动生成 / 故障报修；状态流：待接单 → 维修中 → 待验证 → 关闭；维修中设备在监控墙显示 MAINT"
  >
    <Card>
      <template #extra>
        <Space>
          <Select
            v-model:value="filter.orderType"
            style="width: 110px"
            placeholder="类型"
            allow-clear
            :options="Object.entries(TYPE_META).map(([value, m]) => ({ value, label: m.label }))"
            @change="loadPage"
          />
          <Select
            v-model:value="filter.orderStatus"
            style="width: 110px"
            placeholder="状态"
            allow-clear
            :options="Object.entries(STATUS_META).map(([value, m]) => ({ value, label: m.label }))"
            @change="loadPage"
          />
          <Button type="primary" danger @click="openReport">故障报修</Button>
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
          <template v-if="column.dataIndex === 'orderType'">
            <Tag :color="TYPE_META[record.orderType]?.color">
              {{ TYPE_META[record.orderType]?.label ?? record.orderType }}
            </Tag>
          </template>
          <template v-else-if="column.dataIndex === 'workplace'">
            {{ record.workplaceCode }} {{ record.workplaceName }}
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
              <Button
                v-if="record.orderStatus === 'PENDING'"
                size="small"
                type="link"
                @click="handleAccept(record as any)"
              >
                接单
              </Button>
              <Popconfirm
                v-if="record.orderStatus === 'PENDING'"
                title="确认取消？"
                @confirm="handleCancel(record as any)"
              >
                <Button size="small" type="link" danger>取消</Button>
              </Popconfirm>
              <Button
                v-if="record.orderStatus === 'IN_PROGRESS'"
                size="small"
                type="link"
                @click="openFinish(record as any)"
              >
                完工
              </Button>
              <Popconfirm
                v-if="record.orderStatus === 'REVIEW'"
                title="确认验证通过并关闭？"
                @confirm="handleClose(record as any)"
              >
                <Button size="small" type="link">验证关闭</Button>
              </Popconfirm>
            </Space>
          </template>
        </template>
      </Table>
    </Card>

    <Modal
      v-model:open="reportOpen"
      title="故障报修"
      :confirm-loading="reportSaving"
      @ok="handleReport"
    >
      <Form layout="vertical">
        <FormItem label="设备" required>
          <Select
            v-model:value="reportModel.workplaceId as any"
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
        <FormItem label="故障现象" required>
          <Textarea v-model:value="reportModel.faultDesc" :rows="3" placeholder="描述故障现象" />
        </FormItem>
        <FormItem label="备注">
          <Input v-model:value="reportModel.remark" />
        </FormItem>
      </Form>
    </Modal>

    <Modal
      v-model:open="finishOpen"
      :title="`完工提交 ${finishModel.orderNo ?? ''}`"
      :confirm-loading="finishSaving"
      width="900px"
      @ok="handleFinish"
    >
      <Form layout="vertical" class="mb-2">
        <FormItem v-if="finishModel.orderType === 'REPAIR'" label="故障原因">
          <Textarea v-model:value="finishModel.faultCause" :rows="2" />
        </FormItem>
        <FormItem label="处理措施">
          <Textarea v-model:value="finishModel.actionTaken" :rows="2" />
        </FormItem>
        <FormItem label="备件消耗">
          <Input v-model:value="finishModel.spareParts" placeholder="如 轴承 6204 ×2" />
        </FormItem>
      </Form>
      <Table
        v-if="finishModel.items?.length"
        :columns="finishItemColumns"
        :data-source="finishModel.items"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'checkResult'">
            <RadioGroup v-model:value="record.checkResult" size="small">
              <RadioButton value="OK">OK</RadioButton>
              <RadioButton value="NG">NG</RadioButton>
            </RadioGroup>
          </template>
          <template v-else-if="column.dataIndex === 'resultRemark'">
            <Input v-model:value="record.resultRemark" size="small" />
          </template>
        </template>
      </Table>
    </Modal>

    <Modal v-model:open="detailOpen" :title="`维护工单 ${detail.orderNo ?? ''}`" width="800px" :footer="null">
      <Descriptions :column="2" size="small" bordered>
        <DescriptionsItem label="类型">
          {{ TYPE_META[detail.orderType ?? '']?.label ?? detail.orderType }}
        </DescriptionsItem>
        <DescriptionsItem label="状态">
          {{ STATUS_META[detail.orderStatus ?? '']?.label ?? detail.orderStatus }}
        </DescriptionsItem>
        <DescriptionsItem label="设备" :span="2">
          {{ detail.workplaceCode }} {{ detail.workplaceName }}
        </DescriptionsItem>
        <DescriptionsItem label="故障/内容" :span="2">{{ detail.faultDesc }}</DescriptionsItem>
        <DescriptionsItem label="故障原因" :span="2">{{ detail.faultCause }}</DescriptionsItem>
        <DescriptionsItem label="处理措施" :span="2">{{ detail.actionTaken }}</DescriptionsItem>
        <DescriptionsItem label="备件消耗" :span="2">{{ detail.spareParts }}</DescriptionsItem>
        <DescriptionsItem label="报修人">{{ detail.reportBy }}</DescriptionsItem>
        <DescriptionsItem label="报修时间">{{ detail.reportTime }}</DescriptionsItem>
        <DescriptionsItem label="接单人">{{ detail.acceptBy }}</DescriptionsItem>
        <DescriptionsItem label="接单时间">{{ detail.acceptTime }}</DescriptionsItem>
        <DescriptionsItem label="完成时间">{{ detail.finishTime }}</DescriptionsItem>
        <DescriptionsItem label="停机时长">
          {{ detail.downtimeMinutes == null ? '-' : `${detail.downtimeMinutes} 分钟` }}
        </DescriptionsItem>
      </Descriptions>
      <Table
        v-if="detail.items?.length"
        class="mt-4"
        :columns="[
          { title: '项目号', dataIndex: 'itemNo', width: 80 },
          { title: '项目名称', dataIndex: 'itemName', width: 160 },
          { title: '标准/要求', dataIndex: 'standardDesc' },
          { title: '结果', dataIndex: 'checkResult', width: 70 },
          { title: '说明', dataIndex: 'resultRemark', width: 150 },
        ]"
        :data-source="detail.items"
        :pagination="false"
        row-key="id"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.dataIndex === 'checkResult'">
            <Tag v-if="record.checkResult" :color="record.checkResult === 'OK' ? 'success' : 'error'">
              {{ record.checkResult }}
            </Tag>
          </template>
        </template>
      </Table>
    </Modal>
  </Page>
</template>
