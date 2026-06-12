<script setup lang="ts">
import type { Material, Routing, RoutingOperation } from '#/api/mes/masterdata';

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
  Tag,
} from 'ant-design-vue';

import {
  copyRoutingVersion,
  freezeRouting,
  getMaterialList,
  getRoutingDetail,
  getRoutingPage,
  releaseRouting,
  removeRoutings,
  saveRouting,
  updateRouting,
} from '#/api/mes/masterdata';
import { requestClient } from '#/api/request';

defineOptions({ name: 'MesMasterdataRouting' });

const STATUS_META: Record<string, { color: string; label: string }> = {
  DRAFT: { label: '草稿', color: 'default' },
  RELEASED: { label: '已发布', color: 'success' },
  FROZEN: { label: '已冻结', color: 'error' },
};

const OP_TYPE_OPTIONS = [
  { label: '加工', value: 'PROCESS' },
  { label: '装配', value: 'ASSEMBLY' },
  { label: '检验', value: 'INSPECT' },
  { label: '外协', value: 'OUTSOURCE' },
  { label: '返修', value: 'REWORK' },
];

const REPORT_MODE_OPTIONS = [
  { label: '按件', value: 'PIECE' },
  { label: '按时', value: 'TIME' },
  { label: '里程碑', value: 'MILESTONE' },
];

