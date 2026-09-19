import * as vscode from 'vscode';
import { logger } from '../LoggerService';
import { VsCodeFrontendService } from './VsCodeFrontendService';
import type { FrontendFramework } from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Frontend Handler';

export class FrontendHandler {
    // ✅ Instanciation du service qui contient la logique et les mocks
    private service = new VsCodeFrontendService();

    constructor(private panel: vscode.WebviewPanel) {}

    // ═══ ROUTAGE VERS LE SERVICE (Lecture) ═══
    // Plus de try/catch ici, le service garantit un retour (réel ou mock).

    async handleGetFrontendFrameworks(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getFrontends] Récupération des frameworks frontend...');
        const data = await this.service.fetchFrontendFrameworks();
        panel.webview.postMessage({ type: 'FRONTEND_FRAMEWORKS_LOADED', payload: data });
    }

    async handleGetAvailableLanguages(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getLanguages] Récupération des langues disponibles...');
        const data = await this.service.fetchAvailableLanguages();
        panel.webview.postMessage({ type: 'AVAILABLE_LANGUAGES_LOADED', payload: data });
    }

    // ═══ ACTION (Écriture) ═══
    // Try/catch nécessaire ici pour transformer une erreur réseau en message UI lisible.

    async handleSelectFrontendFramework(payload: { framework: FrontendFramework }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [selectFrontend] Sélection du framework: ${payload.framework.name}`);
        
        try {
            await this.service.selectFrontendFramework(payload.framework);
            logger.log(LOG_CHANNEL, `✅ [selectFrontend] Framework sélectionné avec succès.`);
            
            panel.webview.postMessage({ 
                type: 'FRONTEND_FRAMEWORK_SELECTED', 
                payload: { success: true, framework: payload.framework } 
            });
            
        } catch (error) {
            logger.log(LOG_CHANNEL, `❌ [selectFrontend] Échec de la sélection: ${(error as Error).message}`);
            panel.webview.postMessage({ 
                type: 'API_ERROR', 
                payload: { 
                    command: 'SELECT_FRONTEND', 
                    message: `Échec de la sélection du framework frontend` 
                } 
            });
        }
    }
}