import * as vscode from 'vscode';
// Import des types partagés (Ajuste le chemin d'import si ton tsconfig utilise un alias différent, ex: '@genesis-labs/shared-types')
import type { 
    DatabaseEngineDto, 
    DatabaseConfig, 
    DatabaseConnectionTestResult 
} from '@genesis-labs/shared-types'; 

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
// Alignées sur la structure de la classe Java org.labs.genesis.connexion.Database
const MOCK_DATABASE_ENGINES: DatabaseEngineDto[] = [
    { 
        id: 1, 
        name: 'PostgreSQL', 
        driver: 'org.postgresql.Driver', 
        driverName: 'PostgreSQL JDBC Driver', 
        port: '5432',
        driverType: 'jdbc'
    },
    { 
        id: 2, 
        name: 'MySQL', 
        driver: 'com.mysql.cj.jdbc.Driver', 
        driverName: 'MySQL Connector/J', 
        port: '3306',
        driverType: 'jdbc'
    },
    { 
        id: 3, 
        name: 'SQL Server', 
        driver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver', 
        driverName: 'Microsoft JDBC Driver for SQL Server', 
        port: '1433',
        driverType: 'jdbc'
    },
    { 
        id: 4, 
        name: 'Oracle', 
        driver: 'oracle.jdbc.OracleDriver', 
        driverName: 'Oracle JDBC Driver', 
        port: '1521',
        driverType: 'jdbc',
        sid: 'ORCL'
    },
    {
        id: 5, 
        name: 'MongoDB', 
        driver: 'MongoDB.driver', 
        driverName: 'Mongo JDBC Driver', 
        port: '1524',
        driverType: 'jdbc',
        sid: 'Mongo'
    },
];

export class DatabaseHandler {
    constructor(private panel: vscode.WebviewPanel) {}

    /**
     * Récupère la liste des moteurs de base de données disponibles
     */
    async handleGetAvailableEngines(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            // 🔄 SIMULATION D'APPEL API (À remplacer par ton vrai endpoint plus tard)
            // const { data } = await getAxiosInstance().get<DatabaseEngineDto[]>('/api/database/engines');
            
            // Petit délai pour simuler un appel réseau réaliste (bon pour l'UX)
            await new Promise(resolve => setTimeout(resolve, 300));

            panel.webview.postMessage({
                type: 'DATABASE_ENGINES_LOADED',
                payload: MOCK_DATABASE_ENGINES
            });

        } catch (error) {
            console.warn('[DatabaseHandler] API Engines échouée, utilisation du fallback:', (error as Error).message);
            panel.webview.postMessage({
                type: 'DATABASE_ENGINES_LOADED',
                payload: MOCK_DATABASE_ENGINES
            });
        }
    }

    /**
     * Teste la connexion à la base de données avec la configuration fournie
     */
    async handleTestDatabaseConnection(payload: DatabaseConfig, panel: vscode.WebviewPanel): Promise<void> {
        try {
            // 🔄 SIMULATION D'APPEL API (À remplacer par ton vrai endpoint plus tard)
            // const { data } = await getAxiosInstance().post<DatabaseConnectionTestResult>('/api/database/test-connection', payload);
            
            // Délai simulé
            await new Promise(resolve => setTimeout(resolve, 800));

            // Validation basique (mimant ce que ferait le backend Java)
            if (!payload.host || !payload.databaseName) {
                throw new Error("L'hôte et le nom de la base de données sont requis.");
            }

            // Succès simulé
            const result: DatabaseConnectionTestResult = {
                success: true,
                message: `Connexion réussie à ${payload.engine} sur ${payload.host}:${payload.port} !`
            };

            panel.webview.postMessage({
                type: 'DATABASE_CONNECTION_TESTED',
                payload: result
            });

        } catch (error) {
            console.warn('[DatabaseHandler] Test de connexion échoué:', (error as Error).message);
            
            const result: DatabaseConnectionTestResult = {
                success: false,
                message: (error as Error).message || 'Échec de la connexion à la base de données.'
            };

            panel.webview.postMessage({
                type: 'DATABASE_CONNECTION_TESTED',
                payload: result
            });
        }
    }
}