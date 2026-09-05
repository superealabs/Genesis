import type { IFrontendService } from '@genesis-labs/core/features/frontend/types/frontend.service.interface';
import { useFrontendStore } from '@genesis-labs/core/features/frontend/store/useFrontend.store';
import type { FrontendFramework } from '@genesis-labs/core/features/frontend/types/frontend.types';

// ✅ Import du service de communication local à VS Code
import { VscodeService } from '../../../core/services/vscode.service';

export class FrontendService implements IFrontendService {
    // ✅ Injection de la dépendance
    constructor(private vscode: VscodeService) {}

    private get store() {
        return useFrontendStore();
    }

    /**
     * Initialisation des écouteurs (OUTPUT)
     */
    init(): void {
        this.vscode.onMessage<FrontendFramework[]>('FRONTEND_FRAMEWORKS_LOADED', (frameworks) => {
            this.store.setAvailableFrameworks(frameworks);
        });
    }

    /**
     * Demande la liste des frameworks frontend à l'extension (INPUT)
     */
    fetchFrontendFrameworks(): void {
        this.vscode.sendMessage('GET_FRONTEND_FRAMEWORKS');
    }

    /**
     * Enregistre le choix de l'utilisateur
     */
    selectFrontendFramework(framework: FrontendFramework): void {
        this.store.selectFramework(framework);
    }
}

// ✅ Export du singleton initialisé avec l'outil VS Code
export const frontendService = new FrontendService(new VscodeService());