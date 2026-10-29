// genesis-sdk-web/genesis-web-core/src/features/repository/types/repository.service.interface.ts

import type { InjectionKey } from 'vue';
import type { IRepositoryService } from '@genesis-labs/shared-types'; // Import de l'interface pure

// On réexporte l'interface pour la commodité locale
export type { IRepositoryService };

// La clé d'injection reste spécifique à Vue, mais elle référence l'interface pure
export const REPOSITORY_SERVICE_KEY: InjectionKey<IRepositoryService> = Symbol('RepositoryService');