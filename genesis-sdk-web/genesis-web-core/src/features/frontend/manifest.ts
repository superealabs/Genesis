// genesis-web-core/src/features/frontend/manifest.ts

// ═══ Types & Contrats ═══
export type { FrontendFramework } from '../../../../genesis-web-types-shared/src/frontend.shared.ts';
export type { } from './types/frontend.types';
export type { IFrontendService } from './types/frontend.service.interface';
export { FRONTEND_SERVICE_KEY } from './types/frontend.service.interface';

// ═══ Store ═══
export { useFrontendStore } from './store/useFrontend.store';

// ═══ Composable ═══
export { useFrontend } from './composables/useFrontend';

// ═══ Vue ═══
export { default as FrontendSelectionView } from './views/FrontEndSelectionView.vue';