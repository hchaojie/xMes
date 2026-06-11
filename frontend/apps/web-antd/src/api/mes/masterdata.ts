import type { PageParams, PageResult } from '#/api/mes/modeling';

import { requestClient } from '#/api/request';

/** 物料 */
export interface Material {
  id?: string;
  materialCode: string;
  materialName: string;
  materialType?: 'AUX' | 'FINISHED' | 'RAW' | 'SEMI';
  spec?: string;
  unit: string;
  lotStrategy?: 'LOT' | 'NONE' | 'SERIAL';
  shelfLifeDays?: number;
  status?: string;
  remark?: string;
}

export function getMaterialPage(params: PageParams) {
  return requestClient.get<PageResult<Material>>(
    '/mes/masterdata/material/page',
    { params },
  );
}

export function getMaterialList(keyword?: string) {
  return requestClient.get<Material[]>('/mes/masterdata/material/list', {
    params: { keyword },
  });
}

export function saveMaterial(data: Material) {
  return requestClient.post('/mes/masterdata/material', data);
}

export function updateMaterial(data: Material) {
  return requestClient.put('/mes/masterdata/material', data);
}

export function removeMaterials(ids: string[]) {
  return requestClient.delete('/mes/masterdata/material', { data: ids });
}

/** 版本化文档状态 */
export type DocStatus = 'DRAFT' | 'FROZEN' | 'RELEASED';

/** BOM */
export interface BomLine {
  id?: string;
  bomId?: string;
  childMaterialId: string;
  quantity: number;
  lossRate?: number;
  consumeOperationNo?: string;
  keyFlag?: string;
  remark?: string;
}

export interface Bom {
  id?: string;
  materialId: string;
  materialCode?: string;
  materialName?: string;
  version?: number;
  bomStatus?: DocStatus;
  effectiveFrom?: string;
  effectiveTo?: string;
  remark?: string;
  lines?: BomLine[];
}

export function getBomPage(params: PageParams) {
  return requestClient.get<PageResult<Bom>>('/mes/masterdata/bom/page', {
    params,
  });
}

export function getBomDetail(id: string) {
  return requestClient.get<Bom>(`/mes/masterdata/bom/details/${id}`);
}

export function saveBom(data: Bom) {
  return requestClient.post('/mes/masterdata/bom', data);
}

export function updateBom(data: Bom) {
  return requestClient.put('/mes/masterdata/bom', data);
}

export function releaseBom(id: string) {
  return requestClient.put(`/mes/masterdata/bom/release/${id}`);
}

export function freezeBom(id: string) {
  return requestClient.put(`/mes/masterdata/bom/freeze/${id}`);
}

export function copyBomVersion(id: string) {
  return requestClient.post<string>(`/mes/masterdata/bom/copy/${id}`);
}

export function removeBoms(ids: string[]) {
  return requestClient.delete('/mes/masterdata/bom', { data: ids });
}

/** 工作计划 */
export interface RoutingOperation {
  id?: string;
  routingId?: string;
  operationNo: string;
  operationName: string;
  operationType?: string;
  workCenterId?: string;
  setupTime?: number;
  unitTime?: number;
  waitTime?: number;
  reportMode?: string;
  qualityGate?: string;
  transferQty?: number;
  remark?: string;
}

export interface Routing {
  id?: string;
  materialId: string;
  materialCode?: string;
  materialName?: string;
  version?: number;
  routingName?: string;
  routingStatus?: DocStatus;
  effectiveFrom?: string;
  effectiveTo?: string;
  remark?: string;
  operations?: RoutingOperation[];
}

export function getRoutingPage(params: PageParams) {
  return requestClient.get<PageResult<Routing>>(
    '/mes/masterdata/routing/page',
    { params },
  );
}

export function getRoutingDetail(id: string) {
  return requestClient.get<Routing>(`/mes/masterdata/routing/details/${id}`);
}

export function saveRouting(data: Routing) {
  return requestClient.post('/mes/masterdata/routing', data);
}

export function updateRouting(data: Routing) {
  return requestClient.put('/mes/masterdata/routing', data);
}

export function releaseRouting(id: string) {
  return requestClient.put(`/mes/masterdata/routing/release/${id}`);
}

export function freezeRouting(id: string) {
  return requestClient.put(`/mes/masterdata/routing/freeze/${id}`);
}

export function copyRoutingVersion(id: string) {
  return requestClient.post<string>(`/mes/masterdata/routing/copy/${id}`);
}

export function removeRoutings(ids: string[]) {
  return requestClient.delete('/mes/masterdata/routing', { data: ids });
}
