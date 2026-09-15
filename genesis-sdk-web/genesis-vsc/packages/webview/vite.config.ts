// genesis-vsc/packages/webview/vite.config.ts
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';
import tailwindcss from '@tailwindcss/vite';
import path from 'path';
import { createRequire } from 'module';

const require = createRequire(import.meta.url);

// Résolution dynamique — trouve où npm a RÉELLEMENT installé ces packages
// (fonctionne que vue soit hoisté ou local)


export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    vueDevTools(),
  ],
  resolve: {
    dedupe: ['vue', 'vue-router', 'pinia'],
    alias: {
      // Alias projet
      '@':                  path.resolve(__dirname, './src'),
      '@genesis-labs/shared-types': path.resolve(__dirname, '../../../genesis-web-types-shared/src/index.ts'),
      '@genesis-labs/core': path.resolve(__dirname, '../../../genesis-web-core/src'),
    }
  },
  build: {
    outDir: 'dist',
    rollupOptions: {
      input: 'index.html',
      output: {
        entryFileNames: 'assets/[name].js',
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  },
  base: './'
});