const loading = ref(false);
const dataSource = ref<Routing[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const statusFilter = ref<string | undefined>();
const filterMaterialId = ref<string | undefined>();

const materialOptions = ref<Material[]>([]);
const workCenterOptions = ref<{ label: string; value: string }[]>([]);

async function searchMaterials(keyword?: string) {
  materialOptions.value = await getMaterialList(keyword);
}

async function loadWorkCenters() {
  const list = await requestClient.get<any[]>('/mes/modeling/work-center/list');
  workCenterOptions.value = list.map((c) => ({
    value: c.id,
    label: `${c.centerCode} ${c.centerName}`,
  }));
}

const columns = [
  { title: '物料', dataIndex: 'materialCode', width: 220 },
  { title: '版本', dataIndex: 'version', width: 70 },
  { title: '名称', dataIndex: 'routingName', width: 160 },
  { title: '状态', dataIndex: 'routingStatus', width: 90 },
  { title: '生效日期', dataIndex: 'effectiveFrom', width: 110 },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', key: 'action', width: 260 },
];

const opColumns = [
  { title: '工序号', dataIndex: 'operationNo', width: 90 },
  { title: '工序名称', dataIndex: 'operationName', width: 140 },
  { title: '类型', dataIndex: 'operationType', width: 110 },
  { title: '默认工作中心', dataIndex: 'workCenterId', width: 180 },
  { title: '准备(分)', dataIndex: 'setupTime', width: 90 },
  { title: '单件(分)', dataIndex: 'unitTime', width: 90 },
  { title: '报工方式', dataIndex: 'reportMode', width: 100 },
  { title: '质量门', dataIndex: 'qualityGate', width: 70 },
  { title: '操作', key: 'action', width: 60 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getRoutingPage({
      current: pagination.current,
      size: pagination.pageSize,
      materialId: filterMaterialId.value,
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

/** 编辑弹窗 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const isEdit = ref(false);
const formModel = reactive<Routing>({ materialId: '', operations: [] });

function emptyOperation(no: string): RoutingOperation {
  return {
    operationNo: no,
    operationName: '',
    operationType: 'PROCESS',
    setupTime: 0,
    unitTime: 0,
    waitTime: 0,
    reportMode: 'PIECE',
    qualityGate: '0',
    transferQty: 0,
  };
}

function nextOperationNo(): string {
  const max = (formModel.operations ?? [])
    .map((o) => Number.parseInt(o.operationNo, 10))
    .filter((n) => !Number.isNaN(n))
    .reduce((a, b) => Math.max(a, b), 0);
  return String(max + 10).padStart(4, '0');
}

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    materialId: undefined,
    version: undefined,
    routingName: '',
    routingStatus: 'DRAFT',
    effectiveFrom: undefined,
    remark: '',
    operations: [emptyOperation('0010')],
  });
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: Routing) {
  const detail = await getRoutingDetail(row.id!);
  Object.assign(formModel, detail);
  if (!formModel.operations?.length) {
    formModel.operations = [emptyOperation('0010')];
  }
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.materialId) {
    message.warning('请选择物料');
    return;
  }
  if (
    !formModel.operations?.length ||
    formModel.operations.some((o) => !o.operationNo || !o.operationName)
  ) {
    message.warning('请完整填写工序（工序号与名称）');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateRouting({ ...formModel })
      : saveRouting({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRelease(row: Routing) {
  await releaseRouting(row.id!);
  message.success('发布成功（同物料其他已发布版本已自动冻结）');
  await loadPage();
}

async function handleFreeze(row: Routing) {
  await freezeRouting(row.id!);
  message.success('已冻结');
  await loadPage();
}

async function handleCopy(row: Routing) {
  await copyRoutingVersion(row.id!);
  message.success('已复制为新草稿版本');
  await loadPage();
}

async function handleRemove(row: Routing) {
  await removeRoutings([row.id!]);
  message.success('删除成功');
  await loadPage();
}

onMounted(async () => {
  await Promise.all([loadPage(), searchMaterials(), loadWorkCenters()]);
});
</script>

<template>
  <Page title="工作计划" description="工艺路线与工序（决策 Q2：以 MES 维护为主，支持从工作计划生成工单）；版本化，草稿 → 发布 → 冻结">
    <Card>
      <template #extra>
        <Space>
          <Select
            v-model:value="filterMaterialId"
            style="width: 220px"
            placeholder="按物料过滤"
            show-search
            allow-clear
            :filter-option="false"
            :options="
              materialOptions.map((m) => ({
                value: m.id,
                label: `${m.materialCode} ${m.materialName}`,
              }))
            "
            @search="searchMaterials"
          />
          <Select
            v-model:value="statusFilter"
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
          <Button type="primary" @click="openCreate">新增工作计划</Button>
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
          <template v-else-if="column.dataIndex === 'version'">
            V{{ record.version }}
          </template>
          <template v-else-if="column.dataIndex === 'routingStatus'">
            <Tag :color="STATUS_META[record.routingStatus]?.color">
              {{ STATUS_META[record.routingStatus]?.label ?? record.routingStatus }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space>
              <Button size="small" type="link" @click="openEdit(record as any)">
                {{ record.routingStatus === 'DRAFT' ? '编辑' : '查看' }}
              </Button>
              <Popconfirm
                v-if="record.routingStatus === 'DRAFT'"
                title="发布后不可修改，确认发布？"
                @confirm="handleRelease(record as any)"
              >
                <Button size="small" type="link">发布</Button>
              </Popconfirm>
              <Popconfirm
                v-if="record.routingStatus === 'RELEASED'"
                title="确认冻结？"
                @confirm="handleFreeze(record as any)"
              >
                <Button size="small" type="link">冻结</Button>
              </Popconfirm>
              <Button size="small" type="link" @click="handleCopy(record as any)">
                复制新版
              </Button>
              <Popconfirm
                v-if="record.routingStatus === 'DRAFT'"
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
      :title="isEdit ? `工作计划 ${formModel.materialCode ?? ''} V${formModel.version ?? ''}` : '新增工作计划（草稿）'"
      :confirm-loading="modalSaving"
      width="1100px"
      :footer="formModel.routingStatus !== 'DRAFT' && isEdit ? null : undefined"
      @ok="handleSave"
    >
      <Form layout="inline" class="mb-4">
        <FormItem label="物料" required>
          <Select
            v-model:value="formModel.materialId as any"
            style="width: 260px"
            placeholder="搜索物料"
            show-search
            :disabled="isEdit"
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
        <FormItem label="名称">
          <Input v-model:value="formModel.routingName" placeholder="如 标准工艺" />
        </FormItem>
        <FormItem label="生效日期">
          <Input
            v-model:value="formModel.effectiveFrom"
            placeholder="YYYY-MM-DD"
            style="width: 130px"
          />
        </FormItem>
        <FormItem label="备注">
          <Input v-model:value="formModel.remark" placeholder="备注" />
        </FormItem>
      </Form>

      <Table
        :columns="opColumns"
        :data-source="formModel.operations"
        :pagination="false"
        row-key="operationNo"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'operationNo'">
            <Input v-model:value="record.operationNo" placeholder="0010" />
          </template>
          <template v-else-if="column.dataIndex === 'operationName'">
            <Input v-model:value="record.operationName" placeholder="工序名称" />
          </template>
          <template v-else-if="column.dataIndex === 'operationType'">
            <Select
              v-model:value="record.operationType"
              style="width: 100%"
              :options="OP_TYPE_OPTIONS"
            />
          </template>
          <template v-else-if="column.dataIndex === 'workCenterId'">
            <Select
              v-model:value="record.workCenterId"
              style="width: 100%"
              placeholder="选择工作中心"
              show-search
              allow-clear
              option-filter-prop="label"
              :options="workCenterOptions"
            />
          </template>
          <template v-else-if="column.dataIndex === 'setupTime'">
            <InputNumber v-model:value="record.setupTime" :min="0" style="width: 100%" />
          </template>
          <template v-else-if="column.dataIndex === 'unitTime'">
            <InputNumber v-model:value="record.unitTime" :min="0" style="width: 100%" />
          </template>
          <template v-else-if="column.dataIndex === 'reportMode'">
            <Select
              v-model:value="record.reportMode"
              style="width: 100%"
              :options="REPORT_MODE_OPTIONS"
            />
          </template>
          <template v-else-if="column.dataIndex === 'qualityGate'">
            <Switch
              :checked="record.qualityGate === '1'"
              @change="(v: any) => (record.qualityGate = v ? '1' : '0')"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button
              size="small"
              type="link"
              danger
              @click="formModel.operations?.splice(index, 1)"
            >
              删
            </Button>
          </template>
        </template>
      </Table>
      <Button
        v-if="!isEdit || formModel.routingStatus === 'DRAFT'"
        class="mt-2"
        block
        type="dashed"
        @click="formModel.operations?.push(emptyOperation(nextOperationNo()))"
      >
        + 添加工序
      </Button>
    </Modal>
  </Page>
</template>
