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

export interface LanguageDto {
    code: string;   // ex: 'fr', 'en'
    name: string;   // ex: 'Français', 'English'
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

const MOCK_LANGUAGES: LanguageDto[] = [
    { code: 'fr', name: 'Français' },
    { code: 'en', name: 'English' },
    { code: 'es', name: 'Español' },
    { code: 'de', name: 'Deutsch' },
    { code: 'it', name: 'Italiano' },
    { code: 'pt', name: 'Português' },
    { code: 'ar', name: 'العربية' },
    { code: 'zh', name: '中文' }
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


    // NOUVELLE MÉTHODE : Récupération des langues
    async handleGetAvailableLanguages(_payload: any): Promise<void> {
        try {
            // Tentative d'appel à l'API Java (à adapter selon ton endpoint réel)
            const { data } = await getAxiosInstance().get<LanguageDto[]>('/frontend/languages');
            this.panel.webview.postMessage({ 
                type: 'AVAILABLE_LANGUAGES_LOADED', 
                payload: data 
            });
        } catch (error) {
            console.warn('[FrontendHandler] API Languages échouée, utilisation du fallback statique:', (error as Error).message);
            this.panel.webview.postMessage({
                type: 'AVAILABLE_LANGUAGES_LOADED',
                payload: MOCK_LANGUAGES
            });
        }
    }
}