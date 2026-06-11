import type { PageParams, PageResult } from '#/api/mes/modeling';

import { requestClient } from '#/api/request';

/** 工单状态 */
export type OrderStatus =
  | 'CANCELLED'
  | 'CLOSED'
  | 'COMPLETED'
  | 'CREATED'
  | 'HOLD'
  | 'IN_PROGRESS'
  | 'RELEASED';

/** 作业 */
export interface WoTask {
  id?: string;
  orderId?: string;
  operationNo: string;
  operationName?: string;
  operationType?: string;
  taskStatus?: string;
  workCenterId?: string;
  workplaceId?: string;
  planStart?: string;
  planEnd?: string;
  qtyTarget?: number;
  qtyGood?: number;
  qtyScrap?: number;
  qtyRework?: number;
  setupTime?: number;
  unitTime?: number;
  reportMode?: string;
  qualityGate?: string;
}

/** 工单 */
export interface WoOrder {
  id?: string;
  orderNo?: string;
  orderType?: string;
  sourceType?: string;
  materialId: string;
  materialCode?: string;
  materialName?: string;
  quantity: number;
  unit?: string;
  planStartDate?: string;
  planEndDate?: string;
  priority?: number;
  orderStatus?: OrderStatus;
  routingId?: string;
  routingVersion?: number;
  erpOrderNo?: string;
  qtyGood?: number;
  qtyScrap?: number;
  qtyRework?: number;
  holdReason?: string;
  remark?: string;
  tasks?: WoTask[];
}

export function getWorkOrderPage(params: PageParams) {
  return requestClient.get<PageResult<WoOrder>>('/mes/order/work-order/page', {
    params,
  });
}

export function getWorkOrderDetail(id: string) {
  return requestClient.get<WoOrder>(`/mes/order/work-order/details/${id}`);
}

export function createWorkOrder(data: WoOrder) {
  return requestClient.post<string>('/mes/order/work-order', data);
}

export function releaseWorkOrders(ids: string[]) {
  return requestClient.put('/mes/order/work-order/release', ids);
}

export function holdWorkOrder(id: string, reason: string) {
  return requestClient.put(`/mes/order/work-order/hold/${id}`, null, {
    params: { reason },
  });
}

export function resumeWorkOrder(id: string) {
  return requestClient.put(`/mes/order/work-order/resume/${id}`);
}

export function cancelWorkOrder(id: string) {
  return requestClient.put(`/mes/order/work-order/cancel/${id}`);
}

export function removeWorkOrders(ids: string[]) {
  return requestClient.delete('/mes/order/work-order', { data: ids });
}

/** 报工事件 */
export interface WoBooking {
  id: string;
  taskId: string;
  orderId: string;
  bookingType: 'FINISH' | 'PAUSE' | 'QTY' | 'RESUME' | 'REVERSE' | 'START';
  qtyGood?: number;
  qtyScrap?: number;
  qtyRework?: number;
  reasonCode?: string;
  workplaceId?: string;
  personName?: string;
  bookingTime: string;
  source?: string;
  reverseOfId?: string;
  remark?: string;
}

/** 报数/完工参数 */
export interface BookingQty {
  taskId: string;
  qtyGood?: number;
  qtyScrap?: number;
  qtyRework?: number;
  reasonCode?: string;
  workplaceId?: string;
  remark?: string;
  source?: string;
}

export function startTask(taskId: string, workplaceId?: string, source?: string) {
  return requestClient.post(`/mes/order/booking/start/${taskId}`, null, {
    params: { workplaceId, source },
  });
}

export function pauseTask(taskId: string, reasonCode: string, source?: string) {
  return requestClient.post(`/mes/order/booking/pause/${taskId}`, null, {
    params: { reasonCode, source },
  });
}

export function resumeTask(taskId: string, source?: string) {
  return requestClient.post(`/mes/order/booking/resume/${taskId}`, null, {
    params: { source },
  });
}

export function reportTaskQty(data: BookingQty) {
  return requestClient.post('/mes/order/booking/qty', data);
}

export function finishTask(data: BookingQty) {
  return requestClient.post('/mes/order/booking/finish', data);
}

export function reverseBooking(bookingId: string, remark?: string) {
  return requestClient.post(`/mes/order/booking/reverse/${bookingId}`, null, {
    params: { remark },
  });
}

export function getTaskBookings(taskId: string) {
  return requestClient.get<WoBooking[]>(`/mes/order/booking/task/${taskId}`);
}
