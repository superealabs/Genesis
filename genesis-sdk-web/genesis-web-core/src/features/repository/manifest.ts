// genesis-sdk-web/genesis-web-core/src/features/repository/manifest.ts

// ═══ EXPORTS PARTAGÉS (Types purs) ═══
export type { 
    RepositoryProviderType, 
    RepositoryAuthMethod, 
    RepositoryScope, 
    RepositoryProviderMetadata, 
    RepositoryConfig 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Tu pourras ajouter ici des types spécifiques à l'UI si besoin à l'avenir
} from '@genesis-labs/web-core/features/repository/types/repository.types';

export type { IRepositoryService } from '@genesis-labs/web-core/features/repository/types/repository.service.interface';
export { REPOSITORY_SERVICE_KEY } from '@genesis-labs/web-core/features/repository/types/repository.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useRepositoryStore } from '@genesis-labs/web-core/features/repository/store/useRepository.store';
export { useRepository } from '@genesis-labs/web-core/features/repository/composables/useRepository';
export { default as RepositoryView } from '@genesis-labs/web-core/features/repository/views/RepositoryView.vue';