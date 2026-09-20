import type { 
    IGeneratorService,
    GeneratorData,
    TableMetadataDto,
    RelationParameter,
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    TableSelectionConfig,
    LlmModelDto,
    AiPromptPayload,
    AiResponseDto,
    FrontendLayoutConfig,
    GitConfiguration,
    GenerationResult
} from '@genesis-labs/shared-types';
import { vscodeService } from '../../../core/services/vscode.service';

export class GeneratorServiceVsc implements IGeneratorService {
    constructor(private vscode = vscodeService) {}

    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    selectFramework(frameworkId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_FRAMEWORK', { id: frameworkId });
            
            const cleanup = this.vscode.onMessage<any>('FRAMEWORK_SELECTED', (data) => {
                cleanup();
                if (data.success) resolve();
                else reject(new Error(data.message || 'Échec de la sélection'));
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_FRAMEWORK') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
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

    saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_PROJECT_CONFIG', { config });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('PROJECT_CONFIG_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_PROJECT_CONFIG') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 3 & 4 : BASE DE DONNÉES ═══
    selectDatabase(engineId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_DATABASE', { id: engineId });
            
            const cleanup = this.vscode.onMessage<any>('DATABASE_SELECTED', (data) => {
                cleanup();
                if (data.success) resolve();
                else reject(new Error(data.message || 'Échec de la sélection de la base de données'));
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_DATABASE') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    saveDatabaseConfig(databaseConfig: DatabaseConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_DATABASE_CONFIG', { config: databaseConfig });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('DATABASE_CONFIG_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_DATABASE_CONFIG') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 5 : ASSISTANT IA (LLM) ═══
    fetchAvailableLlmModels(): Promise<LlmModelDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_AVAILABLE_LLM_MODELS');
            const cleanup = this.vscode.onMessage<LlmModelDto[]>('AVAILABLE_LLM_MODELS_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    generateAiScript(payload: AiPromptPayload): Promise<AiResponseDto> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('GENERATE_AI_SCRIPT', payload);
            
            const cleanup = this.vscode.onMessage<AiResponseDto>('AI_SCRIPT_GENERATED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'GENERATE_AI_SCRIPT') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    saveScriptConfig(script: ScriptConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_SCRIPT_CONFIG', { script });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('SCRIPT_CONFIG_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_SCRIPT_CONFIG') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 6 : SÉLECTION DES TABLES, VUES ET COMPOSANTS ═══
    fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_LOADED', (data) => {
                cleanup(); resolve(data);
            });
        });
    }

    saveTableSelection(config: TableSelectionConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_TABLE_SELECTION', { config });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('TABLE_SELECTION_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_TABLE_SELECTION') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 7 : RELATIONS ═══
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

    saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_RELATION_PARAMETERS', { relations });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('RELATION_PARAMETERS_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_RELATION_PARAMETERS') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 8 & 9 : FRONTEND ═══
    selectFrontendFramework(frameworkFrontEndId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_FRONTEND_FRAMEWORK', { id: frameworkFrontEndId });
            
            const cleanup = this.vscode.onMessage<any>('FRONTEND_FRAMEWORK_SELECTED', (data) => {
                cleanup();
                if (data.success) resolve();
                else reject(new Error(data.message || 'Échec de la sélection du framework frontend'));
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_FRONTEND_FRAMEWORK') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_FRONTEND_LAYOUT_CONFIG', { config });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('FRONTEND_LAYOUT_CONFIG_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_FRONTEND_LAYOUT_CONFIG') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 10 : GIT ═══
    saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SAVE_GIT_CONFIGURATION', { config });
            
            const cleanup = this.vscode.onMessage<{ success: boolean; message: string }>('GIT_CONFIGURATION_SAVED', (data) => {
                cleanup(); resolve(data);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SAVE_GIT_CONFIGURATION') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }

    // ═══ ÉTAPE 11 : GÉNÉRATION FINALE ═══
    launchGeneration(data: GeneratorData): Promise<GenerationResult> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('LAUNCH_GENERATION', data);
            
            const cleanup = this.vscode.onMessage<GenerationResult>('GENERATION_RESULT', (result) => {
                cleanup(); resolve(result);
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'LAUNCH_GENERATION') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }
}

export const generatorServiceVsc = new GeneratorServiceVsc();