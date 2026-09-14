// ═══ EXPORTS PARTAGÉS (Types purs, utilisables côté Node.js & Browser) ═══
// Chemin relatif vers le package de types partagés
export type { 
    DatabaseConfig, 
    DatabaseEngineDto, 
    DatabaseConnectionTestResult 
} from '../../../../genesis-web-types-shared/src/database.shared';

// ═══ EXPORTS SPÉCIFIQUES À L'UI (Uniquement pour le Webview / Vite) ═══
export type { 
    // Tu pourras ajouter ici des types purement UI si besoin à l'avenir
    // ex: DatabaseFormState, DbValidationRules
} from './types/database.types';

export type { IDatabaseService } from './types/database.service.interface';
export { DATABASE_SERVICE_KEY } from './types/database.service.interface';

// ═══ STORE ═══
export { useDatabaseStore } from './store/useDatabase.store';

// ═══ COMPOSABLE ═══
export { useDatabase } from './composables/useDatabase';

// ═══ VUE ═══
export { default as DatabaseSelection } from './views/DatabaseSelection.vue';