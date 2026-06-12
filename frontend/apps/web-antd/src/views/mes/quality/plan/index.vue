<script setup lang="ts">
import type { Material } from '#/api/mes/masterdata';
import type { QcCharacteristic, QcPlan } from '#/api/mes/quality';

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

import { getMaterialList } from '#/api/mes/masterdata';
import {
  getQcPlanDetail,
  getQcPlanPage,
  removeQcPlans,
  saveQcPlan,
  updateQcPlan,
} from '#/api/mes/quality';

defineOptions({ name: 'MesQualityPlan' });

const TYPE_META: Record<string, string> = {
  FIRST_ARTICLE: '首件',
  PROCESS: '过程(质量门)',
  FINAL: '终检',
  IQC: '来料',
  PATROL: '巡检',
};

const CHAR_TYPE_OPTIONS = [
  { label: '计量', value: 'QUANT' },
  { label: '计数', value: 'COUNT' },
  { label: '目视', value: 'VISUAL' },
];

const loading = ref(false);
const dataSource = ref<QcPlan[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const materialOptions = ref<Material[]>([]);

async function searchMaterials(keyword?: string) {
  materialOptions.value = await getMaterialList(keyword);
}

const columns = [
  { title: '编码', dataIndex: 'planCode', width: 140 },
  { title: '名称', dataIndex: 'planName' },
  { title: '类型', dataIndex: 'planType', width: 120 },
  { title: '适用工序', dataIndex: 'operationNo', width: 90 },
  { title: '抽样', dataIndex: 'samplingType', width: 110 },
  { title: '状态', dataIndex: 'status', width: 80 },
  { title: '操作', key: 'action', width: 140 },
];

const charColumns = [
  { title: '特性号', dataIndex: 'charNo', width: 90 },
  { title: '特性名称', dataIndex: 'charName', width: 140 },
  { title: '类型', dataIndex: 'charType', width: 100 },
  { title: '目标值', dataIndex: 'targetValue', width: 100 },
  { title: '下限', dataIndex: 'lowerLimit', width: 100 },
  { title: '上限', dataIndex: 'upperLimit', width: 100 },
  { title: '单位', dataIndex: 'unit', width: 80 },
  { title: 'CTQ', dataIndex: 'criticalFlag', width: 60 },
  { title: '操作', key: 'action', width: 60 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getQcPlanPage({
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
const formModel = reactive<QcPlan>({
  planCode: '',
  planName: '',
  characteristics: [],
});

function emptyChar(no: string): QcCharacteristic {
  return { charNo: no, charName: '', charType: 'QUANT', criticalFlag: '0' };
}

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    planCode: '',
    planName: '',
    planType: 'PROCESS',
    materialId: undefined,
    operationNo: '',
    samplingType: 'FULL',
    sampleSize: undefined,
    status: '0',
    remark: '',
    characteristics: [emptyChar('C01')],
  });
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: QcPlan) {
  const detail = await getQcPlanDetail(row.id!);
  Object.assign(formModel, detail);
  if (!formModel.characteristics?.length) {
    formModel.characteristics = [emptyChar('C01')];
  }
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.planCode || !formModel.planName) {
    message.warning('请填写编码与名称');
    return;
  }
  if (
    !formModel.characteristics?.length ||
    formModel.characteristics.some((c) => !c.charNo || !c.charName)
  ) {
    message.warning('请完整填写特性（特性号与名称）');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateQcPlan({ ...formModel })
      : saveQcPlan({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: QcPlan) {
  await removeQcPlans([row.id!]);
  message.success('删除成功');
  await loadPage();
}

onMounted(async () => {
  await Promise.all([loadPage(), searchMaterials()]);
});
</script>

<template>
  <Page title="检验计划" description="定义检验特性与公差；过程（质量门）计划按 物料+工序 匹配自动生成检验单">
    <Card>
      <template #extra>
        <Button type="primary" @click="openCreate">新增检验计划</Button>
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
            {{ TYPE_META[record.planType] ?? record.planType }}
          </template>
          <template v-else-if="column.dataIndex === 'samplingType'">
            {{ record.samplingType === 'FULL' ? '全检' : `固定 ${record.sampleSize ?? ''}` }}
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
      :title="isEdit ? '编辑检验计划' : '新增检验计划'"
      :confirm-loading="modalSaving"
      width="1000px"
      @ok="handleSave"
    >
      <Form layout="inline" class="mb-4">
        <FormItem label="编码" required>
          <Input v-model:value="formModel.planCode" :disabled="isEdit" placeholder="QP-001" />
        </FormItem>
        <FormItem label="名称" required>
          <Input v-model:value="formModel.planName" placeholder="终检计划" />
        </FormItem>
        <FormItem label="类型">
          <Select
            v-model:value="formModel.planType"
            style="width: 140px"
            :options="Object.entries(TYPE_META).map(([value, label]) => ({ value, label }))"
          />
        </FormItem>
        <FormItem label="适用物料">
          <Select
            v-model:value="formModel.materialId as any"
            style="width: 220px"
            placeholder="空=通用"
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
        </FormItem>
        <FormItem label="适用工序号">
          <Input v-model:value="formModel.operationNo" style="width: 100px" placeholder="如 0020" />
        </FormItem>
        <FormItem label="抽样">
          <Select
            v-model:value="formModel.samplingType"
            style="width: 100px"
            :options="[
              { label: '全检', value: 'FULL' },
              { label: '固定', value: 'FIXED' },
            ]"
          />
        </FormItem>
        <FormItem v-if="formModel.samplingType === 'FIXED'" label="抽样数">
          <InputNumber v-model:value="formModel.sampleSize" :min="1" />
        </FormItem>
      </Form>

      <Table
        :columns="charColumns"
        :data-source="formModel.characteristics"
        :pagination="false"
        row-key="charNo"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'charNo'">
            <Input v-model:value="record.charNo" />
          </template>
          <template v-else-if="column.dataIndex === 'charName'">
            <Input v-model:value="record.charName" placeholder="如 外径" />
          </template>
          <template v-else-if="column.dataIndex === 'charType'">
            <Select v-model:value="record.charType" style="width: 100%" :options="CHAR_TYPE_OPTIONS" />
          </template>
          <template v-else-if="column.dataIndex === 'targetValue'">
            <InputNumber v-model:value="record.targetValue" style="width: 100%" :disabled="record.charType !== 'QUANT'" />
          </template>
          <template v-else-if="column.dataIndex === 'lowerLimit'">
            <InputNumber v-model:value="record.lowerLimit" style="width: 100%" :disabled="record.charType !== 'QUANT'" />
          </template>
          <template v-else-if="column.dataIndex === 'upperLimit'">
            <InputNumber v-model:value="record.upperLimit" style="width: 100%" :disabled="record.charType !== 'QUANT'" />
          </template>
          <template v-else-if="column.dataIndex === 'unit'">
            <Input v-model:value="record.unit" />
          </template>
          <template v-else-if="column.dataIndex === 'criticalFlag'">
            <Switch
              :checked="record.criticalFlag === '1'"
              @change="(v: any) => (record.criticalFlag = v ? '1' : '0')"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button
              size="small"
              type="link"
              danger
              @click="formModel.characteristics?.splice(index, 1)"
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
          formModel.characteristics?.push(
            emptyChar(`C${String((formModel.characteristics?.length ?? 0) + 1).padStart(2, '0')}`),
          )
        "
      >
        + 添加特性
      </Button>
    </Modal>
  </Page>
</template>
