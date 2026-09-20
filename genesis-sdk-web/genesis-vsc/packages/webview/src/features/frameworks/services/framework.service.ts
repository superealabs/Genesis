// import type { IFrameworkService, Framework } from '@genesis-labs/core/features/frameworks/manifest';
import { CoreFramework, Framework, IFrameworkService, Language, ViewTemplate } from '@genesis-labs/shared-types';

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
                resolve(data); // Retourne la donnée brute, NE TOUCHE PAS AU STORE
            });
        });
    }

    fetchLanguages(): Promise<Language[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_LANGUAGES');
            
            const cleanup = this.vscode.onMessage<Language[]>('LANGUAGES_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchCoreFrameworks(): Promise<CoreFramework[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_CORE_FRAMEWORKS');
            
            const cleanup = this.vscode.onMessage<CoreFramework[]>('CORE_FRAMEWORKS_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchViewTemplates(): Promise<ViewTemplate[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_VIEW_TEMPLATES');
            
            const cleanup = this.vscode.onMessage<ViewTemplate[]>('VIEW_TEMPLATES_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }
}

// ✅ 3. Instanciation sans argument : elle utilisera automatiquement le singleton
export const frameworkServiceVsc = new FrameworkServiceVsc();