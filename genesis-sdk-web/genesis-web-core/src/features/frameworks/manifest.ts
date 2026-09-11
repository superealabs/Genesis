export type { 
    FrameworkType, 
    Framework, 
    FrameworkFilters 
} from '../../../../genesis-web-types-shared/src/framework.shared.ts';

// ═══ EXPORTS SPÉCIFIQUES À L'UI (Uniquement pour le Webview / Vite) ═══
export type { 
    // Ajoute ici les types UI si nécessaire 
} from './types/framework.types';
export type { IFrameworkService } from './types/framework.service.interface';
export { FRAMEWORK_SERVICE_KEY } from './types/framework.service.interface';

// Store
export { useFrameworkStore } from './store/useFramework.store';

// Composable
export { useFrameworks } from './composables/useFrameworks';

// Vue
export { default as FrameworksView } from './views/FrameworksView.vue';