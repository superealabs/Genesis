import { VscodeService } from '@/core/services/vscode.service';
import { useFrontendStore } from '../store/useFrontend.store';
import type { FrontendFramework } from '../types/frontend.types';

export class FrontendService extends VscodeService {
    // ✅ Pattern du getter pour éviter les erreurs d'initialisation Pinia
    private get store() {
        return useFrontendStore();
    }

    /**
     * Initialisation des écouteurs (OUTPUT)
     */
    init(): void {
        // Écoute la réponse de l'Extension Host contenant la liste des frameworks
        this.onMessage<FrontendFramework[]>('FRONTEND_FRAMEWORKS_LOADED', (frameworks) => {
            this.store.setAvailableFrameworks(frameworks);
        });
    }

    /**
     * Demande la liste des frameworks frontend à l'extension (INPUT)
     */
    fetchFrontendFrameworks(): void {
        this.sendMessage('GET_FRONTEND_FRAMEWORKS');
    }

    /**
     * Enregistre le choix de l'utilisateur dans le store
     */
    selectFrontendFramework(framework: FrontendFramework): void {
        this.store.selectFramework(framework);
    }
}

// ✅ Export en tant que singleton
export const frontendService = new FrontendService();