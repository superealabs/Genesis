// genesis-sdk-web/genesis-web-core/src/features/repository/types/repository.types.ts

// ═══ TYPES SPÉCIFIQUES À L'UI ═══
// Ce fichier peut contenir des imports avec '@/'.
// Il réexporte les types partagés pour la commodité des composants Vue.

export type { 
    RepositoryProviderType, 
    RepositoryAuthMethod, 
    RepositoryScope, 
    RepositoryProviderMetadata, 
    RepositoryConfig 
} from '@genesis-labs/shared-types';

// Tu pourras ajouter ici des types spécifiques à l'UI, par exemple :
// import type { SomeCoreType } from '@genesis-labs/web-core/core/types/...';
// export interface RepositoryUiState { ... }