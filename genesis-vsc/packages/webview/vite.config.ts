import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@genesis-labs/core': path.resolve(__dirname, '../../../genesis-web-core/src'),
      '@': path.resolve(__dirname, '../../../genesis-web-core/src')
    }
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      input: 'index.html'
    }
  }
});