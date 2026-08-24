import { VscodeService } from '@/core/services/VscodeService';
import { useFrameworkStore } from '@/features/frameworks/store/useFramework.store';
import type { Framework } from '@/features/frameworks/types/framework.types';

// ═══ DONNÉES STATIQUES (MOCK) ═══
const MOCK_FRAMEWORKS: Framework[] = [
    { id: 1, languageId: 1, name: 'Spring Boot REST', coreFramework: 'Spring', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 2, languageId: 1, name: 'Spring MVC', coreFramework: 'Spring', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 3, languageId: 2, name: 'Django REST', coreFramework: 'Django', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 4, languageId: 3, name: 'Laravel MVC', coreFramework: 'Laravel', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 5, languageId: 4, name: 'Express REST', coreFramework: 'Express', type: 'REST API', isProd: false, useDB: false, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
];

export class FrameworkService extends VscodeService {
    private _store: ReturnType<typeof useFrameworkStore> | null = null;


    private get store() {
        if (!this._store) {
            this._store = useFrameworkStore();
        }
        return this._store;
    }

    /**
     * Initialisation des écouteurs (OUTPUT)
     */
    init(): void {
        this.onMessage<Framework[]>('FRAMEWORKS_LOADED', (data) => {
            this.store.setFrameworks(data);
        });

        this.onMessage<{ success: boolean; framework: Framework }>('FRAMEWORK_SELECTED', (data) => {
            if (data.success) {
                // Logique de confirmation de sélection si nécessaire
                console.log('Framework sélectionné avec succès:', data.framework.name);
            }
        });
    }

    /**
     * Demande la liste des frameworks (INPUT)
     * Pour le moment, on simule la réponse du backend directement.
     */
    fetchFrameworks(): void {
        setTimeout(() => {
            // ✅ Plus de cast sauvage, simulateMessage est typé et encapsulé
            this.simulateMessage('FRAMEWORKS_LOADED', MOCK_FRAMEWORKS);
        }, 300);
    }

    selectFramework(id: number): void {
        if (!id) return;
        setTimeout(() => {
            const selected = MOCK_FRAMEWORKS.find(f => f.id === id);
            if (selected) {
                this.simulateMessage('FRAMEWORK_SELECTED', { success: true, framework: selected });
            }
        }, 100);
    }
}

export const frameworkService = new FrameworkService();