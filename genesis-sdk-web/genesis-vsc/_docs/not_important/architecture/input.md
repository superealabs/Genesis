## 📥 Exemple complet : INPUT (Sélection d'un Framework)

Scénario : L'utilisateur clique sur un framework pour le sélectionner. L'information doit remonter jusqu'au JAR pour être persistée.

---

### 1. **View** (déclenche l'action via le composable)

```vue
<!-- webview-ui/src/features/frameworks/views/FrameworksView.vue -->
<template>
    <div class="p-4">
        <h1 class="text-2xl font-bold mb-4">Frameworks</h1>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            <FrameworkCard
                v-for="framework in frameworks"
                :key="framework.id"
                :framework="framework"
                :selected="selectedFramework?.id === framework.id"
                @select="handleSelect(framework.id)"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { useFrameworks } from '../composables/useFrameworks';
import FrameworkCard from '../components/FrameworkCard.vue';

// La View n'utilise QUE le composable
const { frameworks, selectedFramework, selectFramework } = useFrameworks();

function handleSelect(id: number): void {
    // Délègue au composable, ne touche JAMAIS au service directement
    selectFramework(id);
}
</script>
```

---

### 2. **Composable** (interface entre View et Service)

```typescript
// webview-ui/src/features/frameworks/composables/useFrameworks.ts
import { storeToRefs } from 'pinia';
import { useFrameworkStore } from '../stores/framework.store';
import { frameworkService } from '../services/FrameworkService';

export function useFrameworks() {
    const store = useFrameworkStore();
    const { getFrameworks, getSelectedFramework } = storeToRefs(store);

    /**
     * Sélectionne un framework :
     * 1. Met à jour le Store (UI réactive immédiate - optimistic UI)
     * 2. Appelle le Service pour persister côté JAR
     */
    function selectFramework(id: number): void {
        // Mise à jour locale immédiate (UX fluide)
        store.selectFramework(id);
        
        // Envoi au backend via le Service
        frameworkService.selectFramework(id);
    }

    return {
        frameworks: getFrameworks,
        selectedFramework: getSelectedFramework,
        selectFramework
    };
}
```

---

### 3. **Store** (passif, stocke l'état)

```typescript
// webview-ui/src/features/frameworks/stores/framework.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '../types/framework.types';

export const useFrameworkStore = defineStore('framework', () => {
    const frameworks = ref<Framework[]>([]);
    const selectedFrameworkId = ref<number | null>(null);

    const getFrameworks = computed(() => frameworks.value);
    const getSelectedFramework = computed(() => 
        frameworks.value.find(f => f.id === selectedFrameworkId.value) || null
    );

    function setFrameworks(data: Framework[]): void {
        frameworks.value = data;
    }

    function selectFramework(id: number): void {
        selectedFrameworkId.value = id;
    }

    return {
        frameworks,
        selectedFrameworkId,
        getFrameworks,
        getSelectedFramework,
        setFrameworks,
        selectFramework
    };
});
```

---

### 4. **Service Webview** (hérite de VscodeService, gère INPUT + OUTPUT)

```typescript
// webview-ui/src/features/frameworks/services/FrameworkService.ts
import { VscodeService } from '@/core/services/VscodeService';
import { useFrameworkStore } from '../stores/framework.store';
import type { Framework, FrameworkRaw } from '../types/framework.types';

export class FrameworkService extends VscodeService {
    private store = useFrameworkStore();

    // ═══════════════════════════════════════
    // INPUT : Envoi vers le JAR
    // ═══════════════════════════════════════

    /**
     * Demande la sélection d'un framework au backend
     */
    selectFramework(id: number): void {
        // Validation avant envoi
        if (!id || id <= 0) {
            console.warn('Invalid framework ID');
            return;
        }

        this.postMessage('SELECT_FRAMEWORK', { id });
    }

    /**
     * Demande la liste des frameworks
     */
    fetchFrameworks(): void {
        this.postMessage('GET_FRAMEWORKS');
    }

    // ═══════════════════════════════════════
    // OUTPUT : Réception depuis le JAR
    // ═══════════════════════════════════════

    init(): void {
        // Réponse à la sélection
        this.onMessage<{ success: boolean; framework: FrameworkRaw }>(
            'FRAMEWORK_SELECTED',
            (response) => {
                if (response.success) {
                    const framework = this.mapFramework(response.framework);
                    this.store.selectFramework(framework.id);
                }
            }
        );

        // Réponse à la récupération de la liste
        this.onMessage<FrameworkRaw[]>('FRAMEWORKS_LOADED', (rawFrameworks) => {
            const frameworks = rawFrameworks.map(raw => this.mapFramework(raw));
            this.store.setFrameworks(frameworks);
        });

        // Gestion des erreurs
        this.onMessage<{ message: string }>('ERROR', (error) => {
            console.error('[FrameworkService]', error.message);
            // Optionnel : rollback du store en cas d'échec
        });
    }

    // ═══════════════════════════════════════
    // MAPPING : Transformation des données
    // ═══════════════════════════════════════

    private mapFramework(raw: FrameworkRaw): Framework {
        return {
            id: raw.id,
            name: raw.name.trim(),
            language: this.normalizeLanguage(raw.language)
        };
    }

    private normalizeLanguage(language: string): string {
        const map: Record<string, string> = {
            'java': 'Java',
            'javascript': 'JavaScript',
            'typescript': 'TypeScript'
        };
        return map[language.toLowerCase()] || language;
    }
}

export const frameworkService = new FrameworkService();
```

