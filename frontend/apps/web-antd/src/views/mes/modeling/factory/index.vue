<script setup lang="ts">
import type { FactoryNodeType, FactoryTreeNode } from '#/api/mes/modeling';

import { computed, onMounted, reactive, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Form,
  FormItem,
  Input,
  message,
  Modal,
  Popconfirm,
  Select,
  Space,
  Table,
  Tag,
  Tree,
} from 'ant-design-vue';

import {
  getFactoryTree,
  getNodeDetails,
  removeNodes,
  saveNode,
  updateNode,
} from '#/api/mes/modeling';

defineOptions({ name: 'MesModelingFactory' });

/** 节点类型显示与层级关系 */
const TYPE_META: Record<
  FactoryNodeType,
  { childType?: FactoryNodeType; color: string; label: string }
> = {
  SITE: { label: '工厂', color: 'blue', childType: 'AREA' },
  AREA: { label: '车间', color: 'green', childType: 'WORK_CENTER' },
  WORK_CENTER: { label: '工作中心', color: 'orange', childType: 'WORKPLACE' },
  WORKPLACE: { label: '工位', color: 'purple' },
};

const CENTER_TYPE_OPTIONS = [
  { label: '产线', value: 'LINE' },
  { label: '设备组', value: 'CELL' },
  { label: '班组', value: 'TEAM' },
  { label: '外协', value: 'OUTSOURCE' },
];

const WORKPLACE_TYPE_OPTIONS = [
  { label: '机加', value: 'MACHINE' },
  { label: '装配', value: 'ASSEMBLY' },
  { label: '检验', value: 'INSPECT' },
  { label: '返修', value: 'REWORK' },
  { label: '外协虚拟', value: 'VIRTUAL' },
];

/** 各类型编码/名称字段名（与后端实体一致） */
const FIELD_MAP: Record<FactoryNodeType, { code: string; name: string }> = {
  SITE: { code: 'siteCode', name: 'siteName' },
  AREA: { code: 'areaCode', name: 'areaName' },
  WORK_CENTER: { code: 'centerCode', name: 'centerName' },
  WORKPLACE: { code: 'workplaceCode', name: 'workplaceName' },
};

/** 各类型父级字段名 */
const PARENT_FIELD: Record<FactoryNodeType, string> = {
  SITE: '',
  AREA: 'siteId',
  WORK_CENTER: 'areaId',
  WORKPLACE: 'workCenterId',
};

const treeData = ref<FactoryTreeNode[]>([]);
const treeLoading = ref(false);
const selectedKeys = ref<string[]>([]);
const selectedNode = ref<FactoryTreeNode | null>(null);

const childType = computed<FactoryNodeType | undefined>(() =>
  selectedNode.value
    ? TYPE_META[selectedNode.value.nodeType].childType
    : 'SITE',
);

const childList = computed<FactoryTreeNode[]>(() =>
  selectedNode.value ? (selectedNode.value.children ?? []) : treeData.value,
);

const columns = [
  { title: '编码', dataIndex: 'code', key: 'code', width: 180 },
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '类型', dataIndex: 'nodeType', key: 'nodeType', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 },
  { title: '操作', key: 'action', width: 160 },
];

async function loadTree() {
  treeLoading.value = true;
  try {
    treeData.value = await getFactoryTree();
    // 重新定位选中节点（避免引用失效）
    if (selectedNode.value) {
      selectedNode.value = findNode(treeData.value, selectedNode.value.id);
    }
  } finally {
    treeLoading.value = false;
  }
}

function findNode(
  nodes: FactoryTreeNode[],
  id: string,
): FactoryTreeNode | null {
  for (const n of nodes) {
    if (n.id === id) return n;
    const found = findNode(n.children ?? [], id);
    if (found) return found;
  }
  return null;
}

function onSelect(keys: (number | string)[]) {
  const key = keys[0] as string;
  selectedNode.value = key ? findNode(treeData.value, key) : null;
}

/** 编辑弹窗 */
const modalOpen = ref(false);
const modalSaving = ref(false);
const editType = ref<FactoryNodeType>('SITE');
const isEdit = ref(false);
const formModel = reactive<Record<string, any>>({});

