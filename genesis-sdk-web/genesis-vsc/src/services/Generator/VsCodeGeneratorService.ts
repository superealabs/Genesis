import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IGeneratorService, 
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
    GeneratorData,
    GenerationResult
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Generator Service';

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
const MOCK_TABLES: TableMetadataDto[] = [
    { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
    { tableName: 'produit', className: 'Produit', isView: false },
    { tableName: 'categorie', className: 'Categorie', isView: false },
    { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
    { tableName: 'commande', className: 'Commande', isView: false },
];

const MOCK_PARENT_TABLES: TableMetadataDto[] = [
    { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
    { tableName: 'produit', className: 'Produit', isView: false },
    { tableName: 'categorie', className: 'Categorie', isView: false },
    { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
];

const MOCK_CHILD_TABLES: TableMetadataDto[] = [
    { tableName: 'commande', className: 'Commande', isView: false },
    { tableName: 'facture', className: 'Facture', isView: false },
    { tableName: 'detail_commande', className: 'DetailCommande', isView: false },
    { tableName: 'vue_ventes_mensuelles', className: 'VueVentesMensuelles', isView: true },
];

const MOCK_RELATIONS: RelationParameter[] = [
    { parentTable: 'Utilisateur', childTable: 'Commande', mandatory: true, hasForm: true },
    { parentTable: 'Categorie', childTable: 'Produit', mandatory: true, hasForm: false },
    { parentTable: 'Commande', childTable: 'DetailCommande', mandatory: true, hasForm: true },
];

// ✅ NOUVEAUX MOCKS POUR LES MÉTHODES MANQUANTES
const MOCK_LLM_MODELS: LlmModelDto[] = [
    { id: 'gpt-4o', name: 'GPT-4o', provider: 'OpenAI' },
    { id: 'claude-sonnet-3-5', name: 'Claude 3.5 Sonnet', provider: 'Anthropic' }
];

export class VsCodeGeneratorService implements IGeneratorService {
    
    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata_all');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadata API échouée. Fallback mock.`);
            return MOCK_TABLES;
        }
    }

    async fetchTablesMetadataParents(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/parents');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataParents API échouée. Fallback mock.`);
            return MOCK_PARENT_TABLES;
        }
    }

    async fetchTablesMetadataChilds(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/childs');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataChilds API échouée. Fallback mock.`);
            return MOCK_CHILD_TABLES;
        }
    }

    async fetchRelations(): Promise<RelationParameter[]> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/api/relations');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchRelations API échouée. Fallback mock.`);
            return MOCK_RELATIONS;
        }
    }



    async saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config du projet: ${config.projectName}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/project-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveProjectConfig API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    async selectFramework(frameworkId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du framework ID: ${frameworkId}`);
            await getAxiosInstance().post(`/api/generator/frameworks/${frameworkId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFramework API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    async selectDatabase(engineId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du database ID: ${engineId}`);
            await getAxiosInstance().post(`/api/database/${engineId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectDatabase API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    // ✅ CORRECTION : URL corrigée (était /api/database/...)
    async selectFrontendFramework(frameworkFrontEndId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du frontend framework ID: ${frameworkFrontEndId}`);
            await getAxiosInstance().post(`/api/frontend/${frameworkFrontEndId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFrontendFramework API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    // ═══ NOUVELLES MÉTHODES REQUISES PAR LE CONTRAT ═══

    async saveDatabaseConfig(databaseConfig: DatabaseConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config DB: ${databaseConfig.engine}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/database-config', databaseConfig);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveDatabaseConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async fetchAvailableLlmModels(): Promise<LlmModelDto[]> {
        try {
            const { data } = await getAxiosInstance().get<LlmModelDto[]>('/api/generator/llm-models');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchAvailableLlmModels API échouée. Fallback mock.`);
            return MOCK_LLM_MODELS;
        }
    }

    async generateAiScript(payload: AiPromptPayload): Promise<AiResponseDto> {
        try {
            logger.log(LOG_CHANNEL, `🤖 Génération de script via IA (modèle: ${payload.model})`);
            const { data } = await getAxiosInstance().post<AiResponseDto>('/api/generator/ai-generate', payload);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ generateAiScript API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveScriptConfig(script: ScriptConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde du script: ${script.path}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/script-config', script);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveScriptConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveTableSelection(config: TableSelectionConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la sélection des tables/vues`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/table-selection', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveTableSelection API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde des paramètres de relations (${relations.length} relations)`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/relation-parameters', { relations });
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveRelationParameters API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration du layout frontend`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/frontend-layout-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveFrontendLayoutConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration Git`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/api/generator/git-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveGitConfiguration API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async launchGeneration(data: GeneratorData): Promise<GenerationResult> {
        try {
            logger.log(LOG_CHANNEL, `🚀 Lancement de la génération du projet: ${data.config.projectName}`);
            const { data: result } = await getAxiosInstance().post<GenerationResult>('/api/generator/launch', data);
            return result;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ launchGeneration API échouée: ${(error as Error).message}`);
            throw error;
        }
    }
}