import type { IFrameworkService, Framework } from '@genesis-labs/core/features/frameworks/manifest';

// ✅ 1. Import de l'INSTANCE singleton (et non de la classe)
import { vscodeService } from '../../../core/services/vscode.service';

export class FrameworkServiceVsc implements IFrameworkService {
    
    // ✅ 2. Utilise l'instance singleton par défaut
    constructor(private vscode = vscodeService) {}

    fetchFrameworks(): Promise<Framework[]> {
            console.log("démarrage de la récupération de framework")        
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRAMEWORKS');
            console.log("récupération de framework")
            // Écoute UNE SEULE FOIS, puis cleanup pour éviter les fuites mémoire
            const cleanup = this.vscode.onMessage<Framework[]>('FRAMEWORKS_LOADED', (data) => {
                cleanup();
                resolve(data); // ✅ Retourne la donnée brute, NE TOUCHE PAS AU STORE
            });
        });
    }

    selectFramework(id: number): Promise<void> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('SELECT_FRAMEWORK', { id });
            
            const cleanup = this.vscode.onMessage<{ success: boolean }>('FRAMEWORK_SELECTED', (data) => {
                cleanup();
                if (data.success) {
                    console.log('Framework sélectionné avec succès');
                }
                resolve();
            });
        });
    }
}

// ✅ 3. Instanciation sans argument : elle utilisera automatiquement le singleton
export const frameworkServiceVsc = new FrameworkServiceVsc();