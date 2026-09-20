import * as vscode from 'vscode';
import { logger } from '../LoggerService';
// ✅ On importe UNIQUEMENT le service. Plus besoin d'importer les mocks ici.
import { VsCodeFrameworkService } from './VsCodeFrameworkService';

const LOG_CHANNEL = 'Genesis Frameworks';

export class FrameworkHandler {
    // ✅ Instanciation unique du service respectant le contrat
    private service = new VsCodeFrameworkService();

    // ═══ 1. RÉCUPÉRATION DES LISTES (Lecture) ═══
    // Le service gère lui-même le fallback en interne si l'API échoue.

    async getAll(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getAll] Tentative de récupération des frameworks...');
        try {
            const data = await this.service.fetchFrameworks();
            logger.log(LOG_CHANNEL, `✅ [getAll] Succès. ${data.length} éléments reçus.`);
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: data });
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ [getAll] Erreur critique inattendue.`);
            panel.webview.postMessage({ type: 'API_ERROR', payload: { command: 'GET_FRAMEWORKS', message: 'Erreur de chargement' } });
        }
    }

    async getLanguages(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getLanguages] Récupération des langages...');
        const data = await this.service.fetchLanguages();
        panel.webview.postMessage({ type: 'LANGUAGES_LOADED', payload: data });
    }

    async getCoreFrameworks(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getCoreFrameworks] Récupération des core frameworks...');
        const data = await this.service.fetchCoreFrameworks();
        panel.webview.postMessage({ type: 'CORE_FRAMEWORKS_LOADED', payload: data });
    }

    async getViewTemplates(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getViewTemplates] Récupération des moteurs de template...');
        const data = await this.service.fetchViewTemplates();
        panel.webview.postMessage({ type: 'VIEW_TEMPLATES_LOADED', payload: data });
    }

    // ═══ 2. ACTION MÉTIER (Écriture) ═══
}