---

### 5. **Types partagés** (contrat entre Webview et Extension)

```typescript
// shared/types/messages.types.ts

// ═══ Messages du Webview vers l'Extension ═══
export type WebviewMessage =
    | { command: 'GET_FRAMEWORKS' }
    | { command: 'SELECT_FRAMEWORK'; payload: { id: number } };

// ═══ Messages de l'Extension vers le Webview ═══
export type ExtensionMessage =
    | { command: 'FRAMEWORKS_LOADED'; payload: FrameworkRaw[] }
    | { command: 'FRAMEWORK_SELECTED'; payload: { success: boolean; framework: FrameworkRaw } }
    | { command: 'ERROR'; payload: { message: string } };

export interface FrameworkRaw {
    id: number;
    name: string;
    language: string;
}
```

---

### 6. **Extension Host** (routeur de messages)

```typescript
// src/extension/extension.ts
import * as vscode from 'vscode';
import { FrameworkExtensionService } from './services/FrameworkExtensionService';

export function activate(context: vscode.ExtensionContext) {
    const panel = vscode.window.createWebviewPanel(/* ... */);
    
    const frameworkExtService = new FrameworkExtensionService(panel);

    // Routeur de messages : PAS de logique métier ici
    panel.webview.onDidReceiveMessage(async (message) => {
        switch (message.command) {
            case 'GET_FRAMEWORKS':
                await frameworkExtService.handleGetFrameworks();
                break;
                
            case 'SELECT_FRAMEWORK':
                await frameworkExtService.handleSelectFramework(message.payload.id);
                break;
                
            default:
                console.warn(`Unknown command: ${message.command}`);
        }
    });
}
```

---

### 7. **Extension Service** (logique métier + appel API)

```typescript
// src/extension/services/FrameworkExtensionService.ts
import axios from 'axios';
import type { WebviewPanel } from 'vscode';

export class FrameworkExtensionService {
    private readonly apiUrl = 'http://localhost:8080/api/frameworks';

    constructor(private webviewPanel: WebviewPanel) {}

    /**
     * Gère la sélection d'un framework
     */
    async handleSelectFramework(id: number): Promise<void> {
        try {
            // 1. Validation côté extension
            if (!id || id <= 0) {
                throw new Error('Invalid framework ID');
            }

            // 2. Appel à l'API du JAR
            const response = await axios.post(`${this.apiUrl}/select`, { id });

            // 3. Réponse brute du JAR
            // { success: true, framework: { id: 1, name: "Spring Boot", language: "java" } }

            // 4. Renvoi au webview
            this.webviewPanel.webview.postMessage({
                command: 'FRAMEWORK_SELECTED',
                payload: response.data
            });
        } catch (error) {
            this.webviewPanel.webview.postMessage({
                command: 'ERROR',
                payload: { message: 'Failed to select framework' }
            });
        }
    }

    async handleGetFrameworks(): Promise<void> {
        // ... (exemple précédent)
    }
}
```

---

## 🔄 Flux INPUT complet

```
1.  [View]         → handleSelect(1)
2.  [Composable]   → store.selectFramework(1) + frameworkService.selectFramework(1)
3.  [Store]        → selectedFrameworkId = 1 (UI réactive immédiatement)
4.  [Service]      → postMessage('SELECT_FRAMEWORK', { id: 1 })
5.  ═══════════════ 🚧 FRONTIÈRE SANDBOX 🚧 ═══════════════
6.  [Ext Host]     → onDidReceiveMessage → dispatch vers FrameworkExtensionService
7.  [Ext Service]  → Validation + axios.post('/api/frameworks/select', { id: 1 })
8.  [JAR]          → Traite, persiste, renvoie { success: true, framework: {...} }
9.  [Ext Service]  → postMessage('FRAMEWORK_SELECTED', payload)
10. ═══════════════ 🚧 FRONTIÈRE SANDBOX 🚧 ═══════════════
11. [Service]      → onMessage reçoit, mapFramework(), store.selectFramework(id)
12. [Store]        → État confirmé (ou rollback si erreur)
13. [View]         → Se met à jour automatiquement via la réactivité
```

---

## 🎯 Points clés de ce flux INPUT

| Principe | Respecté ? | Comment |
|---|---|---|
| **View ne touche pas au Service** | ✅ | Passe toujours par le composable |
| **Optimistic UI** | ✅ | Le store est mis à jour immédiatement avant la réponse du JAR |
| **Validation multi-couches** | ✅ | Service webview + Extension Service |
| **Typage partagé** | ✅ | `messages.types.ts` utilisé des deux côtés |
| **Store passif** | ✅ | Ne fait aucun appel externe |
| **Extension Host = routeur** | ✅ | Aucun `if` métier, juste du dispatch |
| **Service = classe héritée** | ✅ | `FrameworkService extends VscodeService` |

Cette architecture garantit un code **maintenable**, **testable** et **évolutif**. 🚀