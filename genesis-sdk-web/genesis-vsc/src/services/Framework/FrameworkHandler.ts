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

    async handleGetLoggingLevels(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchLoggingLevels(payload.frameworkId);
        panel.webview.postMessage({ type: 'LOGGING_LEVELS_LOADED', payload: data });
    }

    async handleGetSecurityTypes(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchSecurityTypes(payload.frameworkId);
        panel.webview.postMessage({ type: 'SECURITY_TYPES_LOADED', payload: data });
    }

    async handleGetCacheProviders(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchCacheProviders(payload.frameworkId);
        panel.webview.postMessage({ type: 'CACHE_PROVIDERS_LOADED', payload: data });
    }

    async handleGetLanguageVersions(payload: { languageId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchLanguageVersions(payload.languageId);
        panel.webview.postMessage({ type: 'LANGUAGE_VERSIONS_LOADED', payload: data });
    }

    async handleGetFrameworkVersions(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchFrameworkVersions(payload.frameworkId);
        panel.webview.postMessage({ type: 'FRAMEWORK_VERSIONS_LOADED', payload: data });
    }

    async handleGetBuildTools(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [getBuildTools] Récupération pour le framework ID: ${payload.frameworkId}`);
        const data = await this.service.fetchBuildTools(payload.frameworkId);
        panel.webview.postMessage({ type: 'BUILD_TOOLS_LOADED', payload: data });
    }

    async handleGetHibernateDdlAutoOptions(payload: { frameworkId: number }, panel: vscode.WebviewPanel): Promise<void> {
        const data = await this.service.fetchHibernateDdlAutoOptions(payload.frameworkId);
        panel.webview.postMessage({ type: 'HIBERNATE_DDL_AUTO_LOADED', payload: data });
    }
}