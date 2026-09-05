import * as vscode from 'vscode';
import { GenesisApiService } from './services/GenesisApiService';
import { registerCommands } from './commands/registerCommands';
import { logger } from './services/LoggerService';

// On crée l'instance, mais on ne la démarre PAS encore
const genesisApi = new GenesisApiService();

export async function activate(context: vscode.ExtensionContext) {
    // Enregistrer les commandes immédiatement
    registerCommands(context, genesisApi);
}

export function deactivate() {
    // Sécurité : on coupe tout si l'extension est désactivée brutalement
    logger.dispose();
    genesisApi.stop();
}