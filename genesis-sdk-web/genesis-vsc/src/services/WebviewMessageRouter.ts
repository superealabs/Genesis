import * as vscode from 'vscode';
import { GenesisApiService } from './GenesisApiService';
import { FrameworkHandler } from './Framework/FrameworkHandler';
import { GeneratorHandler } from './Generator/GeneratorHandler';
import { FrontendHandler } from './FrontEnd/FrontendFrameworkHandler';
import { DatabaseHandler } from './database/DatabaseHandler';

export class WebviewMessageRouter {
    private frameworkHandler: FrameworkHandler;
    private generatorHandler: GeneratorHandler;
    private frontendHandler: FrontendHandler;
    private databaseHandler: DatabaseHandler;

    constructor(
        private panel: vscode.WebviewPanel,
        private genesisApi: GenesisApiService,
        private context: vscode.ExtensionContext
    ) {
        // Initialisation des handlers
        this.frameworkHandler = new FrameworkHandler(); 
        this.generatorHandler = new GeneratorHandler(this.panel);
        this.frontendHandler = new FrontendHandler(this.panel);
        this.databaseHandler = new DatabaseHandler(this.panel);
    }

    /**
     * Méthode publique appelée par GenesisPanel pour router les messages métier
     */
    public async route(message: any): Promise<void> {
        try {
            switch (message.type) {
                // ═══ FRAMEWORKS (Étape 1) ═══
                case 'GET_FRAMEWORKS':
                    await this.frameworkHandler.getAll(message.payload, this.panel);
                    break;
                case 'GET_LANGUAGES':
                    await this.frameworkHandler.getLanguages(message.payload, this.panel);
                    break;
                case 'GET_CORE_FRAMEWORKS':
                    await this.frameworkHandler.getCoreFrameworks(message.payload, this.panel);
                    break;
                case 'GET_VIEW_TEMPLATES':
                    await this.frameworkHandler.getViewTemplates(message.payload, this.panel);
                    break;

                // ═══ GENERATOR : CONFIGURATION PROJET (Étape 2) ═══
                case 'GET_LOGGING_LEVELS':
                    await this.generatorHandler.handleGetLoggingLevels(message.payload, this.panel);
                    break;
                case 'GET_SECURITY_TYPES':
                    await this.generatorHandler.handleGetSecurityTypes(message.payload, this.panel);
                    break;
                case 'GET_LANGUAGE_VERSIONS':
                    await this.generatorHandler.handleGetLanguageVersions(message.payload, this.panel);
                    break;
                case 'GET_FRAMEWORK_VERSIONS':
                    await this.generatorHandler.handleGetFrameworkVersions(message.payload, this.panel);
                    break;
                case 'GET_BUILD_TOOLS':
                    await this.generatorHandler.handleGetBuildTools(message.payload, this.panel);
                    break;
                case 'GET_CACHE_PROVIDERS':
                    await this.generatorHandler.handleGetCacheProviders(message.payload, this.panel);
                    break;
                case 'GET_HIBERNATE_DDL_AUTO_OPTIONS':
                    await this.generatorHandler.handleGetHibernateDdlAutoOptions(message.payload, this.panel);
                    break;
                case 'SAVE_PROJECT_CONFIG':
                    await this.generatorHandler.handleSaveProjectConfig(message.payload, this.panel);
                    break;

                // ═══ GENERATOR : BASE DE DONNÉES (Étapes 3 & 4) ═══
                case 'GET_DATABASE_ENGINES':
                    await this.databaseHandler.handleGetAvailableEngines(message.payload, this.panel);
                    break;
                case 'TEST_DATABASE_CONNECTION':
                    await this.databaseHandler.handleTestDatabaseConnection(message.payload, this.panel);
                    break;
                case 'SELECT_DATABASE':
                    await this.generatorHandler.handleSelectDatabase(message.payload, this.panel);
                    break;
                case 'SAVE_DATABASE_CONFIG':
                    await this.generatorHandler.handleSaveDatabaseConfig(message.payload, this.panel);
                    break;

                // ═══ GENERATOR : ASSISTANT IA (Étape 5) ═══
                case 'GET_AVAILABLE_LLM_MODELS':
                    await this.generatorHandler.handleGetAvailableLlmModels(message.payload, this.panel);
                    break;
                case 'GENERATE_AI_SCRIPT':
                    await this.generatorHandler.handleGenerateAiScript(message.payload, this.panel);
                    break;
                case 'SAVE_SCRIPT_CONFIG':
                    await this.generatorHandler.handleSaveScriptConfig(message.payload, this.panel);
                    break;

                // ═══ GENERATOR : TABLES & RELATIONS (Étapes 6 & 7) ═══
                case 'GET_TABLES_METADATA':
                    await this.generatorHandler.handleGetTablesMetadata(message.payload, this.panel);
                    break;
                case 'GET_TABLES_METADATA_PARENTS':
                    await this.generatorHandler.handleGetTablesMetadataParents(message.payload, this.panel);
                    break;
                case 'GET_TABLES_METADATA_CHILDS':
                    await this.generatorHandler.handleGetTablesMetadataChilds(message.payload, this.panel);
                    break;
                case 'GET_RELATION_PARAMETERS':
                    await this.generatorHandler.handleGetRelations(message.payload, this.panel);
                    break;
                case 'SAVE_TABLE_SELECTION':
                    await this.generatorHandler.handleSaveTableSelection(message.payload, this.panel);
                    break;
                case 'SAVE_RELATION_PARAMETERS':
                    await this.generatorHandler.handleSaveRelationParameters(message.payload, this.panel);
                    break;

                // ═══ FRONTEND (Étapes 8 & 9) ═══
                case 'GET_FRONTEND_FRAMEWORKS':
                    await this.frontendHandler.handleGetFrontendFrameworks(message.payload, this.panel);
                    break;
                case 'GET_FRONTEND_PROGRAMMING_LANGUAGES': // ✅ NOUVEAU
                    await this.frontendHandler.handleGetFrontendProgrammingLanguages(message.payload, this.panel);
                    break;
                case 'GET_INTERFACE_LANGUAGES': // ✅ RENOMMÉ (était GET_AVAILABLE_LANGUAGES)
                    await this.frontendHandler.handleGetInterfaceLanguages(message.payload, this.panel);
                    break;
                case 'GET_NAVBAR_TYPES': // ✅ NOUVEAU
                    await this.frontendHandler.handleGetNavbarTypes(message.payload, this.panel);
                    break;
                case 'SELECT_FRONTEND_FRAMEWORK': // ✅ NOUVEAU
                    await this.generatorHandler.handleSelectFrontendFramework(message.payload, this.panel);
                    break;
                case 'SAVE_FRONTEND_LAYOUT_CONFIG': // ✅ NOUVEAU
                    await this.generatorHandler.handleSaveFrontendLayoutConfig(message.payload, this.panel);
                    break;

                // ═══ GIT & GÉNÉRATION FINALE (Étapes 10 & 11) ═══
                case 'SAVE_GIT_CONFIGURATION': // ✅ NOUVEAU
                    await this.generatorHandler.handleSaveGitConfiguration(message.payload, this.panel);
                    break;
                case 'LAUNCH_GENERATION': // ✅ NOUVEAU
                    await this.generatorHandler.handleLaunchGeneration(message.payload, this.panel);
                    break;

                // ═══ UTILITAIRES UI ═══
                case 'REQUEST_FOLDER_PATH':
                    await this.generatorHandler.handleRequestFolderPath();
                    break;
                case 'REQUEST_FILE_PATH':
                    await this.generatorHandler.handleRequestFilePath(message.payload?.extensions);
                    break;
                case 'SELECT_FRAMEWORK':
                    await this.generatorHandler.handleSelectFramework(message.payload, this.panel);
                    break;

                default:
                    console.warn(`[WebviewMessageRouter] Message non géré : ${message.type}`);
            }
        } catch (err) {
            this.panel.webview.postMessage({
                type: 'API_ERROR', // ✅ Harmonisé avec le reste de l'architecture
                payload: { 
                    command: message.type || 'UNKNOWN',
                    message: (err as Error).message 
                }
            });
        }
    }

    private getThemeConfig(): { theme: string; colorMode: string } {
        const config = vscode.workspace.getConfiguration('genesis');
        return {
            theme: config.get<string>('theme', 'genesis'),
            colorMode: config.get<string>('colorMode', 'auto')
        };
    }
}