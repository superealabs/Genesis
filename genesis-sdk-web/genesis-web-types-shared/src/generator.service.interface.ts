import { DatabaseConfig } from './database.shared';
import type { 
    TableMetadataDto, 
    RelationParameter, 
    ProjectConfig, 
    ScriptConfig,
    TableSelectionConfig,
    LlmModelDto, 
    AiPromptPayload,
    AiResponseDto,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData,
    GenerationResult
} from './generator.shared';

import type { FrontendFramework } from './frontend.shared';

/**
 * Contrat du service générateur (Pure Interface).
 */
export interface IGeneratorService {
    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    selectFramework(id: number): Promise<void>;

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
    fetchLoggingLevels(frameworkId: number): Promise<string[]>;
    fetchSecurityTypes(frameworkId: number): Promise<string[]>;
    fetchCacheProviders(frameworkId: number): Promise<string[]>;
    fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]>;
    fetchLanguageVersions(languageId: number): Promise<string[]>;
    fetchFrameworkVersions(frameworkId: number): Promise<string[]>;
    fetchBuildTools(frameworkId: number): Promise<string[]>;
    saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 3 & 4 : BASE DE DONNÉES ═══
    selectDatabase(engineId: number): Promise<void>;
    saveDatabaseConfig(databaseConfig: DatabaseConfig): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 5 : ASSISTANT IA (LLM) ═══
    fetchAvailableLlmModels(): Promise<LlmModelDto[]>;
    generateAiScript(payload: AiPromptPayload): Promise<AiResponseDto>;
    saveScriptConfig(script: ScriptConfig): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 6 : SÉLECTION DES TABLES, VUES ET COMPOSANTS ═══
    fetchTablesMetadata(): Promise<TableMetadataDto[]>;
    saveTableSelection(config: TableSelectionConfig): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 7 : RELATIONS ═══
    fetchRelations(): Promise<RelationParameter[]>;
    saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 8 & 9 : FRONTEND ═══
    /**
     * Sauvegarde le choix du framework frontend (ou du moteur de template MVC) 
     * après validation de l'étape 8.
     */
    saveFrontendSelection(framework: FrontendFramework): Promise<{ success: boolean; message: string }>;

    /**
     * Sauvegarde la configuration du layout frontend (langues d'interface, navbar, couleurs, branding, port).
     */
    saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 10 : GIT ═══
    saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 11 : GÉNÉRATION FINALE ═══
    /**
     * Déclenche la génération complète du projet en envoyant l'état global.
     */
    launchGeneration(data: GeneratorData): Promise<GenerationResult>;
}