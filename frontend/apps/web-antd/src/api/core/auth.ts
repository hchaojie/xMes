import CryptoJS from 'crypto-js';

import { baseRequestClient, requestClient } from '#/api/request';

export namespace AuthApi {
  /** 登录接口参数 */
  export interface LoginParams {
    password?: string;
    username?: string;
  }

  /** 登录接口返回值 */
  export interface LoginResult {
    accessToken: string;
  }

  export interface RefreshTokenResult {
    data: string;
    status: number;
  }
}

/**
 * pig 登录密码加密：AES-128-CFB / NoPadding，IV 与密钥相同。
 * 密钥须与后端 security.encodeKey 一致（16 字节）。
 */
const ENCODE_KEY = 'thanks,pig4cloud';

function encryptPassword(plain: string): string {
  const key = CryptoJS.enc.Utf8.parse(ENCODE_KEY);
  return CryptoJS.AES.encrypt(plain, key, {
    iv: key,
    mode: CryptoJS.mode.CFB,
    padding: CryptoJS.pad.NoPadding,
  }).toString();
}

/**
 * pig OAuth2 客户端。开发期使用 test 客户端（免图形验证码）；
 * 上线切换 pig 客户端并接入验证码组件。
 */
const OAUTH_CLIENT = { id: 'test', secret: 'test' };

/**
 * 登录：pig OAuth2 密码模式
 */
export async function loginApi(data: AuthApi.LoginParams) {
  const body = new URLSearchParams();
  body.append('username', data.username ?? '');
  body.append('password', encryptPassword(data.password ?? ''));
  body.append('grant_type', 'password');
  body.append('scope', 'server');

  // token 端点返回 OAuth2 原始报文（非 R 包装），使用 baseRequestClient
  const resp = await baseRequestClient.post('/admin/oauth2/token', body, {
    headers: {
      'Authorization': `Basic ${btoa(`${OAUTH_CLIENT.id}:${OAUTH_CLIENT.secret}`)}`,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
  });
  return { accessToken: resp.data.access_token } as AuthApi.LoginResult;
}

/**
 * 刷新accessToken（pig 开发客户端未启用 refresh，保留接口形态）
 */
export async function refreshTokenApi() {
  return baseRequestClient.post<AuthApi.RefreshTokenResult>('/auth/refresh', {
    withCredentials: true,
  });
}

/**
 * 退出登录
 */
export async function logoutApi() {
  return baseRequestClient.delete('/admin/token/logout');
}

/**
 * 获取用户权限码：pig 将按钮权限放在 /admin/user/info 的 permissions 中
 */
export async function getAccessCodesApi() {
  const info = await requestClient.get<{ permissions?: string[] }>(
    '/admin/user/info',
  );
  return info?.permissions ?? [];
}
