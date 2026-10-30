import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeGeneratorService } from './VsCodeGeneratorService';
import type { 
    ProjectConfig,
    DatabaseConfig,
    ScriptConfig,
    GenerationParameter,
    AiPromptPayload,
    FrontendLayoutConfig,
    GitConfiguration,
    GeneratorData
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Generator Handler';

export class GeneratorHandler {
    private service = new VsCodeGeneratorService();

    constructor(private panel: vscode.WebviewPanel) {}

    async handleSelectGenerationMode(
        payload: { modes?: string },
        panel: vscode.WebviewPanel
    ): Promise<void> {
        try {
            await this.service.selectGenerationMode(payload?.modes);
            panel.webview.postMessage({
                type: 'SELECT_GENERATION_MODE_DONE',
                payload: { success: true }
            });
        } catch (error) {
            this.sendApiError(panel, 'SELECT_GENERATION_MODE', error);
        }
    }

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

    async handleSaveGenerationParameters(payload: { params: GenerationParameter[] }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.saveGenerationParameters(payload.params);
            panel.webview.postMessage({ type: 'GENERATION_PARAMETERS_SAVED', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'SAVE_GENERATION_PARAMETERS', error);
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

// src/services/Generator/GeneratorHandler.ts

    async handleLaunchGeneration(payload: GeneratorData, panel: vscode.WebviewPanel): Promise<void> {
        try {
            logger.log(LOG_CHANNEL, `🚀 [launchGeneration] Lancement de la génération pour: ${payload.config.projectName}`);
            
            // 1. Appel au service pour la génération réelle
            const result = await this.service.launchGeneration(payload);
            
            // 2. ✅ CORRECTION : Notifier la webview IMMÉDIATEMENT que la génération est finie.
            // Cela permet au frontend de résoudre sa Promise et d'arrêter l'écran de chargement.
            panel.webview.postMessage({ type: 'GENERATION_RESULT', payload: result });
                        
        } catch (error) {
            this.sendApiError(panel, 'LAUNCH_GENERATION', error);
        }
    }


    // Utilitaire
    // ═══ RESET ═══

    async handleResetFrontend(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.resetFrontend();
            panel.webview.postMessage({ type: 'FRONTEND_CONFIG_RESET', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'RESET_FRONTEND', error);
        }
    }

    async handleResetGit(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.resetGit();
            panel.webview.postMessage({ type: 'GIT_CONFIG_RESET', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'RESET_GIT', error);
        }
    }

    async handleResetAll(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const result = await this.service.resetAll();
            panel.webview.postMessage({ type: 'GENERATOR_RESET', payload: result });
        } catch (error) {
            this.sendApiError(panel, 'RESET_ALL', error);
        }
    }

    async handleOpenProject(payload: { projectPath: string }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const action = await vscode.window.showInformationMessage(
                `Le projet a été généré avec succès dans :\n${payload.projectPath}`,
                { modal: true },
                'Ouvrir dans cette fenêtre',
                'Ouvrir dans une nouvelle fenêtre'
            );

            if (action === 'Ouvrir dans cette fenêtre') {
                const uri = vscode.Uri.file(payload.projectPath);
                await vscode.commands.executeCommand('vscode.openFolder', uri, { forceReuseWindow: true });
            } 
            else if (action === 'Ouvrir dans une nouvelle fenêtre') {
                const uri = vscode.Uri.file(payload.projectPath);
                await vscode.commands.executeCommand('vscode.openFolder', uri, { forceNewWindow: true });
            }
            // Si 'action' est undefined, l'utilisateur a cliqué sur Annuler ou fermé la popup : on ne fait rien.
        } catch (error) {
            this.sendApiError(panel, 'OPEN_PROJECT', error);
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