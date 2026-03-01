import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  define: {
    // 关键修复：SockJS 内部需要 global 变量
    global: 'window',
  },
  server: {
    port: 8080,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:9090', // 修正为正确的后端端口
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''), // 如果后端接口不带 /api 前缀
        ws: true
      }
    }
  }
})
