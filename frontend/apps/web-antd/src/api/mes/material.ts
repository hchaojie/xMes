import type { PageParams, PageResult } from '#/api/mes/modeling';

import { requestClient } from '#/api/request';

/** 物料批次 */
export interface MtLot {
  id?: string;
  lotNo?: string;
  materialId: string;
  materialCode?: string;
  materialName?: string;
  qty: number;
  unit?: string;
  lotStatus?: 'AVAILABLE' | 'CONSUMED' | 'FROZEN' | 'HOLD';
  sourceType?: 'PRODUCTION' | 'PURCHASE' | 'SPLIT';
  woOrderId?: string;
  supplierLotNo?: string;
  prodDate?: string;
  expireDate?: string;
  remark?: string;
}

/** 上料消耗记录 */
export interface MtLotConsume {
  id: string;
  taskId: string;
  orderId?: string;
  lotNo?: string;
  materialCode?: string;
  qty: number;
  personName?: string;
  feedTime?: string;
}

export function getLotPage(params: PageParams) {
  return requestClient.get<PageResult<MtLot>>('/mes/material/lot/page', {
    params,
  });
}

export function registerLot(data: MtLot) {
  return requestClient.post<string>('/mes/material/lot', data);
}

export function changeLotStatus(id: string, status: string) {
  return requestClient.put(`/mes/material/lot/status/${id}`, null, {
    params: { status },
  });
}

export function feedLot(taskId: string, lotNo: string, qty: number) {
  return requestClient.post('/mes/material/lot/feed', null, {
    params: { taskId, lotNo, qty },
  });
}

export function getTaskConsumes(taskId: string) {
  return requestClient.get<MtLotConsume[]>(
    `/mes/material/lot/consumes/${taskId}`,
  );
}

export function refreshOutputs() {
  return requestClient.post<number>('/mes/material/trace/refresh-output');
}

export function traceBackward(no: string) {
  return requestClient.get<Record<string, any>>(
    '/mes/material/trace/backward',
    { params: { no } },
  );
}

export function traceForward(lotNo: string) {
  return requestClient.get<Record<string, any>>(
    '/mes/material/trace/forward',
    { params: { lotNo } },
  );
}
