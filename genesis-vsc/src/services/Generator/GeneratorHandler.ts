import * as vscode from 'vscode';
import { getAxiosInstance } from '../http/genesisAxiosInstance';

// ═══ TYPES ═══
export interface TableMetadataDto {
    tableName: string;
    className: string;
    isView: boolean;
}

export interface RelationParameter {
    parentTable: string;
    childTable: string;
    mandatory: boolean;
    hasForm: boolean;
}

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
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

// 👇 NOUVELLES DONNÉES STATIQUES POUR LES RELATIONS
const MOCK_RELATIONS: RelationParameter[] = [
    { parentTable: 'Utilisateur', childTable: 'Commande', mandatory: true, hasForm: true },
    { parentTable: 'Categorie', childTable: 'Produit', mandatory: true, hasForm: false },
    { parentTable: 'Commande', childTable: 'DetailCommande', mandatory: true, hasForm: true },
];

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
        try {
            const { data } = await getAxiosInstance().get('/tables_metadata_loaded');
            panel.webview.postMessage({ type: 'TABLES_METADATA_LOADED', payload: data });
        } catch (error) {
            console.warn('[GeneratorHandler] API échouée, utilisation du fallback statique:', (error as Error).message);
            this.panel.webview.postMessage({
                type: 'TABLES_METADATA_LOADED',
                payload: {
                    tables: MOCK_PARENT_TABLES,
                    views: [MOCK_PARENT_TABLES[3], MOCK_CHILD_TABLES[3]]
                }
            });
        }
    }

    async handleGetTablesMetadataParents(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/tables_metadata/parents');
            panel.webview.postMessage({ type: 'TABLES_METADATA_PARENTS_LOADED', payload: data });
        } catch (error) {
            console.warn('[GeneratorHandler] API Parents échouée, utilisation du fallback statique:', (error as Error).message);
            panel.webview.postMessage({
                type: 'TABLES_METADATA_PARENTS_LOADED',
                payload: MOCK_PARENT_TABLES
            });
        }
    }

    async handleGetTablesMetadataChilds(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/tables_metadata/childs');
            panel.webview.postMessage({ type: 'TABLES_METADATA_CHILDS_LOADED', payload: data });
        } catch (error) {
            console.warn('[GeneratorHandler] API Childs échouée, utilisation du fallback statique:', (error as Error).message);
            panel.webview.postMessage({
                type: 'TABLES_METADATA_CHILDS_LOADED',
                payload: MOCK_CHILD_TABLES
            });
        }
    }

    // ═══ NOUVELLE FONCTION POUR LES RELATIONS ═══
    async handleGetRelations(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/relations');
            panel.webview.postMessage({ type: 'RELATIONS_LOADED', payload: data });
        } catch (error) {
            console.warn('[GeneratorHandler] API Relations échouée, utilisation du fallback statique:', (error as Error).message);
            panel.webview.postMessage({
                type: 'RELATIONS_LOADED',
                payload: MOCK_RELATIONS
            });
        }
    }
}