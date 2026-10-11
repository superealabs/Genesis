import type { Framework, Language, CoreFramework, ViewTemplate } from './index'; // Tes types partagés

// Interface PURE. Aucune dépendance à Vue ou Node.
export interface IFrameworkService {
    fetchFrameworks(): Promise<Framework[]>;
    fetchLanguages(): Promise<Language[]>;
    fetchCoreFrameworks(): Promise<CoreFramework[]>;
    fetchViewTemplates(): Promise<ViewTemplate[]>;

    fetchLoggingLevels(frameworkId: number): Promise<string[]>;
    fetchSecurityTypes(frameworkId: number): Promise<string[]>;
    fetchCacheProviders(frameworkId: number): Promise<string[]>;
    fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]>;
    fetchLanguageVersions(languageId: number): Promise<string[]>;
    fetchFrameworkVersions(frameworkId: number): Promise<string[]>;
    fetchBuildTools(frameworkId: number): Promise<string[]>;
}