import type { Framework, Language, CoreFramework, ViewTemplate } from './index'; // Tes types partagés

// Interface PURE. Aucune dépendance à Vue ou Node.
export interface IFrameworkService {
    fetchFrameworks(): Promise<Framework[]>;
    fetchLanguages(): Promise<Language[]>;
    fetchCoreFrameworks(): Promise<CoreFramework[]>;
    fetchViewTemplates(): Promise<ViewTemplate[]>;
}