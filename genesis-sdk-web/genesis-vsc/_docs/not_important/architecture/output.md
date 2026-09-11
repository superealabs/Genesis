---

## 🏗️ VSCODE_SERVICE : Classe abstraite

```typescript
// webview-ui/src/core/services/VscodeService.ts
import type { ExtensionMessage, WebviewMessage } from '@/shared/types/messages.types';

export abstract class VscodeService {
    protected vscode: any;
    private messageHandlers: Map<string, Set<(payload: any) => void>> = new Map();

    constructor() {
        this.vscode = acquireVsCodeApi();
        this.setupMessageListener();
    }

    /**
     * Envoie un message à l'extension
     */
    protected postMessage<T>(command: string, payload?: T): void {
        const message: WebviewMessage = { command, payload };
        this.vscode.postMessage(message);
    }

    /**
     * Écoute les messages de l'extension
     */
    protected onMessage<T>(command: string, callback: (payload: T) => void): () => void {
        if (!this.messageHandlers.has(command)) {
            this.messageHandlers.set(command, new Set());
        }
        
        this.messageHandlers.get(command)!.add(callback);

        // Retourne une fonction de cleanup
        return () => {
            this.messageHandlers.get(command)?.delete(callback);
        };
    }

    /**
     * Setup du listener global
     */
    private setupMessageListener(): void {
        window.addEventListener('message', (event: MessageEvent<ExtensionMessage>) => {
            const { command, payload } = event.data;
            const handlers = this.messageHandlers.get(command);
            
            if (handlers) {
                handlers.forEach(handler => handler(payload));
            }
        });
    }

    /**
     * Cleanup tous les listeners
     */
    protected cleanup(): void {
        this.messageHandlers.clear();
    }
}
```

---

## 📋 Exemple complet : OUTPUT d'une liste de Frameworks

### 1. **Extension Service** (côté extension)

```typescript
// src/extension/services/FrameworkExtensionService.ts
import axios from 'axios';
import type { WebviewPanel } from 'vscode';

export class FrameworkExtensionService {
    constructor(private webviewPanel: WebviewPanel) {}

    async handleGetFrameworks(): Promise<void> {
        try {
            // Appel à l'API du JAR
            const response = await axios.get('http://localhost:8080/api/frameworks');
            
            // Réponse brute du JAR (exemple)
            // [
            //   { name: "Spring Boot", language: "Java", type: "MVC", ... },
            //   { name: "Express", language: "JavaScript", type: "REST API", ... }
            // ]

            // Validation basique
            if (!Array.isArray(response.data)) {
                throw new Error('Invalid response format');
            }

            // Renvoi au webview
            this.webviewPanel.webview.postMessage({
                command: 'FRAMEWORKS_LOADED',
                payload: response.data
            });
        } catch (error) {
            this.webviewPanel.webview.postMessage({
                command: 'ERROR',
                payload: { message: 'Failed to load frameworks' }
            });
        }
    }
}
```

### 2. **Service Webview** (hérite de VscodeService)

```typescript
// webview-ui/src/features/frameworks/services/FrameworkService.ts
import { VscodeService } from '@/core/services/VscodeService';
import { useFrameworkStore } from '../stores/framework.store';
import type { Framework, FrameworkRaw } from '../types/framework.types';

export class FrameworkService extends VscodeService {
    private store = useFrameworkStore();

    /**
     * INPUT : Demande la liste des frameworks
     */
    async fetchFrameworks(): Promise<void> {
        this.postMessage('GET_FRAMEWORKS');
    }

    /**
     * OUTPUT : Reçoit la liste, transforme, met à jour le Store
     */
    init(): void {
        this.onMessage<FrameworkRaw[]>('FRAMEWORKS_LOADED', (rawFrameworks) => {
            // Transformation et validation
            const frameworks = this.mapFrameworks(rawFrameworks);
            
            // Mise à jour du Store
            this.store.setFrameworks(frameworks);
        });

        this.onMessage<{ message: string }>('ERROR', (error) => {
            console.error('Framework error:', error.message);
            // Optionnel : mettre à jour un état d'erreur dans le Store
        });
    }

    /**
     * Mapping et validation des données brutes
     */
    private mapFrameworks(rawFrameworks: FrameworkRaw[]): Framework[] {
        return rawFrameworks
            .filter(raw => raw.name && raw.language) // Validation
            .map(raw => ({
                id: this.generateId(raw.name), // Génération d'un ID si absent
                name: raw.name.trim(),
                language: this.normalizeLanguage(raw.language),
                // Autres transformations...
            }));
    }

    private normalizeLanguage(language: string): string {
        const languageMap: Record<string, string> = {
            'java': 'Java',
            'javascript': 'JavaScript',
            'typescript': 'TypeScript',
            // ...
        };
        return languageMap[language.toLowerCase()] || language;
    }

    private generateId(name: string): number {
        return name.split('').reduce((acc, char) => acc + char.charCodeAt(0), 0);
    }
}

// Singleton
export const frameworkService = new FrameworkService();
```

### 3. **Store** (passif)

```typescript
// webview-ui/src/features/frameworks/stores/framework.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework } from '../types/framework.types';

export const useFrameworkStore = defineStore('framework', () => {
    const frameworks = ref<Framework[]>([]);
    const selectedFrameworkId = ref<number | null>(null);

    // Getters
    const getFrameworks = computed(() => frameworks.value);
    const getSelectedFramework = computed(() => 
        frameworks.value.find(f => f.id === selectedFrameworkId.value) || null
    );

    // Actions (mutations)
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

### 4. **Composable** (interface entre View et Service/Store)

```typescript
// webview-ui/src/features/frameworks/composables/useFrameworks.ts
import { onMounted } from 'vue';
import { storeToRefs } from 'pinia';
import { useFrameworkStore } from '../stores/framework.store';
import { frameworkService } from '../services/FrameworkService';

export function useFrameworks() {
    const store = useFrameworkStore();
    const { getFrameworks, getSelectedFramework } = storeToRefs(store);

    /**
     * Initialise le service et charge les données
     */
    function init(): void {
        frameworkService.init();
        frameworkService.fetchFrameworks();
    }

    /**
     * Sélectionne un framework
     */
    function selectFramework(id: number): void {
        store.selectFramework(id);
    }

    return {
        frameworks: getFrameworks,
        selectedFramework: getSelectedFramework,
        init,
        selectFramework
    };
}
```

### 5. **View** (affiche uniquement)

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
import { onMounted } from 'vue';
import { useFrameworks } from '../composables/useFrameworks';
import FrameworkCard from '../components/FrameworkCard.vue';

const { frameworks, selectedFramework, init, selectFramework } = useFrameworks();

onMounted(() => {
    init();
});

function handleSelect(id: number): void {
    selectFramework(id);
}
</script>
```

---

## 🔄 Flux complet OUTPUT

```
1. View appelle init() du composable
   ↓
2. Composable appelle frameworkService.init() + fetchFrameworks()
   ↓
3. Service envoie postMessage('GET_FRAMEWORKS')
   ↓
4. Extension Host reçoit et dispatch vers FrameworkExtensionService
   ↓
5. Extension Service appelle l'API JAR
   ↓
6. JAR renvoie la liste brute
   ↓
7. Extension Service renvoie via postMessage('FRAMEWORKS_LOADED', data)
   ↓
8. FrameworkService (OUTPUT) reçoit, transforme, met à jour le Store
   ↓
9. Store met à jour l'état réactif
   ↓
10. View se met à jour automatiquement via le composable
```

Cette architecture est maintenant **complètement cohérente**, **testable**, et respecte les **standards professionnels**.