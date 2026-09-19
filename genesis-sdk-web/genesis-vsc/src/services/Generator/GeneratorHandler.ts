import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeGeneratorService } from './VsCodeGeneratorService';

const LOG_CHANNEL = 'Genesis Generator Handler';

export class GeneratorHandler {
    // ✅ Instanciation du service qui contient la logique et les mocks
    private service = new VsCodeGeneratorService();

    constructor(private panel: vscode.WebviewPanel) {}

    // ═══ ACTIONS UI SPÉCIFIQUES À VS CODE (Pas d'API backend) ═══

    async handleRequestFolderPath(): Promise<void> {
        const folders = await vscode.window.showOpenDialog({
            canSelectFolders: true,
            canSelectFiles: false,
            canSelectMany: false,
            openLabel: 'Sélectionner un dossier'
        });

        if (folders && folders.length > 0) {
            this.panel.webview.postMessage({ 
                type: 'FOLDER_PATH_SELECTED', 
                payload: folders[0].fsPath 
            });
        }
    }

    async handleRequestFilePath(extensions?: string[]): Promise<void> {
        const filters: Record<string, string[]> = extensions?.length
            ? { 'Fichiers compatibles': extensions }
            : { 'Tous les fichiers': ['*'] };

        const files = await vscode.window.showOpenDialog({
            canSelectFolders: false,
            canSelectFiles: true,
            canSelectMany: false,
            openLabel: 'Sélectionner un fichier',
            filters
        });

        if (files && files.length > 0) {
            const filePath = files[0].fsPath;
            const fileContent = await vscode.workspace.fs.readFile(files[0]);
            const content = Buffer.from(fileContent).toString('utf-8');

            this.panel.webview.postMessage({
                type: 'FILE_PATH_SELECTED',
                payload: { path: filePath, content }
            });
        }
    }

    // ═══ ROUTAGE VERS LE SERVICE (Lecture) ═══
    // Plus de try/catch ici, le service garantit un retour (réel ou mock).

    async handleGetTablesMetadata(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getTables] Récupération des tables...');
        const data = await this.service.fetchTablesMetadata();
        panel.webview.postMessage({ type: 'TABLES_METADATA_LOADED', payload: data });
    }

    async handleGetTablesMetadataParents(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchTablesMetadataParents();
        panel.webview.postMessage({ type: 'TABLES_METADATA_PARENTS_LOADED', payload: data });
    }

    async handleGetTablesMetadataChilds(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchTablesMetadataChilds();
        panel.webview.postMessage({ type: 'TABLES_METADATA_CHILDS_LOADED', payload: data });
    }

    async handleGetRelations(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchRelations();
        panel.webview.postMessage({ type: 'RELATIONS_LOADED', payload: data });
    }

    async handleGetLoggingLevels(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchLoggingLevels();

        panel.webview.postMessage({
            type: 'LOGGING_LEVELS_LOADED',
            payload: data
        });
    }
}