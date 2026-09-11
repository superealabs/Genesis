import type { InjectionKey } from 'vue';
import type { Framework } from './framework.types';

export interface IFrameworkService {
    // Retourne une Promise, ne touche pas au store
    fetchFrameworks(): Promise<Framework[]>;
    selectFramework(id: number): Promise<void>;
} 

// Clé typée pour l'injection de dépendance (obligatoire)
export const FRAMEWORK_SERVICE_KEY: InjectionKey<IFrameworkService> = Symbol('FrameworkService');