import { DatabaseConfig } from './database.shared';
import type { 
    TableMetadataDto, 
    RelationParameter, 
    ProjectConfig, 
    ScriptConfig,
    GenerationParameter,
    LlmModelDto, 
    AiPromptPayload,
    AiResponseDto,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData,
    GenerationResult
} from './generator.shared';

/**
 * Contrat du service générateur (Pure Interface).
 */
export interface IGeneratorService {

    // initialisateur
    selectGenerationMode(modes?: String): Promise<void>;

    // ═══ ÉTAPE 1 : FRAMEWORK ═══
    selectFramework(id: number): Promise<void>;

    // ═══ ÉTAPE 2 : CONFIGURATION PROJET ═══
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

    // est un post au début mais normalement doit être un put après
    saveGenerationParameters(params: GenerationParameter[]): Promise<{ success: boolean; message: string }>;


    // ═══ ÉTAPE 7 : RELATIONS ═══
    fetchTablesMetadataParents(): Promise<TableMetadataDto[]>;
    fetchTablesMetadataChilds(): Promise<TableMetadataDto[]>;
    fetchRelations(): Promise<RelationParameter[]>;

    // est obligatoirement une MAJ
    saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 8 & 9 : FRONTEND ═══
    /**
     * Sauvegarde le choix du framework frontend (ou du moteur de template MVC) 
     * après validation de l'étape 8.
     */
    selectFrontendFramework(frameworkFrontEndId: number): Promise<void>

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



    // utils

    // ═══ RESET / RÉINITIALISATION ═══
    /**
     * Réinitialise la configuration frontend côté backend.
     * DELETE /api/generator/frontend-config
     */
    resetFrontend(): Promise<{ success: boolean; message: string }>;

    /**
     * Réinitialise la configuration Git côté backend.
     * DELETE /api/generator/git/config
     */
    resetGit(): Promise<{ success: boolean; message: string }>;

    /**
     * Réinitialise TOUT le contexte Genesis côté backend.
     * DELETE /api/generator/reset
     */
    resetAll(): Promise<{ success: boolean; message: string }>;
    // rajouter une fonction qui clear le tout
}