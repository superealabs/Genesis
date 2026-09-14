// ═══ EXPORTS PARTAGÉS (Types purs, utilisables côté Node.js & Browser) ═══
// CORRECTION RÈGLE 1 : Utilisation du nom du package
export type { 
    FrameworkType, 
    Framework, 
    FrameworkFilters 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI (Uniquement pour le Webview / Vite) ═══
// CORRECTION RÈGLE 5 : Utilisation systématique de l'alias @/ pour les imports internes au core
export type { 
    // Ajoute ici les types UI si nécessaire 
} from '@/features/frameworks/types/framework.types';

export type { IFrameworkService } from '@/features/frameworks/types/framework.service.interface';
export { FRAMEWORK_SERVICE_KEY } from '@/features/frameworks/types/framework.service.interface';

// ═══ STORE ═══
export { useFrameworkStore } from '@/features/frameworks/store/useFramework.store';

// ═══ COMPOSABLE ═══
export { useFrameworks } from '@/features/frameworks/composables/useFrameworks';

// ═══ VUE ═══
export { default as FrameworksView } from '@/features/frameworks/views/FrameworksView.vue';