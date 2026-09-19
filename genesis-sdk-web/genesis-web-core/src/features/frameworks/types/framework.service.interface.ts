import type { InjectionKey } from 'vue';
import type { Framework, Language, CoreFramework, ViewTemplate } from '@genesis-labs/shared-types';

export interface IFrameworkService {
    // Retourne une Promise, ne touche pas au store
    fetchFrameworks(): Promise<Framework[]>;
    selectFramework(id: number): Promise<void>;


    fetchLanguages(): Promise<Language[]>;
    
    /** Récupère la liste des cœurs de framework (id, name) */
    fetchCoreFrameworks(): Promise<CoreFramework[]>;
    
    /** Récupère la liste des moteurs de template (id, name) */
    fetchViewTemplates(): Promise<ViewTemplate[]>;
} 

// Clé typée pour l'injection de dépendance (obligatoire)
export const FRAMEWORK_SERVICE_KEY: InjectionKey<IFrameworkService> = Symbol('FrameworkService');