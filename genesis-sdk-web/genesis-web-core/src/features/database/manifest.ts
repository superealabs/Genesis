// ═══ EXPORTS PARTAGÉS (Types purs) ═══
export type { 
    DatabaseConfig, 
    DatabaseEngineDto, 
    DatabaseConnectionTestResult 
} from '@genesis-labs/shared-types';

// ═══ EXPORTS SPÉCIFIQUES À L'UI ═══
export type { 
    // Types UI si besoin
} from './types/database.types';

export type { IDatabaseService } from './types/database.service.interface';
export { DATABASE_SERVICE_KEY } from './types/database.service.interface';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useDatabaseStore } from './store/useDatabase.store';
export { useDatabase } from './composables/useDatabase';
export { default as DatabaseSelection } from './views/DatabaseSelection.vue';