import type { PageParams, PageResult } from '#/api/mes/modeling';

import { requestClient } from '#/api/request';

/** 维护计划标准作业项 */
export interface MntPlanItem {
  id?: string;
  planId?: string;
  itemNo: string;
  itemName: string;
  standardDesc?: string;
  methodDesc?: string;
}

/** 维护计划 */
export interface MntPlan {
  id?: string;
  planCode: string;
  planName: string;
  planType?: 'PM' | 'SPOT';
  workplaceId?: string;
  workplaceCode?: string;
  workplaceName?: string;
  cycleType?: 'DAY' | 'OUTPUT' | 'RUNTIME';
  cycleValue?: number;
  nextDueDate?: string;
  lastOrderDate?: string;
  status?: string;
  remark?: string;
  items?: MntPlanItem[];
}

/** 维护工单检查项结果 */
export interface MntOrderItem {
  id?: string;
  orderId?: string;
  itemNo?: string;
  itemName?: string;
  standardDesc?: string;
  methodDesc?: string;
  checkResult?: 'NG' | 'OK';
  resultRemark?: string;
  repairOrderId?: string;
}

/** 维护工单 */
export interface MntOrder {
  id?: string;
  orderNo?: string;
  orderType?: 'PM' | 'REPAIR' | 'SPOT';
  planId?: string;
  sourceOrderId?: string;
  workplaceId?: string;
  workplaceCode?: string;
  workplaceName?: string;
  orderStatus?: string;
  dueDate?: string;
  faultDesc?: string;
  faultCause?: string;
  actionTaken?: string;
  spareParts?: string;
  reportBy?: string;
  reportTime?: string;
  acceptBy?: string;
  acceptTime?: string;
  finishTime?: string;
  closeBy?: string;
  closeTime?: string;
  downtimeMinutes?: number;
  remark?: string;
  items?: MntOrderItem[];
}

/** 维护分析 */
export interface MntAnalysis {
  byWorkplace: Array<{
    downtimeMinutes: number;
    failureCount: number;
    mtbfMinutes?: number;
    mttrMinutes?: number;
    workplaceCode?: string;
    workplaceId: string;
    workplaceName?: string;
  }>;
  faultPareto: Array<{ cause: string; count: number }>;
  planAchievementRate?: number;
  plannedOnTime: number;
  plannedTotal: number;
}

/** 维护计划 CRUD */
export function getMntPlanPage(params: PageParams) {
  return requestClient.get<PageResult<MntPlan>>('/mes/maintenance/plan/page', {
    params,
  });
}

export function getMntPlanDetail(id: string) {
  return requestClient.get<MntPlan>(`/mes/maintenance/plan/details/${id}`);
}

export function saveMntPlan(data: MntPlan) {
  return requestClient.post('/mes/maintenance/plan', data);
}

export function updateMntPlan(data: MntPlan) {
  return requestClient.put('/mes/maintenance/plan', data);
}

export function removeMntPlans(ids: string[]) {
  return requestClient.delete('/mes/maintenance/plan', { data: ids });
}

export function generateDueOrders() {
  return requestClient.post<number>('/mes/maintenance/plan/generate-due');
}

/** 维护工单 */
export function getMntOrderPage(params: PageParams & Partial<MntOrder>) {
  return requestClient.get<PageResult<MntOrder>>(
    '/mes/maintenance/order/page',
    { params },
  );
}

export function getMntOrderDetail(id: string) {
  return requestClient.get<MntOrder>(`/mes/maintenance/order/details/${id}`);
}

export function reportRepair(data: MntOrder) {
  return requestClient.post<string>('/mes/maintenance/order/report', data);
}

export function acceptMntOrder(id: string) {
  return requestClient.post(`/mes/maintenance/order/accept/${id}`);
}

export function finishMntOrder(data: MntOrder) {
  return requestClient.post('/mes/maintenance/order/finish', data);
}

export function closeMntOrder(id: string) {
  return requestClient.post(`/mes/maintenance/order/close/${id}`);
}

export function cancelMntOrder(id: string) {
  return requestClient.post(`/mes/maintenance/order/cancel/${id}`);
}

/** 维护分析 */
export function getMntAnalysis(startDate: string, endDate: string) {
  return requestClient.get<MntAnalysis>('/mes/maintenance/order/analysis', {
    params: { startDate, endDate },
  });
}
