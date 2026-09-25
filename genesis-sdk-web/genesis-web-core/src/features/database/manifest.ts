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

export type { IDatabaseService } from '@genesis-labs/web-core/features/database/types/database.service.interface.ts';
export { DATABASE_SERVICE_KEY } from '@genesis-labs/web-core/features/database/types/database.service.interface.ts';

// ═══ STORE, COMPOSABLE & VUE ═══
export { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store.ts';
export { useDatabase } from '@genesis-labs/web-core/features/database/composables/useDatabase.ts';
export { default as DatabaseView } from '@genesis-labs/web-core/features/database/views/DatabaseView.vue';