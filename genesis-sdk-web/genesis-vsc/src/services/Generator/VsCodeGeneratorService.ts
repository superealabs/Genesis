// src/services/Generator/VsCodeGeneratorService.ts
import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import FormData from 'form-data';
import type {
    IGeneratorService,
    TableMetadataDto,
    RelationParameter,
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    ScriptConfigResponse,
    SqlScriptRequest,
    SqlValidationResponse,
    SqlExecutionResponse,
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


function getExtensionFromMime(mimeType: string): string {
    const map: Record<string, string> = {
        'image/png': 'png',
        'image/jpeg': 'jpg',
        'image/gif': 'gif',
        'image/svg+xml': 'svg',
        'image/x-icon': 'ico',
        'image/vnd.microsoft.icon': 'ico',
        'image/webp': 'webp',
    };
    return map[mimeType] || 'bin';
}

export class VsCodeGeneratorService implements IGeneratorService {

    /**
     * Active/désactive les logs de ce service.
     * Passe à `true` pour tracer les appels API, `false` en production.
     */
    public static DEBUG = false;

    async selectGenerationMode(modes?: String): Promise<void> {
        try {
            // Valeur par défaut si modes est nul, undefined ou vide
            const mode = (modes && String(modes).trim()) || 'GENERATE_NEW_PROJECT';

            logger.log(LOG_CHANNEL, `🔄 Init choix de génération : mode=${mode}`, VsCodeGeneratorService.DEBUG);

            await getAxiosInstance().post('/generation/modes/select', {
                mode
            });
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ InitGenerateNewProjectChoice API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/tables_metadata_all');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadata API échouée. Fallback mock.`, VsCodeGeneratorService.DEBUG);
            return MOCK_TABLES;
        }
    }

    async fetchTablesMetadataParents(childTableName?: String): Promise<TableMetadataDto[]> {
        const url = '/tables_metadata/parents';
        const fullUrl = (getAxiosInstance().defaults.baseURL || '') + url;
        const params = childTableName ? { childTableName } : undefined;

        logger.log(LOG_CHANNEL, `📤 [fetchTablesMetadataParents] Appel GET ${fullUrl}`, VsCodeGeneratorService.DEBUG);

        try {
            const response = await getAxiosInstance().get<TableMetadataDto[]>(url, { params });

            // 👇 Log la réponse RÉELLE (statut + data)
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Statut: ${response.status}`, VsCodeGeneratorService.DEBUG);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Data reçue: ${JSON.stringify(response.data)}`, VsCodeGeneratorService.DEBUG);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataParents] Nombre d'éléments: ${response.data?.length ?? 'null/undefined'}`, VsCodeGeneratorService.DEBUG);

            // 👇 Détecte les cas suspects
            if (!response.data) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data est null/undefined !`, VsCodeGeneratorService.DEBUG);
            } else if (!Array.isArray(response.data)) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data n'est pas un tableau ! Type: ${typeof response.data}`, VsCodeGeneratorService.DEBUG);
            } else if (response.data.length === 0) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] response.data est un tableau VIDE !`, VsCodeGeneratorService.DEBUG);
            }

            return response.data;
        } catch (error) {
            // 👇 Log l'erreur COMPLÈTE
            logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Erreur: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);

            if (error && typeof error === 'object' && 'response' in error) {
                const axiosError = error as any;
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Statut HTTP: ${axiosError.response?.status}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] Data erreur: ${JSON.stringify(axiosError.response?.data)}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] URL: ${axiosError.config?.url}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataParents] BaseURL: ${axiosError.config?.baseURL}`, VsCodeGeneratorService.DEBUG);
            }

            logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataParents] Fallback mock activé.`, VsCodeGeneratorService.DEBUG);
            return MOCK_PARENT_TABLES;
        }
    }

    async fetchTablesMetadataChilds(parentTableName?: String): Promise<TableMetadataDto[]> {
        const baseURL = getAxiosInstance().defaults.baseURL || '(undefined)';
        const fullUrl = `${baseURL}/tables_metadata/childs`;
        const params = parentTableName ? { parentTableName } : undefined;
        logger.log(LOG_CHANNEL, `📤 [fetchTablesMetadataChilds] GET ${fullUrl}`, VsCodeGeneratorService.DEBUG);
        const url = '/tables_metadata/childs';

        try {
            const response = await getAxiosInstance().get<TableMetadataDto[]>(url, { params });

            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Status: ${response.status}`, VsCodeGeneratorService.DEBUG);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Data: ${JSON.stringify(response.data)}`, VsCodeGeneratorService.DEBUG);
            logger.log(LOG_CHANNEL, `✅ [fetchTablesMetadataChilds] Length: ${response.data?.length ?? 'N/A'}`, VsCodeGeneratorService.DEBUG);

            if (!response.data) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data = null/undefined`, VsCodeGeneratorService.DEBUG);
            } else if (!Array.isArray(response.data)) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data n'est PAS un tableau (type: ${typeof response.data})`, VsCodeGeneratorService.DEBUG);
            } else if (response.data.length === 0) {
                logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] response.data = [] (tableau vide)`, VsCodeGeneratorService.DEBUG);
            }

            return response.data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Erreur: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);

            if (error && typeof error === 'object' && 'response' in error) {
                const axiosError = error as any;
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] HTTP Status: ${axiosError.response?.status}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Response Data: ${JSON.stringify(axiosError.response?.data)}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Config URL: ${axiosError.config?.url}`, VsCodeGeneratorService.DEBUG);
                logger.log(LOG_CHANNEL, `❌ [fetchTablesMetadataChilds] Config BaseURL: ${axiosError.config?.baseURL}`, VsCodeGeneratorService.DEBUG);
            }

            logger.log(LOG_CHANNEL, `⚠️ [fetchTablesMetadataChilds] Fallback mock activé.`, VsCodeGeneratorService.DEBUG);
            return MOCK_CHILD_TABLES;
        }
    }

    async fetchRelations(): Promise<RelationParameter[]> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/relations');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchRelations API échouée. Fallback mock.`, VsCodeGeneratorService.DEBUG);
            return MOCK_RELATIONS;
        }
    }

    async saveProjectConfig(config: ProjectConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config du projet: ${config.projectName}`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/project-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveProjectConfig API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async selectFramework(frameworkId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du framework ID: ${frameworkId}`, VsCodeGeneratorService.DEBUG);
            await getAxiosInstance().post(`/generator/frameworks/${frameworkId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFramework API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async selectDatabase(engineId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du database ID: ${engineId}`, VsCodeGeneratorService.DEBUG);
            await getAxiosInstance().post(`/database/${engineId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectDatabase API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    // inutile
    async selectFrontendFramework(frameworkFrontEndId: number): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Tentative de sélection du frontend framework ID: ${frameworkFrontEndId}`, VsCodeGeneratorService.DEBUG);
            await getAxiosInstance().post(`/frontend/${frameworkFrontEndId}/select`);
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ selectFrontendFramework API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    // ═══ NOUVELLES MÉTHODES REQUISES PAR LE CONTRAT ═══
    async saveDatabaseConfig(databaseConfig: DatabaseConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config DB: ${databaseConfig.engine}`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/database-config', databaseConfig);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveDatabaseConfig API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async fetchAvailableLlmModels(): Promise<LlmModelDto[]> {
        try {
            const { data } = await getAxiosInstance().get<LlmModelDto[]>('/generator/llm-models');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchAvailableLlmModels API échouée. Fallback mock.`, VsCodeGeneratorService.DEBUG);
            return MOCK_LLM_MODELS;
        }
    }

    async generateAiScript(payload: AiPromptPayload): Promise<AiResponseDto> {
        try {
            logger.log(LOG_CHANNEL, `🤖 Génération de script via IA (modèle: ${payload.model})`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<AiResponseDto>('/generator/ai-generate', payload);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ generateAiScript API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async saveScriptConfig(script: ScriptConfig): Promise<ScriptConfigResponse> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde du script: ${script.path}`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<ScriptConfigResponse>(
            '/generator/script-config',
            script
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveScriptConfig API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

        // ═══ VALIDATION / EXÉCUTION SQL ═══

    async validateSqlScript(request: SqlScriptRequest): Promise<SqlValidationResponse> {
    try {
        logger.log(LOG_CHANNEL, `🔍 Validation SQL (${request.script.length} chars)`, VsCodeGeneratorService.DEBUG);
        const { data } = await getAxiosInstance().post<SqlValidationResponse>(
        '/generator/sql/validate',
        request
        );
        logger.log(LOG_CHANNEL, `✅ Validation SQL → valid=${data.valid}`, VsCodeGeneratorService.DEBUG);
        return data;
    } catch (error) {
        logger.log(LOG_CHANNEL, `❌ validateSqlScript API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
        throw error;
    }
    }

    async executeSqlScript(request: SqlScriptRequest): Promise<SqlExecutionResponse> {
    try {
        logger.log(LOG_CHANNEL, `▶️ Exécution SQL (${request.script.length} chars)`, VsCodeGeneratorService.DEBUG);
        const { data } = await getAxiosInstance().post<SqlExecutionResponse>(
        '/generator/sql/execute',
        request
        );
        logger.log(LOG_CHANNEL, `✅ Exécution SQL → success=${data.success}`, VsCodeGeneratorService.DEBUG);
        return data;
    } catch (error) {
        logger.log(LOG_CHANNEL, `❌ executeSqlScript API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
        throw error;
    }
    }

    async saveGenerationParameters(params: GenerationParameter[]): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde des paramètres de génération (${params.length} profils)`, VsCodeGeneratorService.DEBUG);

            // ✅ JSON.stringify avec 'null' et '2' pour un formatage lisible (pretty-print)
            logger.log(LOG_CHANNEL, `💾 génération envoyé au backend : \n${JSON.stringify(params, null, 2)}`, VsCodeGeneratorService.DEBUG);

            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/generation-parameters', params);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveGenerationParameters API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async saveRelationParameters(relations: RelationParameter[]): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde des paramètres de relations (${relations.length} relations)`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/relation-parameters', { relations });
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveRelationParameters API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async saveFrontendConfig(
        frameworkId: number,
        languageId: number | null,
        port: string
    ): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la config frontend (frameworkId=${frameworkId}, languageId=${languageId}, port=${port})`, VsCodeGeneratorService.DEBUG);

            const body = {
                frameworkId,
                languageId,
                port,
            };

            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>(
                '/generator/frontend-config',
                body
            );

            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveFrontendConfig API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async saveFrontendLayoutConfig(config: FrontendLayoutConfig): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration du layout frontend`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/frontend-layout-config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveFrontendLayoutConfig API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async uploadFrontendAsset(
        kind: 'logo' | 'favicon',
        base64Content: string,
        mimeType: string
    ): Promise<{ assetId: string; [key: string]: any }> {
        try {
            logger.log(LOG_CHANNEL, `📤 Upload asset frontend (${kind})`, VsCodeGeneratorService.DEBUG);

            const formData = new FormData();
            formData.append('file', Buffer.from(base64Content, 'base64'), {
                filename: `${kind}.${getExtensionFromMime(mimeType)}`,
                contentType: mimeType
            });

            const { data } = await getAxiosInstance().post(
                `/generator/frontend/assets/${kind}`,
                formData,
                { headers: formData.getHeaders() }
            );

            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ uploadFrontendAsset API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async saveGitConfiguration(config: GitConfiguration): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `💾 Sauvegarde de la configuration Git`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>('/generator/git/config', config);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ saveGitConfiguration API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async initGitRepository(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Initialisation du dépôt Git`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().post<{ success: boolean; message: string }>(
                '/generator/git/init'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ initGitRepository API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async launchGeneration(): Promise<GenerationResult> {
        try {
            logger.log(LOG_CHANNEL, `🚀 Lancement de la génération du projet`, VsCodeGeneratorService.DEBUG);

            // ✅ AJOUT : 3ème argument pour la configuration Axios
            const { data: result } = await getAxiosInstance().post<GenerationResult>(
                '/generator/launch',
                {}, // 2ème argument : les données (vide ici, mais nécessaire pour placer le 3ème argument)
                {
                    timeout: 180000 // ⏱️ Durée en millisecondes (ici 180 000 ms = 3 minutes)
                }
            );

            return result;
        } catch (error: any) {
            // ✅ AMÉLIORATION : Détecter spécifiquement si l'erreur est un timeout Axios
            if (error.code === 'ECONNABORTED') {
                logger.log(LOG_CHANNEL, `❌ launchGeneration API échouée : Le délai d'attente (timeout) de 3 minutes a été dépassé.`, VsCodeGeneratorService.DEBUG);
            } else {
                logger.log(LOG_CHANNEL, `❌ launchGeneration API échouée : ${error.message}`, VsCodeGeneratorService.DEBUG);
            }
            throw error;
        }
    }

    // ═══ RESET / RÉINITIALISATION ═══

    async resetFrontend(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset de la configuration frontend`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/frontend-config'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetFrontend API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async resetGit(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset de la configuration Git`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/git/config'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetGit API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }

    async resetAll(): Promise<{ success: boolean; message: string }> {
        try {
            logger.log(LOG_CHANNEL, `🔄 Reset complet du contexte Genesis`, VsCodeGeneratorService.DEBUG);
            const { data } = await getAxiosInstance().delete<{ success: boolean; message: string }>(
                '/generator/reset'
            );
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ resetAll API échouée: ${(error as Error).message}`, VsCodeGeneratorService.DEBUG);
            throw error;
        }
    }
}