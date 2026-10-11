import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeGeneratorService } from './VsCodeGeneratorService';
import type { 
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    TableSelectionConfig,
    AiPromptPayload,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Generator Handler';

export class GeneratorHandler {
    private service = new VsCodeGeneratorService();

    constructor(private panel: vscode.WebviewPanel) {}

    // ═══ ACTIONS UI SPÉCIFIQUES À VS CODE ═══

    async handleRequestFolderPath(): Promise<void> {
        const folders = await vscode.window.showOpenDialog({
            canSelectFolders: true, canSelectFiles: false, canSelectMany: false,
            openLabel: 'Sélectionner un dossier'
        });
        if (folders && folders.length > 0) {
            this.panel.webview.postMessage({ type: 'FOLDER_PATH_SELECTED', payload: folders[0].fsPath });
        }
    }

    async handleRequestFilePath(extensions?: string[]): Promise<void> {
        const filters: Record<string, string[]> = extensions?.length
            ? { 'Fichiers compatibles': extensions }
            : { 'Tous les fichiers': ['*'] };

        const files = await vscode.window.showOpenDialog({
            canSelectFolders: false, canSelectFiles: true, canSelectMany: false,
            openLabel: 'Sélectionner un fichier', filters
        });

        if (files && files.length > 0) {
            const filePath = files[0].fsPath;
            const fileContent = await vscode.workspace.fs.readFile(files[0]);
            const content = Buffer.from(fileContent).toString('utf-8');
            this.panel.webview.postMessage({ type: 'FILE_PATH_SELECTED', payload: { path: filePath, content } });
        }   
    }

    // ═══ ROUTAGE VERS LE SERVICE (Lecture - Pas de try/catch, le service gère le fallback) ═══

    async handleGetTablesMetadata(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
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

    async handleGetAvailableLlmModels(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchAvailableLlmModels();
        panel.webview.postMessage({ type: 'AVAILABLE_LLM_MODELS_LOADED', payload: data });
    }

    // ═══ ACTIONS (Écriture/Lancement - Try/catch obligatoire pour gérer les erreurs API) ═══

    async handleSelectFramework(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            await this.service.selectFramework(payload.id);
            panel.webview.postMessage({ type: 'FRAMEWORK_SELECTED', payload: { success: true, id: payload.id } });
        } catch (error) {
            this.sendApiError(panel, 'SELECT_FRAMEWORK', error);
        }
    }

    async handleSaveProjectConfig(payload: { config: ProjectConfig }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveProjectConfig(payload.config);
            panel.webview.postMessage({ type: 'PROJECT_CONFIG_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_PROJECT_CONFIG', error);
        }
    }

    async handleSelectDatabase(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            await this.service.selectDatabase(payload.id);
            panel.webview.postMessage({ type: 'DATABASE_SELECTED', payload: { success: true, id: payload.id } });
        } catch (error) {
            this.sendApiError(panel, 'SELECT_DATABASE', error);
        }
    }

    async handleSaveDatabaseConfig(payload: { config: DatabaseConfig }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveDatabaseConfig(payload.config);
            panel.webview.postMessage({ type: 'DATABASE_CONFIG_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_DATABASE_CONFIG', error);
        }
    }

    async handleGenerateAiScript(payload: AiPromptPayload, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.generateAiScript(payload);
            panel.webview.postMessage({ type: 'AI_SCRIPT_GENERATED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'GENERATE_AI_SCRIPT', error);
        }
    }

    async handleSaveScriptConfig(payload: { script: ScriptConfig }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveScriptConfig(payload.script);
            panel.webview.postMessage({ type: 'SCRIPT_CONFIG_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_SCRIPT_CONFIG', error);
        }
    }

    async handleSaveTableSelection(payload: { config: TableSelectionConfig }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveTableSelection(payload.config);
            panel.webview.postMessage({ type: 'TABLE_SELECTION_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_TABLE_SELECTION', error);
        }
    }

    async handleSaveRelationParameters(payload: { relations: any[] }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveRelationParameters(payload.relations);
            panel.webview.postMessage({ type: 'RELATION_PARAMETERS_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_RELATION_PARAMETERS', error);
        }
    }

    async handleSelectFrontendFramework(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            await this.service.selectFrontendFramework(payload.id);
            panel.webview.postMessage({ type: 'FRONTEND_FRAMEWORK_SELECTED', payload: { success: true, id: payload.id } });
        } catch (error) {
            this.sendApiError(panel, 'SELECT_FRONTEND_FRAMEWORK', error);
        }
    }

    async handleSaveFrontendLayoutConfig(payload: { config: FrontendLayoutConfig }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveFrontendLayoutConfig(payload.config);
            panel.webview.postMessage({ type: 'FRONTEND_LAYOUT_CONFIG_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_FRONTEND_LAYOUT_CONFIG', error);
        }
    }

    async handleSaveGitConfiguration(payload: { config: GitConfiguration }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveGitConfiguration(payload.config);
            panel.webview.postMessage({ type: 'GIT_CONFIGURATION_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_GIT_CONFIGURATION', error);
        }
    }

    async handleLaunchGeneration(payload: GeneratorData, panel: vscode.WebviewPanel): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🚀 [launchGeneration] Lancement de la génération pour: ${payload.config.projectName}`);
            const result = await this.service.launchGeneration(payload);
            panel.webview.postMessage({ type: 'GENERATION_RESULT', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'LAUNCH_GENERATION', error);
        }
    }

    // ═══ UTILITAIRE ═══
    private sendApiError(panel: vscode.WebviewPanel, command: string, error: unknown) {
        const errorMsg = (error as Error).message || 'Erreur inconnue';
        logger.log(LOG_CHANNEL, `❌ [${command}] Échec critique: ${errorMsg}`);
        panel.webview.postMessage({
            type: 'API_ERROR',
            payload: { command, message: `Échec de l'opération: ${errorMsg}` }
        });
    }
}