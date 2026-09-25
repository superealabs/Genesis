// ═══ Types & Contrats ═══
export type { FrontendFramework } from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Types UI si besoin
} from '@genesis-labs/web-core/features/frontend/types/frontend.types';

export type { IFrontendService } from '@genesis-labs/web-core/features/frontend/types/frontend.service.interface';
export { FRONTEND_SERVICE_KEY } from '@genesis-labs/web-core/features/frontend/types/frontend.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';
export { useFrontend } from '@genesis-labs/web-core/features/frontend/composables/useFrontend';
export { default as FrontendsView } from '@genesis-labs/web-core/features/frontend/views//FrontendsView.vue';