import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IFrontendService, 
    FrontendFramework, 
    LanguageDto 
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Frontend Service';

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
export const MOCK_FRONTEND_FRAMEWORKS: FrontendFramework[] = [
    { id: 1, languageId: 2, name: 'React', coreFramework: 'React', componentExtension: '.tsx', defaultPort: '3000' },
    { id: 2, languageId: 2, name: 'Vue.js', coreFramework: 'Vue', componentExtension: '.vue', defaultPort: '5173' },
    { id: 3, languageId: 2, name: 'Angular', coreFramework: 'Angular', componentExtension: '.ts', defaultPort: '4200' },
    { id: 4, languageId: 2, name: 'Svelte', coreFramework: 'Svelte', componentExtension: '.svelte', defaultPort: '5173' },
    { id: 5, languageId: 3, name: 'Razor', coreFramework: 'Laravel', componentExtension: '.cshtml', defaultPort: '1456' }
];

export const MOCK_LANGUAGES: LanguageDto[] = [
    { id: 1, code: 'fr', name: 'Français' },
    { id: 2, code: 'en', name: 'English' },
    { id: 3, code: 'es', name: 'Español' },
    { id: 4, code: 'de', name: 'Deutsch' },
    { id: 5, code: 'it', name: 'Italiano' },
    { id: 6, code: 'pt', name: 'Português' },
    { id: 7, code: 'ar', name: 'العربية' },
    { id: 8, code: 'zh', name: '中文' }
];

export class VsCodeFrontendService implements IFrontendService {
    
    async fetchFrontendFrameworks(): Promise<FrontendFramework[]> {
        try {
            const { data } = await getAxiosInstance().get<FrontendFramework[]>('/api/frontend_frameworks');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrontendFrameworks API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_FRONTEND_FRAMEWORKS;
        }
    }

    async fetchAvailableLanguages(): Promise<LanguageDto[]> {
        try {
            const { data } = await getAxiosInstance().get<LanguageDto[]>('/api/frontend/languages');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchAvailableLanguages API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_LANGUAGES;
        }
    }

    // Action d'écriture : on laisse remonter l'erreur si le réseau/backend échoue
    async selectFrontendFramework(framework: FrontendFramework): Promise<void> {
        await getAxiosInstance().post('/api/frontend/select', framework);
    }
}