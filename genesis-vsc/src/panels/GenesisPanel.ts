import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { GenesisApiService } from '../services/GenesisApiService';
import { FrameworkHandler } from '../services/Framework/FrameworkHandler';

export class GenesisPanel {
    private static instance: GenesisPanel | undefined;
    private panel: vscode.WebviewPanel;

    private constructor(
        panel: vscode.WebviewPanel,
        private readonly context: vscode.ExtensionContext,
        private readonly genesisApi: GenesisApiService
    ) {
        this.panel = panel;
        this.loadHtml();
        this.registerMessageHandler();
        this.panel.onDidDispose(() => {
            GenesisPanel.instance = undefined;
        });
        this.watchConfiguration();
    }

    static show(context: vscode.ExtensionContext, genesisApi: GenesisApiService): void {
        if (GenesisPanel.instance) {
            GenesisPanel.instance.panel.reveal();
            return;
        }

        const panel = vscode.window.createWebviewPanel(
            'genesisWebview',
            'Genesis',
            vscode.ViewColumn.Active,
            {
                enableScripts: true,
                localResourceRoots: [vscode.Uri.joinPath(context.extensionUri, 'dist', 'webview')],
                retainContextWhenHidden: true
            }
        );

        GenesisPanel.instance = new GenesisPanel(panel, context, genesisApi);
    }

    private loadHtml(): void {
        const webviewDist = vscode.Uri.joinPath(this.context.extensionUri, 'dist', 'webview');
        const htmlPath = path.join(webviewDist.fsPath, 'index.html');
        let html = fs.readFileSync(htmlPath, 'utf8');
        const baseUri = this.panel.webview.asWebviewUri(webviewDist);
        html = html.replace(/\.\/assets\//g, `${baseUri}/assets/`);
        this.panel.webview.html = html;
    }

    private getThemeConfig(): { theme: string; colorMode: string } {
        const config = vscode.workspace.getConfiguration('genesis');
        return {
            theme: config.get<string>('theme', 'genesis'),
            colorMode: config.get<string>('colorMode', 'auto')
        };
    }

    private watchConfiguration(): void {
        vscode.workspace.onDidChangeConfiguration((event) => {
            if (event.affectsConfiguration('genesis.theme') || event.affectsConfiguration('genesis.colorMode')) {
                this.panel.webview.postMessage({ type: 'themeChanged', payload: this.getThemeConfig() });
            }
        });
    }

    private registerMessageHandler(): void {
        this.panel.webview.onDidReceiveMessage(async (message) => {
            
            // ── 1. Messages Système ───────────────────────────────────────
            if (message.type === 'ready') {
                const status = this.genesisApi.getStatus();

                if (status.error) {
                    this.panel.webview.postMessage({ type: 'apiError', payload: { message: status.error } });
                    return;
                }

                const sendInit = () => {
                    this.panel.webview.postMessage({
                        type: 'init',
                        payload: { port: this.genesisApi.getPort(), theme: this.getThemeConfig() }
                    });
                };

                if (!status.ready) {
                    this.waitForApi().then(sendInit).catch((err) => {
                        this.panel.webview.postMessage({ type: 'apiError', payload: { message: err.message } });
                    });
                    return;
                }
                
                sendInit();
                return;
            }

            if (message.type === 'browseFolder') {
                const folders = await vscode.window.showOpenDialog({
                    canSelectFolders: true, canSelectFiles: false, canSelectMany: false, openLabel: 'Sélectionner un dossier'
                });
                if (folders && folders.length > 0) {
                    this.panel.webview.postMessage({ type: 'folderSelected', payload: folders[0].fsPath });
                }
                return;
            }

            // ── 2. Messages Métier ────────────────────────────────────────
            const status = this.genesisApi.getStatus();
            if (!status.ready) {
                this.panel.webview.postMessage({ type: 'API_NOT_READY', payload: { command: message.type } });
                return;
            }

            try {
                switch (message.type) {
                    // Feature-based (Nouveau)
                    case 'GET_FRAMEWORKS':
                        await new FrameworkHandler().getAll(message.payload, this.panel);
                        break;
                    case 'SELECT_FRAMEWORK':
                        await new FrameworkHandler().select(message.payload, this.panel);
                        break;
                    
                    // Action ponctuelle (Existant, conservé tel quel)
                    case 'generateJavaFile':
                        const { className, destinationPath } = message.payload;
                        const result = await this.genesisApi.generateJavaFile(className, destinationPath);
                        this.panel.webview.postMessage({ type: 'generateResult', payload: result });
                        break;

                    default:
                        console.warn(`[GenesisPanel] Message non géré : ${message.type}`);
                }
            } catch (err) {
                this.panel.webview.postMessage({
                    type: 'apiError',
                    payload: { message: (err as Error).message }
                });
            }
        });
    }

    private waitForApi(): Promise<void> {
        return new Promise((resolve, reject) => {
            let attempts = 0;
            const interval = setInterval(() => {
                const status = this.genesisApi.getStatus();
                if (status.ready) {
                    clearInterval(interval);
                    resolve();
                } else if (status.error) {
                    clearInterval(interval);
                    reject(new Error(status.error));
                } else if (attempts >= 30) {
                    clearInterval(interval);
                    reject(new Error('Timeout : Genesis API trop longue à démarrer'));
                }
                attempts++;
            }, 500);
        });
    }
}