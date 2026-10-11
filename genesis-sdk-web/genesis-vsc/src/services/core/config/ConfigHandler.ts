import * as vscode from 'vscode';
import { ConfigStorageService } from './ConfigStorageService';
import type { ConfigType, GenesisConfig } from '@genesis-labs/shared-types';

export class ConfigHandler {
    private configService: ConfigStorageService;

    constructor(
        private panel: vscode.WebviewPanel,
        private context: vscode.ExtensionContext
    ) {
        this.configService = new ConfigStorageService(context);
    }

    async handleGetAll(payload: { configType?: ConfigType }) {
        try {
            const configs = await this.configService.getAll(payload?.configType);
            this.panel.webview.postMessage({ 
                type: 'CONFIG_GET_ALL_SUCCESS', 
                payload: configs 
            });
        } catch (error) {
            this.handleError('CONFIG_GET_ALL', error);
        }
    }

    async handleSave(payload: GenesisConfig) {
        try {
            await this.configService.save(payload);
            this.panel.webview.postMessage({ 
                type: 'CONFIG_SAVE_SUCCESS', 
                payload: { success: true, id: payload.id } 
            });
        } catch (error) {
            this.handleError('CONFIG_SAVE', error);
        }
    }

    async handleDelete(payload: string) {
        try {
            await this.configService.delete(payload);
            this.panel.webview.postMessage({ 
                type: 'CONFIG_DELETE_SUCCESS', 
                payload: { success: true, id: payload } 
            });
        } catch (error) {
            this.handleError('CONFIG_DELETE', error);
        }
    }

    async handleImport() {
        try {
            const result = await this.configService.import();
            this.panel.webview.postMessage({ 
                type: 'CONFIG_IMPORT_SUCCESS', 
                payload: result 
            });
        } catch (error) {
            this.handleError('CONFIG_IMPORT', error);
        }
    }

    async handleExport(payload: GenesisConfig) {
        try {
            await this.configService.export(payload);
            this.panel.webview.postMessage({ 
                type: 'CONFIG_EXPORT_SUCCESS', 
                payload: { success: true } 
            });
        } catch (error) {
            this.handleError('CONFIG_EXPORT', error);
        }
    }

    async handleExportAll(payload: GenesisConfig[]) {
        try {
            await this.configService.exportAll(payload);
            this.panel.webview.postMessage({ 
                type: 'CONFIG_EXPORT_ALL_SUCCESS', 
                payload: { success: true } 
            });
        } catch (error) {
            this.handleError('CONFIG_EXPORT_ALL', error);
        }
    }

    private handleError(action: string, error: unknown) {
        this.panel.webview.postMessage({
            type: 'API_ERROR',
            payload: { 
                command: action,
                message: (error as Error).message 
            }
        });
    }
}