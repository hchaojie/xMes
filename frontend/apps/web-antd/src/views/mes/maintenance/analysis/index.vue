<script setup lang="ts">
import type { Dayjs } from 'dayjs';

import type { MntAnalysis } from '#/api/mes/maintenance';

import { onMounted, ref } from 'vue';

import { Page } from '@vben/common-ui';

import {
  Button,
  Card,
  Col,
  Progress,
  RangePicker,
  Row,
  Statistic,
  Table,
} from 'ant-design-vue';
import dayjs from 'dayjs';

import { getMntAnalysis } from '#/api/mes/maintenance';

defineOptions({ name: 'MesMaintenanceAnalysis' });

const loading = ref(false);
const range = ref<[Dayjs, Dayjs]>([dayjs().subtract(29, 'day'), dayjs()]);
const data = ref<MntAnalysis>();

const wpColumns = [
  { title: '设备编码', dataIndex: 'workplaceCode', width: 120 },
  { title: '设备名称', dataIndex: 'workplaceName' },
  { title: '故障次数', dataIndex: 'failureCount', width: 100 },
  { title: '停机时长(分)', dataIndex: 'downtimeMinutes', width: 120 },
  { title: 'MTTR(分)', dataIndex: 'mttrMinutes', width: 100 },
  { title: 'MTBF(分)', dataIndex: 'mtbfMinutes', width: 110 },
];

const paretoColumns = [
  { title: '故障原因', dataIndex: 'cause' },
  { title: '次数', dataIndex: 'count', width: 90 },
];

async function load() {
  loading.value = true;
  try {
    data.value = await getMntAnalysis(
      range.value[0].format('YYYY-MM-DD'),
      range.value[1].format('YYYY-MM-DD'),
    );
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<template>
  <Page
    title="维护分析"
    description="MTBF / MTTR、故障 Pareto 与保养计划达成率（口径：期间内报修/生成的维护工单）"
  >
    <Card class="mb-4">
      <RangePicker v-model:value="range" />
      <Button class="ml-2" type="primary" :loading="loading" @click="load">
        查询
      </Button>
    </Card>

    <Row :gutter="16" class="mb-4">
      <Col :span="8">
        <Card>
          <Statistic title="保养/点检工单数" :value="data?.plannedTotal ?? 0" />
        </Card>
      </Col>
      <Col :span="8">
        <Card>
          <Statistic title="按期关闭数" :value="data?.plannedOnTime ?? 0" />
        </Card>
      </Col>
      <Col :span="8">
        <Card>
          <div class="ant-statistic-title">保养计划达成率</div>
          <Progress
            :percent="data?.planAchievementRate ?? 0"
            :status="(data?.planAchievementRate ?? 0) >= 90 ? 'success' : 'active'"
          />
        </Card>
      </Col>
    </Row>

    <Row :gutter="16">
      <Col :span="14">
        <Card title="设备 MTBF / MTTR">
          <Table
            :columns="wpColumns"
            :data-source="data?.byWorkplace ?? []"
            :loading="loading"
            :pagination="false"
            row-key="workplaceId"
            size="small"
          />
        </Card>
      </Col>
      <Col :span="10">
        <Card title="故障 Pareto（按原因）">
          <Table
            :columns="paretoColumns"
            :data-source="data?.faultPareto ?? []"
            :loading="loading"
            :pagination="false"
            row-key="cause"
            size="small"
          />
        </Card>
      </Col>
    </Row>
  </Page>
</template>
