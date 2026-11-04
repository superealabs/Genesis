import * as vscode from 'vscode';
import { GenesisPanel } from '../panels/GenesisPanel';
import { GenesisApiService } from '../services/GenesisApiService';
import { RecentStorageService, RecentResetScope } from '../services/core/recent/RecentStorageService';

export function registerCommands(
    context: vscode.ExtensionContext,
    genesisApi: GenesisApiService
): void {
    const recentService = new RecentStorageService(context);

    context.subscriptions.push(
        vscode.commands.registerCommand('genesis.helloWorld', () => {
            GenesisPanel.show(context, genesisApi);
        }),

        vscode.commands.registerCommand('genesis.resetRecent', async () => {
            const scope = vscode.workspace
                .getConfiguration('genesis')
                .get<RecentResetScope>('recentResetScope', 'all');

            await recentService.resetByScope(scope);

            vscode.window.showInformationMessage(
                scope === 'all'
                    ? 'Genesis : tous les récents ont été réinitialisés.'
                    : `Genesis : les récents "${scope}" ont été réinitialisés.`
            );

            // Rafraîchir l'UI de la webview si elle est ouverte
            GenesisPanel.notifyRecentReset(scope);
        })
    );
}