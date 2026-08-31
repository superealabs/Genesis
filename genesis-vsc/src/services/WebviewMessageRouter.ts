import * as vscode from 'vscode';
import { GenesisApiService } from './GenesisApiService';
import { FrameworkHandler } from './Framework/FrameworkHandler';
import { GeneratorHandler } from './Generator/GeneratorHandler';
import { FrontendHandler } from './FrontEnd/FrontendFrameworkHandler';

export class WebviewMessageRouter {
    private frameworkHandler: FrameworkHandler;
    private generatorHandler: GeneratorHandler;
    private frontendHandler: FrontendHandler;

    constructor(
        private panel: vscode.WebviewPanel,
        private genesisApi: GenesisApiService,
        private context: vscode.ExtensionContext
    ) {
        // Initialisation des handlers (adapte les constructeurs si nécessaire)
        this.frameworkHandler = new FrameworkHandler(); 
        this.generatorHandler = new GeneratorHandler(this.panel);
        this.frontendHandler = new FrontendHandler(this.panel);
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

                case 'REQUEST_FOLDER_PATH': // Nom harmonisé avec le frontend
                    await this.generatorHandler.handleRequestFolderPath();
                    break;

                case 'REQUEST_FILE_PATH':
                    await this.generatorHandler.handleRequestFilePath(message.payload?.extensions);
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

                case 'GET_FRONTEND_FRAMEWORKS':
                    await this.frontendHandler.handleGetFrontendFrameworks(message.payload);
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