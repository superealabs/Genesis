import * as vscode from 'vscode';
import { GenesisApiService } from '../services/GenesisApiService';
import { FrameworkHandler } from './handlers/FrameworkHandler';

type RouteHandler = (payload: any, panel: vscode.WebviewPanel) => Promise<void>;

export class WebviewMessageRouter {
    private routes: Map<string, RouteHandler> = new Map();

    constructor(
        private readonly panel: vscode.WebviewPanel,
        private readonly api: GenesisApiService
    ) {
        this.registerRoutes();
    }

    private registerRoutes(): void {
        const framework = new FrameworkHandler(this.api);

        this.routes.set('GET_FRAMEWORKS',  (p, panel) => framework.getAll(p, panel));
        this.routes.set('SELECT_FRAMEWORK', (p, panel) => framework.select(p, panel));
        // Ajouter ici les futurs handlers : language, generator, etc.
    }

    async dispatch(message: { type: string; payload?: any }): Promise<void> {
        const route = this.routes.get(message.type);
        if (!route) return; // messages système gérés par GenesisPanel, pas ici

        if (!this.api.getStatus().ready) {
            this.panel.webview.postMessage({
                type: 'API_NOT_READY',
                payload: { command: message.type }
            });
            return;
        }

        try {
            await route(message.payload, this.panel);
        } catch (err) {
            const error = err instanceof Error ? err.message : 'Erreur inconnue';
            this.panel.webview.postMessage({
                type: 'apiError',
                payload: { message: error }
            });
        }
    }
}