// src/extension.ts
import * as vscode from 'vscode';
import { GenesisApiService } from './services/GenesisApiService';
import { registerCommands } from './commands/registerCommands';
import { logger } from './services/LoggerService';

// On crée l'instance, mais on ne la démarre PAS encore
const genesisApi = new GenesisApiService();

export async function activate(context: vscode.ExtensionContext) {
    // Enregistrement immédiat des commandes
    registerCommands(context, genesisApi);
}

/**
 * Appelé par VS Code quand l'extension est désactivée (fermeture VS Code,
 * désinstallation, rechargement de fenêtre…).
 *
 * VS Code attend que la promesse retournée soit résolue avant de terminer.
 * Cela nous permet d'attendre que le JAR soit effectivement tué.
 */
export async function deactivate(): Promise<void> {
    try {
        logger.log('Genesis Generator Service', '🛑 Désactivation de l\'extension, arrêt du JAR...');
        await genesisApi.stop();
    } catch (err) {
        console.error('[Genesis] Erreur lors de l\'arrêt du JAR :', err);
    } finally {
        logger.dispose();
    }
}