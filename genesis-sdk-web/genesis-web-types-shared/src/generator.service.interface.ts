import { DatabaseConfig } from './database.shared';
import type { 
    TableMetadataDto, 
    RelationParameter, 
    ProjectConfig, 
    GenerationParameter,
    LlmModelDto, 
    AiPromptPayload,
    AiResponseDto,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData,
    GenerationResult
} from './generator.shared';

import type { ScriptConfig, SqlExecutionResponse, SqlScriptRequest, SqlValidationResponse  } from './sql/sql.types';

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
    validateSqlScript(request: SqlScriptRequest): Promise<SqlValidationResponse>;
    executeSqlScript(request: SqlScriptRequest): Promise<SqlExecutionResponse>;

    // ═══ ÉTAPE 6 : SÉLECTION DES TABLES, VUES ET COMPOSANTS ═══
    fetchTablesMetadata(): Promise<TableMetadataDto[]>;

    // est un post au début mais normalement doit être un put après
    saveGenerationParameters(params: GenerationParameter[]): Promise<{ success: boolean; message: string }>;


    // ═══ ÉTAPE 7 : RELATIONS ═══
    fetchTablesMetadataParents(childTableName?: String): Promise<TableMetadataDto[]>;
    fetchTablesMetadataChilds(parentTableName?: String): Promise<TableMetadataDto[]>;
    
    fetchRelations(): Promise<RelationParameter[]>;

    // est obligatoirement une MAJ
    saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 8 & 9 : FRONTEND ═══

    /**
     * Configure le framework frontend (frameworkId + languageId + port).
     * POST /api/generator/frontend-config
     * 
     * @param frameworkId ID du framework frontend (ex: 5)
     * @param languageId ID du langage de programmation (null si MVC)
     * @param port Port d'exécution (string, ex: '3000')
     */
    saveFrontendConfig(
        frameworkId: number,
        languageId: number | null,
        port: string
    ): Promise<{ success: boolean; message: string }>

    /**
     * Sauvegarde le choix du framework frontend (ou du moteur de template MVC) 
     * après validation de l'étape 8.
     */
    selectFrontendFramework(frameworkFrontEndId: number): Promise<void>

    /**
     * Sauvegarde la configuration du layout frontend (langues d'interface, navbar, couleurs, branding, port).
     */
    saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }>;
    /**
     * Upload un asset frontend (logo ou favicon) via form-data.
     * POST /api/generator/frontend/assets/:kind
     * 
     * @param kind 'logo' | 'favicon'
     * @param base64Content Contenu base64 du fichier
     * @param mimeType MIME type (ex: 'image/png')
     * @returns L'ID de l'asset uploadé + éventuellement d'autres métadonnées
     */
    uploadFrontendAsset(
        kind: 'logo' | 'favicon',
        base64Content: string,
        mimeType: string
    ): Promise<{ assetId: string; [key: string]: any }>;

    // ═══ ÉTAPE 10 : GIT ═══
    saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }>;

    /**
     * Initialise le dépôt Git côté backend (commit + push si remote configuré).
     * POST /api/generator/git/init
     * ⚠️ Non bloquant : si l'utilisateur n'a pas de réseau, l'erreur doit être
     * tolérée côté appelant (la génération a déjà réussi).
     */
    initGitRepository(): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 11 : GÉNÉRATION FINALE ═══
    /**
     * Déclenche la génération complète du projet en envoyant l'état global.
     */
    launchGeneration(data?: GeneratorData): Promise<GenerationResult>;



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