function resetForm() {
  Object.keys(formModel).forEach((k) => delete formModel[k]);
}

function openCreate() {
  if (!childType.value) return;
  resetForm();
  editType.value = childType.value;
  // 挂到当前选中节点下
  const parentField = PARENT_FIELD[editType.value];
  if (parentField && selectedNode.value) {
    formModel[parentField] = selectedNode.value.id;
  }
  formModel.status = '0';
  isEdit.value = false;
  modalOpen.value = true;
}

async function openEdit(row: FactoryTreeNode) {
  resetForm();
  editType.value = row.nodeType;
  const detail = await getNodeDetails(row.nodeType, row.id);
  Object.assign(formModel, detail);
  isEdit.value = true;
  modalOpen.value = true;
}

async function handleSave() {
  const fields = FIELD_MAP[editType.value];
  if (!formModel[fields.code] || !formModel[fields.name]) {
    message.warning('请填写编码与名称');
    return;
  }
  modalSaving.value = true;
  try {
    await (isEdit.value
      ? updateNode(editType.value, { ...formModel })
      : saveNode(editType.value, { ...formModel }));
    message.success(isEdit.value ? '修改成功' : '新增成功');
    modalOpen.value = false;
    await loadTree();
  } finally {
    modalSaving.value = false;
  }
}

async function handleRemove(row: FactoryTreeNode) {
  await removeNodes(row.nodeType, [row.id]);
  message.success('删除成功');
  await loadTree();
}

onMounted(loadTree);
</script>

<template>
  <Page title="工厂结构" description="工厂 → 车间 → 工作中心 → 工位 四级建模">
    <div class="flex gap-4">
      <Card class="w-1/3" title="结构树" :loading="treeLoading">
        <template #extra>
          <Button size="small" @click="loadTree">刷新</Button>
        </template>
        <Tree
          v-model:selected-keys="selectedKeys"
          :tree-data="treeData as any"
          :field-names="{ title: 'name', key: 'id', children: 'children' }"
          default-expand-all
          @select="onSelect"
        >
          <template #title="{ name, code, nodeType }">
            <Tag :color="TYPE_META[nodeType as FactoryNodeType].color">
              {{ TYPE_META[nodeType as FactoryNodeType].label }}
            </Tag>
            <span>{{ code }} {{ name }}</span>
          </template>
        </Tree>
      </Card>

      <Card class="flex-1" :title="selectedNode ? `${selectedNode.name} 的下级` : '工厂列表'">
        <template #extra>
          <Button v-if="childType" type="primary" @click="openCreate">
            新增{{ TYPE_META[childType].label }}
          </Button>
        </template>
        <Table
          :columns="columns"
          :data-source="childList"
          :pagination="false"
          row-key="id"
          size="middle"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'nodeType'">
              <Tag :color="TYPE_META[record.nodeType as FactoryNodeType].color">
                {{ TYPE_META[record.nodeType as FactoryNodeType].label }}
              </Tag>
            </template>
            <template v-else-if="column.key === 'status'">
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
    </div>

    <Modal
      v-model:open="modalOpen"
      :title="`${isEdit ? '编辑' : '新增'}${TYPE_META[editType].label}`"
      :confirm-loading="modalSaving"
      @ok="handleSave"
    >
      <Form layout="vertical">
        <FormItem label="编码" required>
          <Input
            v-model:value="formModel[FIELD_MAP[editType].code]"
            :disabled="isEdit"
            placeholder="请输入编码"
          />
        </FormItem>
        <FormItem label="名称" required>
          <Input
            v-model:value="formModel[FIELD_MAP[editType].name]"
            placeholder="请输入名称"
          />
        </FormItem>
        <FormItem v-if="editType === 'WORK_CENTER'" label="工作中心类型">
          <Select
            v-model:value="formModel.centerType"
            :options="CENTER_TYPE_OPTIONS"
            placeholder="请选择类型"
          />
        </FormItem>
        <FormItem v-if="editType === 'WORKPLACE'" label="工位类型">
          <Select
            v-model:value="formModel.workplaceType"
            :options="WORKPLACE_TYPE_OPTIONS"
            placeholder="请选择类型"
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
  </Page>
</template>
