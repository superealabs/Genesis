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
    { id: 1, languageId: 1, name: 'Spring Boot REST', coreFramework: 'Spring', type: 'REST API', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: false, withHibernateDdlAuto: true },
    { id: 2, languageId: 1, name: 'Spring MVC', coreFramework: 'Spring', type: 'MVC', isProd: true, useDB: true, useCloud: false, useEurekaServer: false, isGateway: false, useFrontendApp: true, withHibernateDdlAuto: true },
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

const MOCK_LOGGING_LEVELS: string[] = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'];
const MOCK_SECURITY_TYPES: string[] = ['NONE', 'Basic Authentication', 'JWT', 'OAuth 2.0'];
const MOCK_CACHE_PROVIDERS: string[] = ['NONE', 'Redis', 'Ehcache', 'Caffeine'];
const MOCK_LANGUAGE_VERSIONS: string[] = ['11', '17', '21', '18', '20', '22', '3.9', '3.10', '3.11', '3.12', '8.x', '9.x', '10.x'];
const MOCK_FRAMEWORK_VERSIONS: string[] = ['3.2.0', '3.1.5', '3.0.0', '4.2', '4.1', '10.x', '9.x', '4.4', '4.3'];
const MOCK_BUILD_TOOLS: string[] = ['maven', 'gradle', 'npm', 'yarn', 'pip'];
const MOCK_HIBERNATE_DDL_AUTO: string[] = ['none', 'update', 'validate', 'create-drop', 'create'];


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

    async fetchLoggingLevels(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/logging_levels/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchLoggingLevels API échouée. Fallback mock.`);
            return MOCK_LOGGING_LEVELS;
        }
    }

    async fetchSecurityTypes(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/security_types/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchSecurityTypes API échouée. Fallback mock.`);
            return MOCK_SECURITY_TYPES;
        }
    }

    async fetchCacheProviders(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/cache_providers/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchCacheProviders API échouée. Fallback mock.`);
            return MOCK_CACHE_PROVIDERS;
        }
    }

    async fetchLanguageVersions(languageId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/language-versions/${languageId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchLanguageVersions échouée. Fallback mock.`);
            return MOCK_LANGUAGE_VERSIONS;
        }
    }

    async fetchFrameworkVersions(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/framework-versions/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrameworkVersions échouée. Fallback mock.`);
            return MOCK_FRAMEWORK_VERSIONS;
        }
    }

    async fetchBuildTools(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/build-tools/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchBuildTools échouée. Fallback mock.`);
            return MOCK_BUILD_TOOLS;
        }
    }

    async fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/hibernate_ddl_auto_options/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchHibernateDdlAutoOptions API échouée. Fallback mock.`);
            return MOCK_HIBERNATE_DDL_AUTO;
        }
    }
}