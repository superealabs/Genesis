import type { Framework, Language, CoreFramework, ViewTemplate } from './index'; // Tes types partagés

// Interface PURE. Aucune dépendance à Vue ou Node.
export interface IFrameworkService {
    fetchFrameworks(): Promise<Framework[]>;
    selectFramework(id: number): Promise<void>; // Ou Promise<any> selon ta réponse API
    fetchLanguages(): Promise<Language[]>;
    fetchCoreFrameworks(): Promise<CoreFramework[]>;
    fetchViewTemplates(): Promise<ViewTemplate[]>;
}