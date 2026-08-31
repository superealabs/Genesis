import * as vscode from 'vscode';
import { getAxiosInstance } from '../http/genesisAxiosInstance';

// ═══ TYPES ═══
export interface FrontendFrameworkDto {
    id: number;
    languageId: number;
    name: string;
    coreFramework: string;
    componentExtension: string;
    defaultPort: string;
}

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
const MOCK_FRONTEND_FRAMEWORKS: FrontendFrameworkDto[] = [
    {
        id: 1,
        languageId: 2,
        name: 'React',
        coreFramework: 'React',
        componentExtension: '.tsx',
        defaultPort: '3000'
    },
    {
        id: 2,
        languageId: 2,
        name: 'Vue.js',
        coreFramework: 'Vue',
        componentExtension: '.vue',
        defaultPort: '5173'
    },
    {
        id: 3,
        languageId: 2,
        name: 'Angular',
        coreFramework: 'Angular',
        componentExtension: '.ts',
        defaultPort: '4200'
    },
    {
        id: 4,
        languageId: 2,
        name: 'Svelte',
        coreFramework: 'Svelte',
        componentExtension: '.svelte',
        defaultPort: '5173'
    }
];

export class FrontendHandler {
    constructor(private panel: vscode.WebviewPanel) {}

    async handleGetFrontendFrameworks(_payload: any): Promise<void> {
        try {
            const { data } = await getAxiosInstance().get<FrontendFrameworkDto[]>('/frontend_frameworks');
            this.panel.webview.postMessage({ 
                type: 'FRONTEND_FRAMEWORKS_LOADED', 
                payload: data 
            });
        } catch (error) {
            console.warn('[FrontendHandler] API Frontend Frameworks échouée, utilisation du fallback statique:', (error as Error).message);
            this.panel.webview.postMessage({
                type: 'FRONTEND_FRAMEWORKS_LOADED',
                payload: MOCK_FRONTEND_FRAMEWORKS
            });
        }
    }
}