import type { UserInfo } from '@vben/types';

import { requestClient } from '#/api/request';

/**
 * 获取用户信息：pig /admin/user/info，映射为 vben UserInfo
 */
export async function getUserInfoApi() {
  const d = await requestClient.get<any>('/admin/user/info');
  return {
    avatar: d?.avatar ?? '',
    realName: d?.name || d?.nickname || d?.username || '',
    roles: (d?.roleList ?? []).map((r: any) => r.roleCode),
    userId: d?.userId,
    username: d?.username,
  } as UserInfo;
}
