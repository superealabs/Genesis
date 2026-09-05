/// <reference types="vite/client" />

// 1. Dit à TS que les fichiers .vue sont des composants valides
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 2. Déclare la fonction globale de VS Code
declare function acquireVsCodeApi(): {
    postMessage: (message: any) => void;
    getState: () => any;
    setState: (state: any) => void;
};