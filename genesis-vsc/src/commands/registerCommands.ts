import * as vscode from 'vscode';
import { GenesisPanel } from '../panels/GenesisPanel';
import { GenesisApiService } from '../services/GenesisApiService';

export function registerCommands(
    context: vscode.ExtensionContext,
    genesisApi: GenesisApiService
): void {
    context.subscriptions.push(
        vscode.commands.registerCommand('genesis.helloWorld', () => {
            GenesisPanel.show(context, genesisApi);
        })
    );
}