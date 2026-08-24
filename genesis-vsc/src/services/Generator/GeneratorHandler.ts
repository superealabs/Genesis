import * as vscode from 'vscode';

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

    async handleGenerateJavaFile(payload: any): Promise<void> {
        // Logique de génération à implémenter
        this.panel.webview.postMessage({
            type: 'GENERATE_RESULT',
            payload: { success: true, message: 'Fichier généré avec succès' }
        });
    }
}