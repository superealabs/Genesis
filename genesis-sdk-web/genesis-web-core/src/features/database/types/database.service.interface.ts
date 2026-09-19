import type { InjectionKey } from 'vue';
import type { IDatabaseService } from '@genesis-labs/shared-types';

// On réexporte le type pour la commodité locale des composants Vue
export type { IDatabaseService };

// ═══ Clé d'injection typée (Obligatoire pour provide/inject dans Vue) ═══
export const DATABASE_SERVICE_KEY: InjectionKey<IDatabaseService> = Symbol('DatabaseService');