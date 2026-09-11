import type { InjectionKey } from 'vue';
import type { FrontendFramework } from './frontend.types';

export interface IFrontendService {
    fetchFrontendFrameworks(): Promise<FrontendFramework[]>;
    
    selectFrontendFramework(framework: FrontendFramework): Promise<void>;
}

// ✅ Clé typée pour l'injection de dépendance (obligatoire)
export const FRONTEND_SERVICE_KEY: InjectionKey<IFrontendService> = Symbol('FrontendService');