import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IGeneratorService, 
    TableMetadataDto, 
    RelationParameter,
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    GenerationParameter,
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
  // --- Données existantes ---
  { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
  { tableName: 'produit', className: 'Produit', isView: false },
  { tableName: 'categorie', className: 'Categorie', isView: false },
  { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
  { tableName: 'commande', className: 'Commande', isView: false },
  
  // --- 10 Nouvelles entrées pour le test de charge ---
  { tableName: 'adresse', className: 'Adresse', isView: false },
  { tableName: 'paiement', className: 'Paiement', isView: false },
  { tableName: 'facture', className: 'Facture', isView: false },
  { tableName: 'vue_chiffre_affaires_mensuel', className: 'VueChiffreAffairesMensuel', isView: true },
  { tableName: 'employe', className: 'Employe', isView: false },
  { tableName: 'departement', className: 'Departement', isView: false },
  { tableName: 'vue_statistiques_produits', className: 'VueStatistiquesProduits', isView: true },
  { tableName: 'journal_connexion', className: 'JournalConnexion', isView: false },
  { tableName: 'parametre_systeme', className: 'ParametreSysteme', isView: false },
  { tableName: 'vue_utilisateurs_inactifs', className: 'VueUtilisateursInactifs', isView: true },
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

    async selectGenerationMode(modes?: String): Promise<void> {
        try {
            // Valeur par défaut si modes est nul, undefined ou vide
            const mode = (modes && String(modes).trim()) || 'GENERATE_NEW_PROJECT';

            logger.log(LOG_CHANNEL, `🔄 Init choix de génération : mode=${mode}`);

            await getAxiosInstance().post('/generation/modes/select', {
                mode
            });
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ InitGenerateNewProjectChoice API échouée: ${(error as Error).message}`);
            throw error;
        }
    }
    
    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/tables_metadata_all');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadata API échouée. Fallback mock.`);
            return MOCK_TABLES;
        }
    }

    async fetchTablesMetadataParents(childTableName?: String): Promise<TableMetadataDto[]> {
        const url = '/tables_metadata/parents';
        const fullUrl = (getAxiosInstance().defaults.baseURL || '') + url;
        const params = childTableName ? { childTableName } : undefined;

        logger.log(LOG_CHANNEL, `📤 [fetchTablesMetadataParents] Appel GET ${fullUrl}`, true);

        try {
            const response = await getAxiosInstance().get<TableMetadataDto[]>(url, { params });

            // 👇 Log la réponse RÉELLE (statut + data)
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Statut: ${response.status}`, true);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Data reçue: ${JSON.stringify(response.data)}`, true);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Nombre d'éléments: ${response.data?.length ?? 'null/undefined'}`, true);

            // 👇 Détecte les cas suspects
            if (!response.data) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data est null/undefined !`);
            } else if (!Array.isArray(response.data)) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data n'est pas un tableau ! Type: ${typeof response.data}`);
            } else if (response.data.length === 0) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data est un tableau VIDE !`);
            }

            return response.data;
        } catch (error) {
            // 👇 Log l'erreur COMPLÈTE
            logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Erreur: ${(error as Error).message}`);

            if (error && typeof error === 'object' && 'response' in error) {
                const axiosError = error as any;
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Statut HTTP: ${axiosError.response?.status}`, true);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Data erreur: ${JSON.stringify(axiosError.response?.data)}`, true);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] URL: ${axiosError.config?.url}`, true);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] BaseURL: ${axiosError.config?.baseURL}`, true);
            }

            logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] Fallback mock activé.`);
            return MOCK_PARENT_TABLES;
        }
    }

    async fetchTablesMetadataChilds(parentTableName?: String): Promise<TableMetadataDto[]> {
        const baseURL = getAxiosInstance().defaults.baseURL || '(undefined)';
        const fullUrl = `${baseURL}/tables_metadata/childs`;
        const params = parentTableName ? { parentTableName } : undefined;
        logger.log(LOG_CHANNEL, `📤 [fetchTablesMetadataChilds] GET ${fullUrl}`);
        const url = '/tables_metadata/childs';

        try {
            const response = await getAxiosInstance().get<TableMetadataDto[]>(url, { params });

            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Status: ${response.status}`);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Data: ${JSON.stringify(response.data)}`);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Length: ${response.data?.length ?? 'N/A'}`);

            if (!response.data) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data = null/undefined`);
            } else if (!Array.isArray(response.data)) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data n'est PAS un tableau (type: ${typeof response.data})`);
            } else if (response.data.length === 0) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data = [] (tableau vide)`);
            }

            return response.data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Erreur: ${(error as Error).message}`);

            if (error && typeof error === 'object' && 'response' in error) {
                const axiosError = error as any;
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] HTTP Status: ${axiosError.response?.status}`);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Response Data: ${JSON.stringify(axiosError.response?.data)}`);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Config URL: ${axiosError.config?.url}`);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Config BaseURL: ${axiosError.config?.baseURL}`);
            }

            logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] Fallback mock activé.`);
            return MOCK_CHILD_TABLES;
        }
    }
    async fetchRelations(): Promise<RelationParameter[]> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/relations');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchRelations API échouée. Fallback mock.`);
            return MOCK_RELATIONS;
        }
    }



    async saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config du projet: ${config.projectName}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/project-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveProjectConfig API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    async selectFramework(frameworkId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du framework ID: ${frameworkId}`);
            await getAxiosInstance().post(`/generator/frameworks/${frameworkId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFramework API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    async selectDatabase(engineId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du database ID: ${engineId}`);
            await getAxiosInstance().post(`/database/${engineId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectDatabase API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    // ✅ CORRECTION : URL corrigée (était /database/...)


    // inutile
    async selectFrontendFramework(frameworkFrontEndId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du frontend framework ID: ${frameworkFrontEndId}`);
            await getAxiosInstance().post(`/frontend/${frameworkFrontEndId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFrontendFramework API échouée: ${(error as Error).message}`);
            throw error; 
        }
    }

    // ═══ NOUVELLES MÉTHODES REQUISES PAR LE CONTRAT ═══
    async saveDatabaseConfig(databaseConfig: DatabaseConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config DB: ${databaseConfig.engine}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/database-config', databaseConfig);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveDatabaseConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async fetchAvailableLlmModels(): Promise<LlmModelDto[]> {
        try {
            const { data } = await getAxiosInstance().get<LlmModelDto[]>('/generator/llm-models');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchAvailableLlmModels API échouée. Fallback mock.`);
            return MOCK_LLM_MODELS;
        }
    }

    async generateAiScript(payload: AiPromptPayload): Promise<AiResponseDto> {
        try {
            logger.log(LOG_CHANNEL, `🤖 Génération de script via IA (modèle: ${payload.model})`);
            const { data } = await getAxiosInstance().post<AiResponseDto>('/generator/ai-generate', payload);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ generateAiScript API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveScriptConfig(script: ScriptConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde du script: ${script.path}`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/script-config', script);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveScriptConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveGenerationParameters(params: GenerationParameter[]): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde des paramètres de génération (${params.length} profils)`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/generation-parameters', params);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveGenerationParameters API échouée: ${(error as Error).message}`, true);
            throw error;
        }
    }

    async saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde des paramètres de relations (${relations.length} relations)`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/relation-parameters', { relations });
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveRelationParameters API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration du layout frontend`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/frontend-layout-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveFrontendLayoutConfig API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration Git`);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/git-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveGitConfiguration API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async launchGeneration(data: GeneratorData): Promise<GenerationResult> {
        try {
            logger.log(LOG_CHANNEL, `🚀 Lancement de la génération du projet: ${data.config.projectName}`);
            
            // Tentative d'appel à l'API (qui échouera pour l'instant)
            const { data: result } = await getAxiosInstance().post<GenerationResult>('/generator/launch', data);
            return result;
            
        } catch (error) {
            // ✅ FORCAGE / FALLBACK POUR LE DÉVELOPPEMENT (API non encore prête)
            logger.log(LOG_CHANNEL, `⚠️ launchGeneration API échouée. Activation du FALLBACK MOCK pour tester le flux.`);
            
            // Construction d'un chemin de projet simulé basé sur les données saisies par l'utilisateur
            const simulatedPath = `${data.config.projectLocation}/${data.config.projectName}`;
            
            const mockResult: GenerationResult = {
                success: true,
                message: "Projet généré avec succès (Mode Simulation).",
                projectPath: simulatedPath
            };
            
            // Petit délai artificiel (1.5s) pour simuler le temps de traitement du backend 
            // et permettre à l'UI d'afficher l'état de chargement ("Génération en cours...")
            await new Promise(resolve => setTimeout(resolve, 1500));

            return mockResult;
        }
    }

    // ═══ RESET / RÉINITIALISATION ═══

    async resetFrontend(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset de la configuration frontend`);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/frontend-config'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetFrontend API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async resetGit(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset de la configuration Git`);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/git/config'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetGit API échouée: ${(error as Error).message}`);
            throw error;
        }
    }

    async resetAll(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset complet du contexte Genesis`);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/reset'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetAll API échouée: ${(error as Error).message}`);
            throw error;
        }
    }
}