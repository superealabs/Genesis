// ═══ EXPORTS PARTAGÉS (Types purs) ═══
export type { 
    FrameworkType, 
    Framework, 
    FrameworkFilters 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Types UI si besoin
} from '@genesis-labs/web-core/features/frameworks/types/framework.types';

export type { IFrameworkService } from '@genesis-labs/web-core/features/frameworks/types/framework.service.interface';
export { FRAMEWORK_SERVICE_KEY } from '@genesis-labs/web-core/features/frameworks/types/framework.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
export { useFrameworks } from '@genesis-labs/web-core/features/frameworks/composables/useFrameworks';
export { default as FrameworksView } from '@genesis-labs/web-core/features/frameworks/views/FrameworksView.vue';