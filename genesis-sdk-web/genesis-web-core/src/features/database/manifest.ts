// ═══ EXPORTS PARTAGÉS (Types purs, utilisables côté Node.js & Browser) ═══
// CORRECTION RÈGLE 1 : Utilisation du nom du package, jamais de chemin relatif
export type { 
    DatabaseConfig, 
    DatabaseEngineDto, 
    DatabaseConnectionTestResult 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI (Uniquement pour le Webview / Vite) ═══
// CORRECTION RÈGLE 5 : Utilisation de l'alias @/ pour les imports internes au core
export type { 
    // Tu pourras ajouter ici des types purement UI si besoin à l'avenir
    // ex: DatabaseFormState, DbValidationRules
} from '@/features/database/types/database.types';

export type { IDatabaseService } from '@/features/database/types/database.service.interface';
export { DATABASE_SERVICE_KEY } from '@/features/database/types/database.service.interface';

// ═══ STORE ═══
export { useDatabaseStore } from '@/features/database/store/useDatabase.store';

// ═══ COMPOSABLE ═══
export { useDatabase } from '@/features/database/composables/useDatabase';

// ═══ VUE ═══
export { default as DatabaseSelection } from '@/features/database/views/DatabaseSelection.vue';