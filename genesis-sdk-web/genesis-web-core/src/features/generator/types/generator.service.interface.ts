import type { InjectionKey } from 'vue';
import type { IGeneratorService } from '@genesis-labs/shared-types';

// On réexporte le type pour la commodité locale des composants Vue
export type { IGeneratorService };

// ═══ Clé d'injection typée (Obligatoire pour provide/inject dans Vue) ═══
export const GENERATOR_SERVICE_KEY: InjectionKey<IGeneratorService> = Symbol('GeneratorService');