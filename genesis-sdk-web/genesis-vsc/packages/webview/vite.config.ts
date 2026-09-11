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
const vuePath      = path.dirname(require.resolve('vue/package.json'));
const vueRouterPath = path.dirname(require.resolve('vue-router/package.json'));
const piniaPath    = path.dirname(require.resolve('pinia/package.json'));

export default defineConfig({
  plugins: [
    vue(),
    tailwindcss(),
    vueDevTools(),
  ],
  resolve: {
    dedupe: ['vue', 'vue-router', 'pinia'],
    alias: {
      // ✅ Chemins réels résolus dynamiquement, pas codés en dur
      'vue':        vuePath,
      'vue-router': vueRouterPath,
      'pinia':      piniaPath,

      // Alias projet
      '@':                  path.resolve(__dirname, '../../../genesis-web-core/src'),
      '@genesis-labs/core': path.resolve(__dirname, '../../../genesis-web-core/src'),
      '@vsc':               path.resolve(__dirname, './src'),
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