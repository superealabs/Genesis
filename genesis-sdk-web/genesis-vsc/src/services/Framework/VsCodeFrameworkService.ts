import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IFrameworkService, 
    Framework, 
    Language, 
    CoreFramework, 
    ViewTemplate 
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Frameworks Service';

// ═══ DONNÉES STATIQUES (FALLBACK) POUR TOUS LES CONTRATS ═══

const MOCK_FRAMEWORKS: Framework[] = [
    { id: 1, languageId: 1, name: 'Spring Boot REST', coreFramework: 'Spring', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 2, languageId: 1, name: 'Spring MVC', coreFramework: 'Spring', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 3, languageId: 2, name: 'Django REST', coreFramework: 'Django', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
    { id: 4, languageId: 3, name: 'Laravel MVC', coreFramework: 'Laravel', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true },
    { id: 5, languageId: 4, name: 'Express REST', coreFramework: 'Express', type: 'REST API', isProd: false, useDB: false, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false },
];

const MOCK_LANGUAGES: Language[] = [
    { id: 1, name: 'Java', extension: '.java' },
    { id: 2, name: 'Python', extension: '.py' },
    { id: 3, name: 'PHP', extension: '.php' },
    { id: 4, name: 'JavaScript', extension: '.js' },
    { id: 5, name: 'TypeScript', extension: '.ts' }
];

const MOCK_CORE_FRAMEWORKS: CoreFramework[] = [
    { id: 1, name: 'Spring' },
    { id: 2, name: 'Django' },
    { id: 3, name: 'Laravel' },
    { id: 4, name: 'Express' },
    { id: 5, name: 'NestJS' }
];

const MOCK_VIEW_TEMPLATES: ViewTemplate[] = [
    { id: 1, name: 'Thymeleaf' },
    { id: 2, name: 'Jinja2' },
    { id: 3, name: 'Twig' },
    { id: 4, name: 'EJS' },
    { id: 5, name: 'Pug' }
];

// ═══ IMPLÉMENTATION DU CONTRAT AVEC FALLBACK ═══

export class VsCodeFrameworkService implements IFrameworkService {
    
    async fetchFrameworks(): Promise<Framework[]> {
        try {
            const { data } = await getAxiosInstance().get<Framework[]>('/frameworks');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrameworks API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_FRAMEWORKS;
        }
    }

    async selectFramework(id: number): Promise<void> {
        // On ne retourne pas de mock ici car c'est une action d'écriture. 
        // Si l'API échoue, on laisse l'erreur remonter pour que le Handler la gère (comme dans ton code actuel).
        await getAxiosInstance().post(`/frameworks/${id}/select`);
    }

    async fetchLanguages(): Promise<Language[]> {
        try {
            const { data } = await getAxiosInstance().get<Language[]>('/frameworks/languages');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchLanguages API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_LANGUAGES;
        }
    }

    async fetchCoreFrameworks(): Promise<CoreFramework[]> {
        try {
            const { data } = await getAxiosInstance().get<CoreFramework[]>('/frameworks/cores');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchCoreFrameworks API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_CORE_FRAMEWORKS;
        }
    }

    async fetchViewTemplates(): Promise<ViewTemplate[]> {
        try {
            const { data } = await getAxiosInstance().get<ViewTemplate[]>('/frameworks/templates');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchViewTemplates API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_VIEW_TEMPLATES;
        }
    }
}