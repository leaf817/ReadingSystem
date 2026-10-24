import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/** 每次 `npm run dev` / 重启 Vite 都会变化；浏览器普通刷新不变 */
const FRONTEND_BOOT_ID = String(Date.now())

// https://vite.dev/config/
export default defineConfig({
  define: {
    __FRONTEND_BOOT_ID__: JSON.stringify(FRONTEND_BOOT_ID),
  },
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    host: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true,
      },
      '/uploads': {
        target: 'http://localhost:8088',
        changeOrigin: true,
      },
    },
  },
})
