// genesis-sdk-web/genesis-web-core/src/core/features/config/types/config.service.interface.ts
import type { InjectionKey } from 'vue';
import type { 
    IConfigService, 
    GenesisConfig, 
    ConfigType 
} from '@genesis-labs/shared-types';

// Réexportation pour la commodité des imports dans les autres fichiers
export type { GenesisConfig, ConfigType };

/**
 * Interface du service de gestion des configurations.
 * Elle étend IConfigStorage pour garantir que toutes les méthodes de stockage 
 * sont implémentées, tout en permettant d'ajouter des méthodes spécifiques au core si nécessaire.
 */
export type { IConfigService };

/**
 * Clé d'injection typée pour Vue (provide / inject).
 * Garantit que le service injecté respecte strictement le contrat IConfigService.
 */
export const CONFIG_SERVICE_KEY: InjectionKey<IConfigService> = Symbol('ConfigService');