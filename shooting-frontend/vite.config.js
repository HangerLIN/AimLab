import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  define: {
    // 修复 sockjs-client 的 global 未定义问题
    global: 'globalThis',
  },
  server: {
    port: 5173,
    strictPort: true, // 如果端口被占用则报错，而不是自动切换
    proxy: {
      '/api': {
        target: 'http://localhost:8083',
        changeOrigin: true
      },
      '/ws': {
        target: 'http://localhost:8083',
        ws: true
      }
    }
  }
})
