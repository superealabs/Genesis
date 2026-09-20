import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeGeneratorService } from './VsCodeGeneratorService';

import { ProjectConfig } from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Generator Handler';

export class GeneratorHandler {
    private service = new VsCodeGeneratorService();

    constructor(private panel: vscode.WebviewPanel) {}

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

    // ═══ ROUTAGE VERS LE SERVICE (Lecture) ═══

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

    // ✅ CORRECTION : Reçoit et transmet frameworkId
    async handleGetLoggingLevels(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchLoggingLevels(payload.frameworkId);
        panel.webview.postMessage({ type: 'LOGGING_LEVELS_LOADED', payload: data });
    }

    async handleGetSecurityTypes(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchSecurityTypes(payload.frameworkId);
        panel.webview.postMessage({ type: 'SECURITY_TYPES_LOADED', payload: data });
    }

    // AJOUT : Pour compléter le trio
    async handleGetCacheProviders(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchCacheProviders(payload.frameworkId);
        panel.webview.postMessage({ type: 'CACHE_PROVIDERS_LOADED', payload: data });
    }

    async handleGetLanguageVersions(payload: { languageId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchLanguageVersions(payload.languageId);
        panel.webview.postMessage({ type: 'LANGUAGE_VERSIONS_LOADED', payload: data });
    }

    async handleGetFrameworkVersions(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchFrameworkVersions(payload.frameworkId);
        panel.webview.postMessage({ type: 'FRAMEWORK_VERSIONS_LOADED', payload: data });
    }

    async handleGetBuildTools(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [getBuildTools] Récupération pour le framework ID: ${payload.frameworkId}`);
        const data = await this.service.fetchBuildTools(payload.frameworkId);
        panel.webview.postMessage({ type: 'BUILD_TOOLS_LOADED', payload: data });
    }

    async handleGetHibernateDdlAutoOptions(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [getHibernateDdlAuto] Récupération pour le framework ID: ${payload.frameworkId}`);
        const data = await this.service.fetchHibernateDdlAutoOptions(payload.frameworkId);
        panel.webview.postMessage({ type: 'HIBERNATE_DDL_AUTO_LOADED', payload: data });
    }



    async handleSelectFramework(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [selectFramework] Réception de la demande pour l'ID: ${payload.id}`);
        
        try {
            // Si ça échoue, ça va sauter directement au catch
            await this.service.selectFramework(payload.id);
            
            logger.log(LOG_CHANNEL, `✅ [selectFramework] Succès.`);
            panel.webview.postMessage({ 
                type: 'FRAMEWORK_SELECTED', 
                payload: { success: true, id: payload.id } 
            });
            
        } catch (error) {
            // ⚠️ CRUCIAL : On intercepte l'erreur et on prévient la Webview
            const errorMsg = (error as Error).message || 'Erreur inconnue';
            logger.log(LOG_CHANNEL, `❌ [selectFramework] Échec critique: ${errorMsg}`);
            
            panel.webview.postMessage({
                type: 'API_ERROR',
                payload: { 
                    command: 'SELECT_FRAMEWORK', 
                    message: `Échec de la communication avec l'API: ${errorMsg}` 
                }
            });
        }
    }

    async handleSaveProjectConfig(payload: { config: ProjectConfig }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [saveProjectConfig] Réception de la demande de sauvegarde`);
        
        try {
            const result = await this.service.saveProjectConfig(payload.config);
            
            if (result.success) {
                logger.log(LOG_CHANNEL, `✅ [saveProjectConfig] Succès: ${result.message}`);
            } else {
                logger.log(LOG_CHANNEL, `⚠️ [saveProjectConfig] Succès partiel/Warning: ${result.message}`);
            }

            panel.webview.postMessage({ 
                type: 'PROJECT_CONFIG_SAVED', 
                payload: result 
            });

        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ [saveProjectConfig] Échec critique: ${(error as Error).message}`);
            
            panel.webview.postMessage({
                type: 'API_ERROR',
                payload: { 
                    command: 'SAVE_PROJECT_CONFIG', 
                    message: `Échec de la sauvegarde de la configuration: ${(error as Error).message}` 
                }
            });
        }
    }

    async handleSelectDatabase(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [selectDatabase] Sélection du moteur ID: ${payload.id}`);
        
        try {
            await this.service.selectDatabase(payload.id);
            logger.log(LOG_CHANNEL, `✅ [selectDatabase] Moteur ID ${payload.id} sélectionné avec succès.`);
            
            panel.webview.postMessage({ 
                type: 'DATABASE_SELECTED', 
                payload: { success: true, id: payload.id } 
            });
            
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ [selectDatabase] Échec de la sélection: ${(error as Error).message}`);
            
            panel.webview.postMessage({ 
                type: 'API_ERROR', 
                payload: { 
                    command: 'SELECT_DATABASE', 
                    message: `Échec de la sélection de la base de données (ID: ${payload.id})` 
                } 
            });
        }
    }
}