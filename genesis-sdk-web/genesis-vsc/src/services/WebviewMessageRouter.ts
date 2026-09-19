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
        // Initialisation des handlers (adapte les constructeurs si nécessaire)
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
                case 'GET_FRAMEWORKS':
                    await this.frameworkHandler.getAll(message.payload, this.panel);
                    break;
                    
                case 'SELECT_FRAMEWORK':
                    await this.frameworkHandler.select(message.payload, this.panel);
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

                case 'GET_LOGGING_LEVELS':
                    await this.generatorHandler.handleGetLoggingLevels(message.payload, this.panel);
                    break;

                case 'REQUEST_FOLDER_PATH': // Nom harmonisé avec le frontend
                    await this.generatorHandler.handleRequestFolderPath();
                    break;

                case 'REQUEST_FILE_PATH':
                    await this.generatorHandler.handleRequestFilePath(message.payload?.extensions);
                    break;

                case 'GET_TABLES_METADATA_PARENTS':
                    await this.generatorHandler.handleGetTablesMetadataParents(message.payload, this.panel);
                    break;

                case 'GET_TABLES_METADATA':
                    await this.generatorHandler.handleGetTablesMetadata(message.payload, this.panel);
                    break;

                case 'GET_TABLES_METADATA_CHILDS':
                    await this.generatorHandler.handleGetTablesMetadataChilds(message.payload, this.panel);
                    break;
                    
                case 'GET_RELATION_PARAMETERS':
                    await this.generatorHandler.handleGetRelations(message.payload, this.panel);
                    break;

                case 'GET_FRONTEND_FRAMEWORKS':
                    await this.frontendHandler.handleGetFrontendFrameworks(message.payload, this.panel);
                    break;

                case 'GET_AVAILABLE_LANGUAGES':
                    await this.frontendHandler.handleGetAvailableLanguages(message.payload, this.panel);
                    break;

                case 'GET_DATABASE_ENGINES':
                    await this.databaseHandler.handleGetAvailableEngines(message.payload, this.panel);
                    break;

                case 'TEST_DATABASE_CONNECTION':
                    await this.databaseHandler.handleTestDatabaseConnection(message.payload, this.panel);
                    break;

                case 'SELECT_DATABASE':
                    await this.databaseHandler.handleSelectDatabase(message.payload, this.panel);
                    break;

                default:
                    console.warn(`[WebviewMessageRouter] Message non géré : ${message.type}`);
            }
        } catch (err) {
            this.panel.webview.postMessage({
                type: 'apiError',
                payload: { message: (err as Error).message }
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