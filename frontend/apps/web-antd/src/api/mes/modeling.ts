import { requestClient } from '#/api/request';

/** 工厂结构节点类型 */
export type FactoryNodeType = 'AREA' | 'SITE' | 'WORK_CENTER' | 'WORKPLACE';

/** 工厂结构树节点 */
export interface FactoryTreeNode {
  id: string;
  parentId?: string;
  code: string;
  name: string;
  nodeType: FactoryNodeType;
  status?: string;
  children?: FactoryTreeNode[];
}

/** 分页参数 */
export interface PageParams {
  current?: number;
  size?: number;
  [key: string]: any;
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[];
  total: number;
  current: number;
  size: number;
}

/** 节点类型 → 后端资源路径 */
const NODE_PATH: Record<FactoryNodeType, string> = {
  SITE: '/mes/modeling/site',
  AREA: '/mes/modeling/area',
  WORK_CENTER: '/mes/modeling/work-center',
  WORKPLACE: '/mes/modeling/workplace',
};

/** 查询工厂结构树 */
export function getFactoryTree() {
  return requestClient.get<FactoryTreeNode[]>('/mes/modeling/site/tree');
}

/** 工厂结构节点通用 CRUD（按节点类型路由到对应资源） */
export function getNodeDetails(type: FactoryNodeType, id: string) {
  return requestClient.get(`${NODE_PATH[type]}/details/${id}`);
}

export function saveNode(type: FactoryNodeType, data: Record<string, any>) {
  return requestClient.post(NODE_PATH[type], data);
}

export function updateNode(type: FactoryNodeType, data: Record<string, any>) {
  return requestClient.put(NODE_PATH[type], data);
}

export function removeNodes(type: FactoryNodeType, ids: string[]) {
  return requestClient.delete(NODE_PATH[type], { data: ids });
}

/** 班次模型 */
export interface ShiftSegment {
  id?: string;
  shiftModelId?: string;
  segmentCode: string;
  segmentName: string;
  startTime: string;
  endTime: string;
  crossDay?: string;
  breakMinutes?: number;
  sortOrder?: number;
}

export interface ShiftModel {
  id?: string;
  modelCode: string;
  modelName: string;
  status?: string;
  remark?: string;
  segments?: ShiftSegment[];
}

export function getShiftModelPage(params: PageParams) {
  return requestClient.get<PageResult<ShiftModel>>(
    '/mes/modeling/shift-model/page',
    { params },
  );
}

export function getShiftModelList() {
  return requestClient.get<ShiftModel[]>('/mes/modeling/shift-model/list');
}

export function getShiftModelDetail(id: string) {
  return requestClient.get<ShiftModel>(`/mes/modeling/shift-model/details/${id}`);
}

export function saveShiftModel(data: ShiftModel) {
  return requestClient.post('/mes/modeling/shift-model', data);
}

export function updateShiftModel(data: ShiftModel) {
  return requestClient.put('/mes/modeling/shift-model', data);
}

export function removeShiftModels(ids: string[]) {
  return requestClient.delete('/mes/modeling/shift-model', { data: ids });
}

/** 工厂日历 */
export interface FactoryCalendar {
  id?: string;
  calendarCode: string;
  calendarName: string;
  siteId?: string;
  status?: string;
  remark?: string;
}

export interface CalendarDay {
  id?: string;
  calendarId: string;
  calDate: string;
  dayType: 'HOLIDAY' | 'REST' | 'WORK';
  shiftModelId?: string;
}

export interface CalendarDayBatch {
  calendarId: string;
  startDate: string;
  endDate: string;
  weekdays?: number[];
  dayType: string;
  shiftModelId?: string;
}

export function getCalendarPage(params: PageParams) {
  return requestClient.get<PageResult<FactoryCalendar>>(
    '/mes/modeling/calendar/page',
    { params },
  );
}

export function getCalendarList() {
  return requestClient.get<FactoryCalendar[]>('/mes/modeling/calendar/list');
}

export function saveCalendar(data: FactoryCalendar) {
  return requestClient.post('/mes/modeling/calendar', data);
}

export function updateCalendar(data: FactoryCalendar) {
  return requestClient.put('/mes/modeling/calendar', data);
}

export function removeCalendars(ids: string[]) {
  return requestClient.delete('/mes/modeling/calendar', { data: ids });
}

export function getCalendarDays(
  calendarId: string,
  startDate?: string,
  endDate?: string,
) {
  return requestClient.get<CalendarDay[]>('/mes/modeling/calendar/days', {
    params: { calendarId, startDate, endDate },
  });
}

export function batchSetCalendarDays(data: CalendarDayBatch) {
  return requestClient.post<number>('/mes/modeling/calendar/days/batch', data);
}
