// ═══ Types & Contrats ═══
export type { FrontendFramework } from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Types UI si besoin
} from './types/frontend.types';

export type { IFrontendService } from './types/frontend.service.interface';
export { FRONTEND_SERVICE_KEY } from './types/frontend.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useFrontendStore } from './store/useFrontend.store';
export { useFrontend } from './composables/useFrontend';
export { default as FrontendSelectionView } from './views/FrontEndSelectionView.vue';