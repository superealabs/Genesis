import * as vscode from 'vscode';

import { getAxiosInstance } from '../http/genesisAxiosInstance';

export class GeneratorHandler {
    constructor(private panel: vscode.WebviewPanel) {}

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

    async handleGetTablesMetadata(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const mockTables = [
            { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
            { tableName: 'produit', className: 'Produit', isView: false },
            { tableName: 'commande', className: 'Commande', isView: false },
            { tableName: 'categorie', className: 'Categorie', isView: false },
            { tableName: 'facture', className: 'Facture', isView: false },
        ];

        const mockViews = [
            { tableName: 'vue_commandes_clients', className: 'VueCommandesClients', isView: true },
            { tableName: 'vue_stock_produits', className: 'VueStockProduits', isView: true },
        ];

        try {
            const { data } = await getAxiosInstance().get('/tables_metadata_loaded');
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: data });
        } catch (error) {
            console.warn('[FrameworkHandler] API échouée, utilisation du fallback statique:', (error as Error).message);
            this.panel.webview.postMessage({
                type: 'TABLES_METADATA_LOADED',
                payload: {
                    tables: mockTables,
                    views: mockViews
                }
            });
        }
    }
}