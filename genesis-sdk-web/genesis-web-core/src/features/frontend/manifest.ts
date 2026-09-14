// genesis-web-core/src/features/frontend/manifest.ts

// ═══ Types & Contrats ═══
export type { FrontendFramework } from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Ajoute ici les types UI si nécessaire (ex: FrontendUiState)
} from '@/features/frontend/types/frontend.types';

export type { IFrontendService } from '@/features/frontend/types/frontend.service.interface';
export { FRONTEND_SERVICE_KEY } from '@/features/frontend/types/frontend.service.interface';

// ═══ STORE ═══
export { useFrontendStore } from '@/features/frontend/store/useFrontend.store';

// ═══ COMPOSABLE ═══
export { useFrontend } from '@/features/frontend/composables/useFrontend';

// ═══ VUE ═══
export { default as FrontendSelectionView } from '@/features/frontend/views/FrontEndSelectionView.vue';