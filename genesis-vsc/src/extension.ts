import * as vscode from 'vscode';
import { GenesisApiService } from './services/GenesisApiService';
import { registerCommands } from './commands/registerCommands';

const genesisApi = new GenesisApiService();

export async function activate(context: vscode.ExtensionContext) {

    // Démarrer le JAR en arrière-plan sans bloquer
    genesisApi.start(context)
        .then(() => {
            console.log(`[Genesis] API prête sur le port ${genesisApi.getPort()}`);
        })
        .catch((err) => {
            console.error('[Genesis] Échec du démarrage de l\'API :', err);
        });

    // Enregistrer les commandes immédiatement
    registerCommands(context, genesisApi);
}

export function deactivate() {
    genesisApi.stop();
}