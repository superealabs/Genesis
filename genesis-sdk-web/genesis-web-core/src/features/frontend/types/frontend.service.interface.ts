import type { InjectionKey } from 'vue';
import type { IFrontendService } from '@genesis-labs/shared-types';

// On réexporte le type pour la commodité locale des composants Vue
export type { IFrontendService };

// ═══ Clé d'injection typée (Obligatoire pour provide/inject dans Vue) ═══
export const FRONTEND_SERVICE_KEY: InjectionKey<IFrontendService> = Symbol('FrontendService');