import * as vscode from 'vscode';
import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService'; // ✅ Import du logger centralisé
import type { Framework } from '../../types/framework.types'; 

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
const MOCK_FRAMEWORKS: Framework[] = [
    { id: 1, languageId: 1, name: 'Spring Boot REST', coreFramework: 'Spring', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 2, languageId: 1, name: 'Spring MVC', coreFramework: 'Spring', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 3, languageId: 2, name: 'Django REST', coreFramework: 'Django', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 4, languageId: 3, name: 'Laravel MVC', coreFramework: 'Laravel', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 5, languageId: 4, name: 'Express REST', coreFramework: 'Express', type: 'REST API', isProd: false, useDB: false, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
];

// ✅ Nom du canal de sortie dédié
const LOG_CHANNEL = 'Genesis Frameworks';

export class FrameworkHandler {
    
    async getAll(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, '➡️ [getAll] Méthode appelée. Tentative de récupération des frameworks...');
        
        try {
            logger.log(LOG_CHANNEL, '🔄 [getAll] Appel API en cours vers /frameworks...');
            const { data } = await getAxiosInstance().get<Framework[]>('/frameworks');
            
            logger.log(LOG_CHANNEL, `✅ [getAll] API réussie. ${data.length} éléments reçus. Envoi à la webview.`);
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: data });
            
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ [getAll] API échouée (${(error as Error).message}). Activation du FALLBACK MOCK !`);
            logger.log(LOG_CHANNEL, `📦 [getAll] Envoi de ${MOCK_FRAMEWORKS.length} frameworks en mock à la webview.`);
            
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: MOCK_FRAMEWORKS });
        }
    }

    async select(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        logger.log(LOG_CHANNEL, `➡️ [select] Méthode appelée pour l'ID: ${payload.id}`);
        
        try {
            const { data } = await getAxiosInstance().post(`/frameworks/${payload.id}/select`);
            logger.log(LOG_CHANNEL, '✅ [select] API réussie. Envoi de la confirmation à la webview.');
            
            panel.webview.postMessage({ type: 'FRAMEWORK_SELECTED', payload: data });
            
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ [select] API échouée. Activation du fallback statique.`);
            const selected = MOCK_FRAMEWORKS.find(f => f.id === payload.id);
            
            if (selected) {
                logger.log(LOG_CHANNEL, `📦 [select] Fallback réussi pour l'ID ${payload.id}.`);
                panel.webview.postMessage({ 
                    type: 'FRAMEWORK_SELECTED', 
                    payload: { success: true, framework: selected } 
                });
            } else {
                logger.log(LOG_CHANNEL, `❌ [select] Fallback échoué : Framework introuvable pour l'ID ${payload.id}.`);
                panel.webview.postMessage({ 
                    type: 'API_ERROR', 
                    payload: { command: 'SELECT_FRAMEWORK', message: 'Framework introuvable' } 
                });
            }
        }
    }
}