// genesis-vsc/packages/webview/src/services/framework.service.ts

import type { IFrameworkService } from '@genesis-labs/core/features/frameworks/types/framework.service.interface';
import { useFrameworkStore } from '@genesis-labs/core/features/frameworks/store/useFramework.store';
import type { Framework } from '@genesis-labs/core/features/frameworks/types/framework.types';

// ✅ Import du service spécifique à VS Code (local à ce dossier)
import { VscodeService } from '../../../core/services/vscode.service'; 

export class FrameworkService implements IFrameworkService {
    private _store: ReturnType<typeof useFrameworkStore> | null = null;

    // ✅ Injection de la dépendance via le constructeur (Composition au lieu d'héritage)
    constructor(private vscode: VscodeService) {}

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
        this.vscode.onMessage<Framework[]>('FRAMEWORKS_LOADED', (data) => {
            this.store.setFrameworks(data);
        });

        // 2. Réception de la confirmation de sélection
        this.vscode.onMessage<{ success: boolean; framework: Framework }>('FRAMEWORK_SELECTED', (data) => {
            if (data.success) {
                console.log('Framework sélectionné avec succès:', data.framework.name);
            }
        });

        // 3. Gestion des erreurs provenant du Handler
        this.vscode.onMessage<{ command: string; message: string }>('API_ERROR', (data) => {
            console.error(`[FrameworkService] Erreur pour la commande ${data.command}:`, data.message);
        });
    }

    /**
     * Demande la liste des frameworks (INPUT : Webview → Extension Host)
     */
    fetchFrameworks(): void {
        this.vscode.sendMessage('GET_FRAMEWORKS');
    }

    /**
     * Demande la sélection d'un framework
     */
    selectFramework(id: number): void {
        if (!id) return;
        this.vscode.sendMessage('SELECT_FRAMEWORK', { id });
    }
}

// ✅ Création de l'instance en lui fournissant l'outil de communication VS Code
export const frameworkService = new FrameworkService(new VscodeService());