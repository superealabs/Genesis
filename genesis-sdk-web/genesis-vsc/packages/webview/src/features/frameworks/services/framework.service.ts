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

    fetchLoggingLevels(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_LOGGING_LEVELS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('LOGGING_LEVELS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchSecurityTypes(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_SECURITY_TYPES', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('SECURITY_TYPES_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchCacheProviders(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_CACHE_PROVIDERS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('CACHE_PROVIDERS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_HIBERNATE_DDL_AUTO_OPTIONS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('HIBERNATE_DDL_AUTO_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchLanguageVersions(languageId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_LANGUAGE_VERSIONS', { languageId });
            const cleanup = this.vscode.onMessage<string[]>('LANGUAGE_VERSIONS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchFrameworkVersions(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_FRAMEWORK_VERSIONS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('FRAMEWORK_VERSIONS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchBuildTools(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_BUILD_TOOLS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('BUILD_TOOLS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }
}

// ✅ 3. Instanciation sans argument : elle utilisera automatiquement le singleton
export const frameworkServiceVsc = new FrameworkServiceVsc();