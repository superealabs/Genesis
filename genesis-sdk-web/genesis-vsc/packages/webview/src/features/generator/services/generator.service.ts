import type { 
    IGeneratorService,
    GeneratorData,
    TableMetadataDto,
    RelationParameter,
    ProjectConfig 
} from '@genesis-labs/shared-types';
import { vscodeService } from '../../../core/services/vscode.service';

export class GeneratorServiceVsc implements IGeneratorService {
    constructor(private vscode = vscodeService) {}

    fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchTablesMetadataParents(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA_PARENTS');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_PARENTS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchTablesMetadataChilds(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA_CHILDS');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_CHILDS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    fetchRelations(): Promise<RelationParameter[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_RELATION_PARAMETERS');
            const cleanup = this.vscode.onMessage<RelationParameter[]>('RELATIONS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    // ✅ CORRECTION : Prend frameworkId et retourne string[]
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

    // ✅ AJOUT : Pour compléter le trio
    fetchCacheProviders(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_CACHE_PROVIDERS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('CACHE_PROVIDERS_LOADED', (data) => {
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

    generateProject(data: GeneratorData): Promise<{ success: boolean; message?: string }> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GENERATE_PROJECT', data);
            const cleanup = this.vscode.onMessage<{ success: boolean; message?: string }>('PROJECT_GENERATED', (result) => {
                cleanup(); resolve(result);
            });
        });
    }

    fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_HIBERNATE_DDL_AUTO_OPTIONS', { frameworkId });
            const cleanup = this.vscode.onMessage<string[]>('HIBERNATE_DDL_AUTO_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }


    selectFramework(frameworkId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_FRAMEWORK', { id: frameworkId });
            
            // Écoute du succès
            const cleanup = this.vscode.onMessage<any>('FRAMEWORK_SELECTED', (data) => {
                cleanup();
                if (data.success) {
                    resolve();
                } else {
                    // Sécurité au cas où le backend renvoie success: false sans erreur HTTP
                    reject(new Error(data.message || 'Échec de la sélection'));
                }
            });

            // CRUCIAL : Écoute spécifique des erreurs pour cette commande
            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_FRAMEWORK') {
                    cleanup();
                    errorCleanup(); // Nettoyer les écouteurs pour éviter les fuites
                    reject(new Error(data.message)); // <-- C'est CE reject qui déclenchera ton catch dans useGenerator
                }
            });
        });
    }

    saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_PROJECT_CONFIG', { config });
            
            // Écoute du succès
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>(
                'PROJECT_CONFIG_SAVED', 
                (data) => {
                    cleanup();
                    resolve(data);
                }
            );

            // Écoute des erreurs spécifiques à cette commande
            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_PROJECT_CONFIG') {
                    cleanup();
                    errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }
}

export const generatorServiceVsc = new GeneratorServiceVsc();