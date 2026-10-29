import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeFrontendService } from './VsCodeFrontendService';
import type { FrontendFramework } from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Frontend Handler';

export class FrontendHandler {
    private service = new VsCodeFrontendService();

    constructor(private panel: vscode.WebviewPanel) {}

    // ═══ ROUTAGE VERS LE SERVICE (Lecture) ═══

    async handleGetFrontendFrameworks(payload: any, panel: vscode.WebviewPanel): Promise<void> {
        // Extraction de l'ID du framework depuis le payload (avec fallback à 0 par sécurité)
        const frameworkID = payload?.frameworkID ?? 0;
        const data = await this.service.fetchFrontendFrameworks(frameworkID);
        panel.webview.postMessage({ type: 'FRONTEND_FRAMEWORKS_LOADED', payload: data });
    }

    //  NOUVEAU
    async handleGetFrontendProgrammingLanguages(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchFrontendProgrammingLanguages();
        panel.webview.postMessage({ type: 'FRONTEND_PROGRAMMING_LANGUAGES_LOADED', payload: data });
    }

    //  RENOMMÉ (était handleGetAvailableLanguages)
    async handleGetInterfaceLanguages(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchInterfaceLanguages();
        panel.webview.postMessage({ type: 'INTERFACE_LANGUAGES_LOADED', payload: data });
    }

    //  NOUVEAU
    async handleGetNavbarTypes(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchNavbarTypes();
        panel.webview.postMessage({ type: 'NAVBAR_TYPES_LOADED', payload: data });
    }


}