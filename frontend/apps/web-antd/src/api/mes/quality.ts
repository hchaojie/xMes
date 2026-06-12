import type { PageParams, PageResult } from '#/api/mes/modeling';

import { requestClient } from '#/api/request';

/** 检验特性 */
export interface QcCharacteristic {
  id?: string;
  planId?: string;
  charNo: string;
  charName: string;
  charType?: 'COUNT' | 'QUANT' | 'VISUAL';
  targetValue?: number;
  upperLimit?: number;
  lowerLimit?: number;
  unit?: string;
  criticalFlag?: string;
}

/** 检验计划 */
export interface QcPlan {
  id?: string;
  planCode: string;
  planName: string;
  planType?: string;
  materialId?: string;
  materialCode?: string;
  operationNo?: string;
  samplingType?: 'FIXED' | 'FULL';
  sampleSize?: number;
  status?: string;
  remark?: string;
  characteristics?: QcCharacteristic[];
}

/** 检验单 */
export interface QcInspectionOrder {
  id: string;
  inspectionNo: string;
  planId?: string;
  planType?: string;
  woOrderId?: string;
  taskId?: string;
  orderNo?: string;
  materialCode?: string;
  materialName?: string;
  operationNo?: string;
  inspectStatus: string;
  sampleQty?: number;
  inspector?: string;
  judgeTime?: string;
  judgeRemark?: string;
  sourceType?: string;
  characteristics?: QcCharacteristic[];
  results?: QcInspectionResult[];
}

/** 检验结果 */
export interface QcInspectionResult {
  id?: string;
  inspectionOrderId: string;
  characteristicId: string;
  charName?: string;
  charType?: string;
  targetValue?: number;
  upperLimit?: number;
  lowerLimit?: number;
  sampleNo?: number;
  measuredValue?: number;
  judge?: string;
  remark?: string;
}

/** 检验计划 CRUD */
export function getQcPlanPage(params: PageParams) {
  return requestClient.get<PageResult<QcPlan>>('/mes/quality/plan/page', {
    params,
  });
}

export function getQcPlanDetail(id: string) {
  return requestClient.get<QcPlan>(`/mes/quality/plan/details/${id}`);
}

export function saveQcPlan(data: QcPlan) {
  return requestClient.post('/mes/quality/plan', data);
}

export function updateQcPlan(data: QcPlan) {
  return requestClient.put('/mes/quality/plan', data);
}

export function removeQcPlans(ids: string[]) {
  return requestClient.delete('/mes/quality/plan', { data: ids });
}

/** 检验工作台 */
export function refreshGateOrders() {
  return requestClient.post<number>('/mes/quality/inspection/refresh-gate');
}

export function getInspectionPage(params: PageParams) {
  return requestClient.get<PageResult<QcInspectionOrder>>(
    '/mes/quality/inspection/page',
    { params },
  );
}

export function getInspectionDetail(id: string) {
  return requestClient.get<QcInspectionOrder>(
    `/mes/quality/inspection/details/${id}`,
  );
}

export function receiveInspection(id: string) {
  return requestClient.post(`/mes/quality/inspection/receive/${id}`);
}

export function recordResults(results: QcInspectionResult[]) {
  return requestClient.post('/mes/quality/inspection/record', results);
}

export function judgeInspection(
  id: string,
  judge: string,
  judgeRemark?: string,
) {
  return requestClient.post(`/mes/quality/inspection/judge/${id}`, null, {
    params: { judge, judgeRemark },
  });
}
