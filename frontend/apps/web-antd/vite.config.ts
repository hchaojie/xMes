import { defineConfig } from '@vben/vite-config';

export default defineConfig(async () => {
  return {
    application: {},
    vite: {
      server: {
        proxy: {
          // MES 业务接口：boot 单体模式下 xmes 端点位于 /admin 上下文；
          // 微服务模式时改 target 为网关地址并将 rewrite 调整为去掉 /api 前缀（网关按 /mes/** 路由）
          '/api/mes': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api\/mes/, '/admin'),
            target: 'http://127.0.0.1:9999',
          },
          // pig 系统接口（认证/用户/文件）
          '/api/admin': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            target: 'http://127.0.0.1:9999',
          },
          // 其余请求保留 vben 自带 mock（示例页面数据）
          '/api': {
            changeOrigin: true,
            rewrite: (path) => path.replace(/^\/api/, ''),
            // mock代理目标地址
            target: 'http://localhost:5320/api',
            ws: true,
          },
        },
      },
    },
  };
});
