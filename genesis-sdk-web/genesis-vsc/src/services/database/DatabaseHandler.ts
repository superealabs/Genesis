import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeDatabaseService } from './VsCodeDatabaseService';
import type { DatabaseConfig } from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Database Handler';

export class DatabaseHandler {
    // ✅ Instanciation du service qui contient la logique et les mocks
    private service = new VsCodeDatabaseService();

    constructor(private panel: vscode.WebviewPanel) {}

    /**
     * Récupère la liste des moteurs de base de données disponibles.
     * Pas de try/catch nécessaire ici : le service garantit un retour (réel ou mock).
     */
    async handleGetAvailableEngines(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getEngines] Récupération des moteurs de base de données...');
        
        // Le service gère le fallback en interne
        const data = await this.service.fetchDatabaseEngines();
        
        logger.log(LOG_CHANNEL, `✅ [getEngines] Succès. ${data.length} moteurs reçus.`);
        panel.webview.postMessage({
            type: 'DATABASE_ENGINES_LOADED',
            payload: data
        });
    }

    /**
     * Teste la connexion à la base de données.
     * Try/catch nécessaire ici pour transformer une erreur réseau en message UI lisible.
     */
    async handleTestDatabaseConnection(payload: DatabaseConfig, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [testConnection] Test de connexion pour: ${payload.engine} (${payload.host})`);
        
        try {
            // Délégation pure au service
            const result = await this.service.testDatabaseConnection(payload);
            
            // On renvoie le résultat à la webview (qu'il soit success: true ou success: false)
            panel.webview.postMessage({
                type: 'DATABASE_CONNECTION_TESTED',
                payload: result
            });

        } catch (error) {
            // Si le service throw (ex: erreur 500, réseau coupé), on l'attrape ici pour l'UI
            logger.log(LOG_CHANNEL, `❌ [testConnection] Erreur réseau/API: ${(error as Error).message}`);
            
            panel.webview.postMessage({
                type: 'API_ERROR',
                payload: { 
                    command: 'TEST_DATABASE_CONNECTION', 
                    message: `Échec du test de connexion: ${(error as Error).message}` 
                }
            });
        }
    }

}