import * as vscode from 'vscode';
import * as fs from 'fs';
import * as path from 'path';
import { GenesisApiService } from '../services/GenesisApiService';
import { WebviewMessageRouter } from '../services/WebviewMessageRouter';

export class GenesisPanel {
    private static instance: GenesisPanel | undefined;
    private panel: vscode.WebviewPanel;
    private shutdownTimer: NodeJS.Timeout | null = null;
    
    // ✅ Le routeur qui s'occupe de tous les messages métier
    private messageRouter: WebviewMessageRouter;

    private constructor(
        panel: vscode.WebviewPanel,
        private readonly context: vscode.ExtensionContext,
        private readonly genesisApi: GenesisApiService
    ) {
        this.panel = panel;
        
        // 1. Initialisation du routeur de messages
        this.messageRouter = new WebviewMessageRouter(this.panel, this.genesisApi, this.context);
        
        // 2. Chargement de l'interface et du thème
        this.loadHtml();
        this.watchConfiguration();
        
        // 3. Enregistrement des événements de la webview
        this.panel.onDidDispose(() => {
            GenesisPanel.instance = undefined;
            this.scheduleShutdown();
        });

        this.registerMessageHandler();
    }

    static show(context: vscode.ExtensionContext, genesisApi: GenesisApiService): void {
        if (GenesisPanel.instance) {
            GenesisPanel.instance.cancelShutdown();
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

    private scheduleShutdown(): void {
        if (this.shutdownTimer) clearTimeout(this.shutdownTimer);
        console.log('[Genesis] Arrêt de l\'API programmé dans 10 minutes...');
        this.shutdownTimer = setTimeout(() => {
            console.log('[Genesis] Arrêt effectif de l\'API après 10 min d\'inactivité.');
            this.genesisApi.stop();
        }, 10 * 60 * 1000);
    }

    private cancelShutdown(): void {
        if (this.shutdownTimer) {
            clearTimeout(this.shutdownTimer);
            this.shutdownTimer = null;
            console.log('[Genesis] Arrêt de l\'API annulé (réouverture de la webview).');
        }
    }

    private registerMessageHandler(): void {
        this.panel.webview.onDidReceiveMessage(async (message) => {
            
            // ── 1. Message de Cycle de Vie (Géré UNIQUEMENT par le Panel) ──
            if (message.type === 'ready') {
                this.cancelShutdown();

                if (!this.genesisApi.isRunning()) {
                    console.log('[Genesis] Démarrage de l\'API à la demande...');
                    try {
                        await this.genesisApi.start(this.context);
                    } catch (err) {
                        console.error('[Genesis] Échec du démarrage de l\'API, passage en mode dégradé (Mock).', err);
                    }
                }

                this.panel.webview.postMessage({
                    type: 'init',
                    payload: {
                        port: this.genesisApi.getPort(),
                        theme: this.getThemeConfig(),
                        isOffline: !this.genesisApi.getStatus().ready
                    }
                });
                return; // ⚠️ IMPORTANT : on s'arrête ici, on ne passe pas au routeur
            }

            // ── 2. Messages Métier (Délégués au Routeur) ────────────────────
            await this.messageRouter.route(message);
        });
    }
}