// vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
    plugins: [react()],
    resolve: { alias: { '@': '/src' } },
    server: {
        port: 3000,
        hmr: {
            overlay: true, // affiche une pop-up + stack dans le terminal
        },
    }
});