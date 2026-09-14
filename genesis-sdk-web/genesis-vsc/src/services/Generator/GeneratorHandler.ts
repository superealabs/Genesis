import * as vscode from 'vscode';
import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { DatabaseConfig } from '@genesis-labs/shared-types';

// 1. Import des types directement depuis le Core (Single Source of Truth)
import type { 
    TableMetadataDto, 
    RelationParameter 
} from '@genesis-labs/shared-types';

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

const MOCK_RELATIONS: RelationParameter[] = [
    { parentTable: 'Utilisateur', childTable: 'Commande', mandatory: true, hasForm: true },
    { parentTable: 'Categorie', childTable: 'Produit', mandatory: true, hasForm: false },
    { parentTable: 'Commande', childTable: 'DetailCommande', mandatory: true, hasForm: true },
];

const MOCK_TABLES: TableMetadataDto[] = [
    { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
    { tableName: 'produit', className: 'Produit', isView: false },
    { tableName: 'categorie', className: 'Categorie', isView: false },
    { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
    { tableName: 'commande', className: 'Commande', isView: false },
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
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/tables_metadata_all');
            panel.webview.postMessage({ type: 'TABLES_METADATA_LOADED', payload: data });
        } catch (error) {
            console.warn('[GeneratorHandler] API Tables échouée, utilisation du fallback:', (error as Error).message);
            panel.webview.postMessage({
                type: 'TABLES_METADATA_LOADED',
                payload: MOCK_TABLES
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

    async handleTestDatabaseConnection(payload: DatabaseConfig, panel: vscode.WebviewPanel): Promise<void> {
        try {
            // 🔄 SIMULATION : Remplace ceci par ton vrai appel API ou logique Node.js (ex: mysql2, pg, etc.)
            // const { data } = await getAxiosInstance().post('/test-db-connection', payload);
            
            // Simulation d'un délai réseau pour le réalisme UX
            await new Promise(resolve => setTimeout(resolve, 800));

            // Validation basique pour la démo (à adapter selon ta logique réelle)
            if (!payload.host || !payload.databaseName) {
                throw new Error("L'hôte et le nom de la base de données sont requis.");
            }

            // Succès simulé
            panel.webview.postMessage({
                type: 'DATABASE_CONNECTION_TESTED',
                payload: { 
                    success: true, 
                    message: `Connexion réussie à ${payload.engine} sur ${payload.host}:${payload.port} !` 
                }
            });

        } catch (error) {
            console.warn('[GeneratorHandler] Test de connexion échoué:', (error as Error).message);
            panel.webview.postMessage({
                type: 'DATABASE_CONNECTION_TESTED',
                payload: { 
                    success: false, 
                    message: (error as Error).message || 'Échec de la connexion à la base de données.' 
                }
            });
        }
    }
}