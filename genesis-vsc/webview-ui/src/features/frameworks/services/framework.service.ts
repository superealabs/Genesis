import { VscodeService } from '@/core/services/VscodeService';
import { useFrameworkStore } from '@/features/frameworks/store/useFramework.store';
import type { Framework } from '@/features/frameworks/types/framework.types';

export class FrameworkService extends VscodeService {
    private _store: ReturnType<typeof useFrameworkStore> | null = null;

    private get store() {
        if (!this._store) {
            this._store = useFrameworkStore();
        }
        return this._store;
        }

    /**
     * Initialisation des écouteurs (OUTPUT : Extension Host → Webview)
     */
    init(): void {
        // 1. Réception de la liste des frameworks
        this.onMessage<Framework[]>('FRAMEWORKS_LOADED', (data) => {
            this.store.setFrameworks(data);
        });

        // 2. Réception de la confirmation de sélection
        this.onMessage<{ success: boolean; framework: Framework }>('FRAMEWORK_SELECTED', (data) => {
            if (data.success) {
                console.log('Framework sélectionné avec succès:', data.framework.name);
                // Optionnel : this.store.setSelectedFramework(data.framework);
            }
        });

        // 3. Gestion des erreurs provenant du Handler
        this.onMessage<{ command: string; message: string }>('API_ERROR', (data) => {
            console.error(`[FrameworkService] Erreur pour la commande ${data.command}:`, data.message);
        });
    }

    /**
     * Demande la liste des frameworks (INPUT : Webview → Extension Host)
     */
    fetchFrameworks(): void {
        this.sendMessage('GET_FRAMEWORKS');
    }

    /**
     * Demande la sélection d'un framework
     */
    selectFramework(id: number): void {
        if (!id) return;
        this.sendMessage('SELECT_FRAMEWORK', { id });
    }
}

export const frameworkService = new FrameworkService();