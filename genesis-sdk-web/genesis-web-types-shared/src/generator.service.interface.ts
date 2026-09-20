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

import { FrontendFramework } from './frontend.shared';

/**
 * Contrat du service générateur (Pure Interface).
 * Ne contient aucune logique UI ou mutation de store.
 * Sert de source de vérité pour les implémentations Web (Vite) et Extension (VS Code).
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
    /**
     * Récupère la liste des paramètres de relations (existantes ou suggérées par l'IA/le backend).
     */
    fetchRelations(): Promise<RelationParameter[]>;

    /**
     * Sauvegarde la liste complète des relations configurées par l'utilisateur 
     * après validation de l'étape 7 (équivalent à setRelationParameterList).
     * @param relations La liste des relations à persister.
     */
    saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }>;

    selectFrontendFramework(framework: FrontendFramework): Promise<void>;



    saveFrontendSelection(framework: FrontendFramework): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 9 : CONFIGURATION LAYOUT FRONTEND ═══
    /**
     * Sauvegarde la configuration du layout frontend (langues d'interface, navbar, couleurs, branding, port).
     * (Équivalent à setFrontEndConfig())
     */
    saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }>;


    saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }>;

    // ═══ ÉTAPE 11 : GÉNÉRATION FINALE (NOUVEAU) ═══
    /**
     * Déclenche la génération complète du projet.
     * C'est l'action finale qui envoie l'intégralité des données collectées durant le wizard 
     * au backend pour créer les fichiers sur le disque.
     * @param data L'objet GeneratorData contenant l'état complet de toutes les étapes.
     * @returns Le résultat de l'opération (succès, message, chemin absolu du projet généré).
     * vérifie si les informations insérés en finalité correspondent à ceux qui se trouvent côté serveur
     */
    launchGeneration(data: GeneratorData): Promise<GenerationResult>;
}