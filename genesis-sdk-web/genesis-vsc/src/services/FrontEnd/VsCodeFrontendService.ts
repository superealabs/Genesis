import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IFrontendService, 
    FrontendFramework, 
    InterfaceLanguage,
    FrontendProgrammingLanguage //  NOUVEAU IMPORT
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Frontend Service';

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
export const MOCK_FRONTEND_FRAMEWORKS: FrontendFramework[] = [
    { id: 1, languageId: 2, name: 'React', coreFramework: 'React', componentExtension: '.tsx', defaultPort: '3000' },
    { id: 2, languageId: 2, name: 'Vue.js', coreFramework: 'Vue', componentExtension: '.vue', defaultPort: '5173' },
    { id: 3, languageId: 2, name: 'Angular', coreFramework: 'Angular', componentExtension: '.ts', defaultPort: '4200' },
    { id: 4, languageId: 2, name: 'Svelte', coreFramework: 'Svelte', componentExtension: '.svelte', defaultPort: '5173' },
    { id: 5, languageId: 3, name: 'Razor', coreFramework: '.NET MVC', viewTemplateEngine: 'Razor', componentExtension: '.cshtml', defaultPort: '1456' }
];

export const MOCK_LANGUAGES: InterfaceLanguage[] = [
    { id: 1, code: 'fr', name: 'Français' },
    { id: 2, code: 'en', name: 'English' },
    { id: 3, code: 'es', name: 'Español' }
];

//  NOUVEAU : Mock pour les langages de programmation (JS/TS)
export const MOCK_PROGRAMMING_LANGUAGES: FrontendProgrammingLanguage[] = [
    { id: 1, name: 'JavaScript', extension: '.js' },
    { id: 2, name: 'TypeScript', extension: '.ts' }
];

//  NOUVEAU : Mock pour les types de Navbar
export const MOCK_NAVBAR_TYPES: string[] = ['side', 'top'];

export class VsCodeFrontendService implements IFrontendService {
    
    async fetchFrontendFrameworks(): Promise<FrontendFramework[]> {
        try {
            const { data } = await getAxiosInstance().get<FrontendFramework[]>('/api/frontend/frameworks');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrontendFrameworks API échouée. Fallback mock.`);
            return MOCK_FRONTEND_FRAMEWORKS;
        }
    }

    //  NOUVEAU : Récupération des langages de programmation (JS/TS)
    async fetchFrontendProgrammingLanguages(): Promise<FrontendProgrammingLanguage[]> {
        try {
            const { data } = await getAxiosInstance().get<FrontendProgrammingLanguage[]>('/api/frontend/programming-languages');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrontendProgrammingLanguages API échouée. Fallback mock.`);
            return MOCK_PROGRAMMING_LANGUAGES;
        }
    }

    //  RENOMMÉ : fetchAvailableLanguages -> fetchInterfaceLanguages pour coller au contrat
    async fetchInterfaceLanguages(): Promise<InterfaceLanguage[]> {
        try {
            const { data } = await getAxiosInstance().get<InterfaceLanguage[]>('/api/frontend/interface-languages');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchInterfaceLanguages API échouée. Fallback mock.`);
            return MOCK_LANGUAGES;
        }
    }

    //  NOUVEAU : Récupération des types de Navbar
    async fetchNavbarTypes(): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>('/api/frontend/navbar-types');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchNavbarTypes API échouée. Fallback mock.`);
            return MOCK_NAVBAR_TYPES;
        }
    }

    //  ACTION : Gestion d'erreur avec THROW explicite

}