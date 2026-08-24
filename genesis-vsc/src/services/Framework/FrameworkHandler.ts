import * as vscode from 'vscode';
import { getAxiosInstance } from '../http/genesisAxiosInstance';

import type { Framework } from '../../types/framework.types'; 

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
const MOCK_FRAMEWORKS: Framework[] = [
    { id: 1, languageId: 1, name: 'Spring Boot REST', coreFramework: 'Spring', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 2, languageId: 1, name: 'Spring MVC', coreFramework: 'Spring', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 3, languageId: 2, name: 'Django REST', coreFramework: 'Django', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 4, languageId: 3, name: 'Laravel MVC', coreFramework: 'Laravel', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 5, languageId: 4, name: 'Express REST', coreFramework: 'Express', type: 'REST API', isProd: false, useDB: false, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
];

export class FrameworkHandler {
    async getAll(_payload: any, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const { data } = await getAxiosInstance().get<Framework[]>('/frameworks');
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: data });
        } catch (error) {
            console.warn('[FrameworkHandler] API échouée, utilisation du fallback statique:', (error as Error).message);
            panel.webview.postMessage({ type: 'FRAMEWORKS_LOADED', payload: MOCK_FRAMEWORKS });
        }
    }

    async select(payload: { id: number }, panel: vscode.WebviewPanel): Promise<void> {
        try {
            const { data } = await getAxiosInstance().post(`/frameworks/${payload.id}/select`);
            panel.webview.postMessage({ type: 'FRAMEWORK_SELECTED', payload: data });
        } catch (error) {
            console.warn('[FrameworkHandler] Sélection API échouée, utilisation du fallback statique');
            const selected = MOCK_FRAMEWORKS.find(f => f.id === payload.id);
            
            if (selected) {
                panel.webview.postMessage({ 
                    type: 'FRAMEWORK_SELECTED', 
                    payload: { success: true, framework: selected } 
                });
            } else {
                panel.webview.postMessage({ 
                    type: 'API_ERROR', 
                    payload: { command: 'SELECT_FRAMEWORK', message: 'Framework introuvable' } 
                });
            }
        }
    }
}