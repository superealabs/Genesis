import type { InjectionKey } from 'vue';
import type { IFrameworkService } from '@genesis-labs/shared-types'; // Import de l'interface pure

// On réexporte l'interface pour la commodité locale
export type { IFrameworkService };

// ✅ La clé d'injection reste spécifique à Vue, mais elle référence l'interface pure
export const FRAMEWORK_SERVICE_KEY: InjectionKey<IFrameworkService> = Symbol('FrameworkService');