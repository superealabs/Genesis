Parfait, on y va dans l'ordre.

---

## Étape 1 — Créer le projet extension VS Code

Tu reparts de zéro avec `yo code`. Dans ton terminal :

```bash
npm install -g yo generator-code
yo code
```

Réponds aux questions :
```
? What type of extension do you want to create? New Extension (TypeScript)
? What's the name of your extension? genesis
? What's the identifier? genesis
? What's the description? Genesis Extension
? Initialize a git repository? Yes
? Which bundler to use? webpack
? Which package manager to use? npm
```

⚠️ **Choisis webpack** cette fois — contrairement au Hello World, on a besoin d'un bundler côté extension pour gérer les imports proprement avec Vue.

---

## Étape 2 — Créer le projet Vue avec Vite

Dans le dossier de ton extension :

```bash
npm create vite@latest webview-ui -- --template vue-ts
cd webview-ui
npm install
```

**Pourquoi Vite ?**
Vite est un bundler moderne qui compile ton app Vue en fichiers statiques (`index.html`, `main.js`, `style.css`). C'est ces fichiers compilés que la Webview VS Code va charger — elle ne peut pas exécuter du `.vue` directement.

---

## Étape 3 — Structure finale du projet

```
genesis/
├── src/                        ← Extension TypeScript
│   └── extension.ts
├── webview-ui/                 ← App Vue + Vite
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.ts
│   │   └── components/
│   ├── index.html
│   ├── vite.config.ts
│   └── package.json
├── package.json                ← Extension
└── webpack.config.js
```

---

## Étape 4 — Configurer Vite pour VS Code

**Pourquoi cette config ?**
Par défaut Vite génère des chemins absolus (`/assets/main.js`). VS Code ne comprend pas ces chemins — il faut des chemins relatifs (`./assets/main.js`) pour que la Webview les charge correctement.

Dans `webview-ui/vite.config.ts` :

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: '../dist/webview',   // output dans le dossier de l'extension
    rollupOptions: {
      output: {
        entryFileNames: 'assets/[name].js',      // pas de hash dans les noms
        chunkFileNames: 'assets/[name].js',
        assetFileNames: 'assets/[name].[ext]'
      }
    }
  },
  base: './'    // ← CRITIQUE : chemins relatifs pour VS Code
})
```

---

## Étape 5 — Workflow de travail théorique

Voici comment les deux projets s'articulent :

```
┌─────────────────────────────────────────────────┐
│                  VS Code                         │
│                                                  │
│  ┌─────────────────┐      ┌──────────────────┐  │
│  │  Extension TS   │      │    Webview        │  │
│  │  (Node.js)      │◄────►│    (iframe)       │  │
│  │                 │      │                   │  │
│  │  - Commandes    │      │  App Vue compilée │  │
│  │  - FileSystem   │      │  HTML/CSS/JS      │  │
│  │  - Settings     │      │                   │  │
│  └─────────────────┘      └──────────────────┘  │
│         postMessage ◄──────────► postMessage     │
└─────────────────────────────────────────────────┘
```

**Les deux processus sont complètement isolés.** L'extension tourne dans Node.js et a accès au filesystem, aux APIs VS Code, au réseau. La Webview est un iframe sandboxé qui ne peut faire que ce qu'un navigateur normal ferait — sauf qu'elle n'a pas accès aux APIs VS Code directement.

**L'unique pont entre les deux : `postMessage`.**

---

## Étape 6 — La communication en détail

C'est la partie la plus importante. Il y a deux sens de communication.

### Sens 1 — Extension → Webview

```typescript
// Dans extension.ts
panel.webview.postMessage({
    type: 'setData',
    payload: { username: 'Ny Antema', env: 'Dev' }
});
```

```typescript
// Dans App.vue
window.addEventListener('message', (event) => {
    const message = event.data;
    if (message.type === 'setData') {
        username.value = message.payload.username;
    }
});
```

### Sens 2 — Webview → Extension

```typescript
// Dans App.vue
const vscode = acquireVsCodeApi();   // ← API injectée par VS Code dans la Webview

vscode.postMessage({
    type: 'formSubmitted',
    payload: { envName: 'Production' }
});
```

```typescript
// Dans extension.ts
panel.webview.onDidReceiveMessage((message) => {
    if (message.type === 'formSubmitted') {
        vscode.window.showInformationMessage(`Env reçu : ${message.payload.envName}`);
    }
});
```

**Pourquoi `acquireVsCodeApi()` ?**
VS Code injecte automatiquement cette fonction dans le contexte de la Webview. Elle te donne accès à `postMessage` pour envoyer des messages à l'extension. Tu ne peux l'appeler **qu'une seule fois** — stocke-la dans une variable.

---

## Étape 7 — Script npm unifié

Pour ne pas avoir à builder manuellement à chaque fois, ajoute dans le `package.json` de l'extension :

```json
"scripts": {
    "build:webview": "cd webview-ui && npm run build",
    "watch:webview": "cd webview-ui && npm run build -- --watch",
    "compile": "npm run build:webview && webpack --mode development",
    "dev": "concurrently \"npm run watch:webview\" \"webpack --watch --mode development\""
}
```

---
