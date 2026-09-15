// ═══ EXPORTS PARTAGÉS (Types purs) ═══
export type { 
    FrameworkType, 
    Framework, 
    FrameworkFilters 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Types UI si besoin
} from './types/framework.types';

export type { IFrameworkService } from './types/framework.service.interface';
export { FRAMEWORK_SERVICE_KEY } from './types/framework.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useFrameworkStore } from './store/useFramework.store';
export { useFrameworks } from './composables/useFrameworks';
export { default as FrameworksView } from './views/FrameworksView.vue';