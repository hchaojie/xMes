<script setup lang="ts">
import type { Bom, BomLine, Material } from '#/api/mes/masterdata';

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
  copyBomVersion,
  freezeBom,
  getBomDetail,
  getBomPage,
  getMaterialList,
  releaseBom,
  removeBoms,
  saveBom,
  updateBom,
} from '#/api/mes/masterdata';

defineOptions({ name: 'MesMasterdataBom' });

const STATUS_META: Record<string, { color: string; label: string }> = {
  DRAFT: { label: '草稿', color: 'default' },
  RELEASED: { label: '已发布', color: 'success' },
  FROZEN: { label: '已冻结', color: 'error' },
};

const loading = ref(false);
const dataSource = ref<Bom[]>([]);
const pagination = reactive({ current: 1, pageSize: 10, total: 0 });
const statusFilter = ref<string | undefined>();

/** 物料远程搜索选择项（页头过滤与表单共用加载逻辑） */
const materialOptions = ref<Material[]>([]);
const filterMaterialId = ref<string | undefined>();

async function searchMaterials(keyword?: string) {
  materialOptions.value = await getMaterialList(keyword);
}

const columns = [
  { title: '父物料', dataIndex: 'materialCode', width: 220 },
  { title: '版本', dataIndex: 'version', width: 70 },
  { title: '状态', dataIndex: 'bomStatus', width: 90 },
  { title: '生效日期', dataIndex: 'effectiveFrom', width: 110 },
  { title: '失效日期', dataIndex: 'effectiveTo', width: 110 },
  { title: '备注', dataIndex: 'remark' },
  { title: '操作', key: 'action', width: 260 },
];

const lineColumns = [
  { title: '子物料', dataIndex: 'childMaterialId', width: 240 },
  { title: '用量', dataIndex: 'quantity', width: 110 },
  { title: '损耗率%', dataIndex: 'lossRate', width: 100 },
  { title: '消耗工序号', dataIndex: 'consumeOperationNo', width: 110 },
  { title: '关键件', dataIndex: 'keyFlag', width: 80 },
  { title: '操作', key: 'action', width: 70 },
];

async function loadPage() {
  loading.value = true;
  try {
    const res = await getBomPage({
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
const formModel = reactive<Bom>({ materialId: '', lines: [] });

function emptyLine(): BomLine {
  return { childMaterialId: '', quantity: 1, lossRate: 0, keyFlag: '0' };
}

function openCreate() {
  Object.assign(formModel, {
    id: undefined,
    materialId: undefined,
    version: undefined,
    bomStatus: 'DRAFT',
    effectiveFrom: undefined,
    effectiveTo: undefined,
    remark: '',
    lines: [emptyLine()],
  });
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: Bom) {
  const detail = await getBomDetail(row.id!);
  Object.assign(formModel, detail);
  if (!formModel.lines?.length) formModel.lines = [emptyLine()];
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  if (!formModel.materialId) {
    message.warning('请选择父物料');
    return;
  }
  if (!formModel.lines?.length || formModel.lines.some((l) => !l.childMaterialId || !l.quantity)) {
    message.warning('请完整填写 BOM 行（子物料与用量）');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value ? updateBom({ ...formModel }) : saveBom({ ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadPage();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRelease(row: Bom) {
  await releaseBom(row.id!);
  message.success('发布成功（同物料其他已发布版本已自动冻结）');
  await loadPage();
}

async function handleFreeze(row: Bom) {
  await freezeBom(row.id!);
  message.success('已冻结');
  await loadPage();
}

async function handleCopy(row: Bom) {
  await copyBomVersion(row.id!);
  message.success('已复制为新草稿版本');
  await loadPage();
}

async function handleRemove(row: Bom) {
  await removeBoms([row.id!]);
  message.success('删除成功');
  await loadPage();
}

onMounted(async () => {
  await Promise.all([loadPage(), searchMaterials()]);
});
</script>

<template>
  <Page title="BOM 管理" description="制造 BOM（按工序挂料）：版本化，草稿 → 发布 → 冻结；发布时同物料旧版本自动冻结">
    <Card>
      <template #extra>
        <Space>
          <Select
            v-model:value="filterMaterialId"
            style="width: 220px"
            placeholder="按父物料过滤"
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
          <Button type="primary" @click="openCreate">新增 BOM</Button>
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
          <template v-else-if="column.dataIndex === 'bomStatus'">
            <Tag :color="STATUS_META[record.bomStatus]?.color">
              {{ STATUS_META[record.bomStatus]?.label ?? record.bomStatus }}
            </Tag>
          </template>
          <template v-else-if="column.key === 'action'">
            <Space>
              <Button
                v-if="record.bomStatus === 'DRAFT'"
                size="small"
                type="link"
                @click="openEdit(record as any)"
              >
                编辑
              </Button>
              <Button v-else size="small" type="link" @click="openEdit(record as any)">
                查看
              </Button>
              <Popconfirm
                v-if="record.bomStatus === 'DRAFT'"
                title="发布后不可修改，确认发布？"
                @confirm="handleRelease(record as any)"
              >
                <Button size="small" type="link">发布</Button>
              </Popconfirm>
              <Popconfirm
                v-if="record.bomStatus === 'RELEASED'"
                title="确认冻结？"
                @confirm="handleFreeze(record as any)"
              >
                <Button size="small" type="link">冻结</Button>
              </Popconfirm>
              <Button size="small" type="link" @click="handleCopy(record as any)">
                复制新版
              </Button>
              <Popconfirm
                v-if="record.bomStatus === 'DRAFT'"
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
      :title="isEdit ? `BOM ${formModel.materialCode ?? ''} V${formModel.version ?? ''}` : '新增 BOM（草稿）'"
      :confirm-loading="modalSaving"
      width="960px"
      :footer="formModel.bomStatus !== 'DRAFT' && isEdit ? null : undefined"
      @ok="handleSave"
    >
      <Form layout="inline" class="mb-4">
        <FormItem label="父物料" required>
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
        :columns="lineColumns"
        :data-source="formModel.lines"
        :pagination="false"
        row-key="childMaterialId"
        size="small"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.dataIndex === 'childMaterialId'">
            <Select
              v-model:value="record.childMaterialId"
              style="width: 100%"
              placeholder="搜索子物料"
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
          </template>
          <template v-else-if="column.dataIndex === 'quantity'">
            <InputNumber v-model:value="record.quantity" :min="0" style="width: 100%" />
          </template>
          <template v-else-if="column.dataIndex === 'lossRate'">
            <InputNumber v-model:value="record.lossRate" :min="0" :max="100" style="width: 100%" />
          </template>
          <template v-else-if="column.dataIndex === 'consumeOperationNo'">
            <Input v-model:value="record.consumeOperationNo" placeholder="如 0010" />
          </template>
          <template v-else-if="column.dataIndex === 'keyFlag'">
            <Switch
              :checked="record.keyFlag === '1'"
              @change="(v: any) => (record.keyFlag = v ? '1' : '0')"
            />
          </template>
          <template v-else-if="column.key === 'action'">
            <Button
              size="small"
              type="link"
              danger
              @click="formModel.lines?.splice(index, 1)"
            >
              删除
            </Button>
          </template>
        </template>
      </Table>
      <Button
        v-if="!isEdit || formModel.bomStatus === 'DRAFT'"
        class="mt-2"
        block
        type="dashed"
        @click="formModel.lines?.push(emptyLine())"
      >
        + 添加 BOM 行
      </Button>
    </Modal>
  </Page>
</template>
