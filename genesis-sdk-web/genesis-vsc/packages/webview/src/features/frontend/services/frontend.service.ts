// ✅ 1. Import via le manifeste (meilleure pratique)
import type { IFrontendService, FrontendFramework } from '@genesis-labs/core/features/frontend/manifest';

// ✅ 2. Import de l'INSTANCE singleton (et non de la classe)
import { vscodeService } from '../../../core/services/vscode.service';

export class FrontendServiceVsc implements IFrontendService {
    
    // ✅ 3. Utilise l'instance singleton par défaut
    constructor(private vscode = vscodeService) {}

    /**
     * Demande la liste des frameworks frontend à l'extension
     * ✅ Retourne une Promise et ne touche PAS au store
     */
    fetchFrontendFrameworks(): Promise<FrontendFramework[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRONTEND_FRAMEWORKS');
            
            // ✅ Écoute la réponse UNE SEULE FOIS, puis cleanup
            const cleanup = this.vscode.onMessage<FrontendFramework[]>('FRONTEND_FRAMEWORKS_LOADED', (data) => {
                cleanup(); // Nettoie le listener pour éviter les fuites mémoire
                resolve(data); // Retourne la donnée brute au composable
            });
        });
    }

    /**
     * Enregistre le choix de l'utilisateur
     * ✅ Retourne une Promise
     */
    selectFrontendFramework(framework: FrontendFramework): Promise<void> {
        return new Promise((resolve) => {
            // Envoie l'ID (ou l'objet complet) selon ce que ton Extension Host attend
            this.vscode.sendMessage('SELECT_FRONTEND_FRAMEWORK', { id: framework.id });
            
            // Résout la promesse (fire-and-forget propre, ou écoute une confirmation si ton handler VSC en renvoie une)
            resolve();
        });
    }
}

// ✅ 4. Export de l'instance utilisant le singleton (plus de "new VscodeService()")
export const frontendServiceVsc = new FrontendServiceVsc();