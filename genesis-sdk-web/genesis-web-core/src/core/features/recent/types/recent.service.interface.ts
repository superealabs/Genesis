// genesis-sdk-web/genesis-web-core/src/core/features/recent/types/recent.service.interface.ts
import type { InjectionKey } from 'vue';
import type { 
    IRecentService, 
    RecencyData, 
    RecentItem, 
    RecencyCategory 
} from '@genesis-labs/shared-types';

// Réexportation pour la commodité des imports dans les autres fichiers du core
export type { IRecentService, RecencyData, RecentItem, RecencyCategory };


/**
 * Nombre maximum d'éléments conservés dans la liste de récence.
 * Au-delà, les éléments les plus anciens sont automatiquement éjectés.
 */
export const MAX_RECENT_ITEMS = 5;

/**
 * Clé d'injection typée pour Vue (provide / inject).
 * Garantit que le service injecté (Web, VS Code, ou IntelliJ) respecte strictement le contrat IRecentService.
 */
export const RECENT_SERVICE_KEY: InjectionKey<IRecentService> = Symbol('RecentService');