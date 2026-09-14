import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useDatabaseStore } from '@/features/database/store/useDatabase.store';
import { useCompareSlots } from '@/core/composables/ux/useCompareSlots';
import { DATABASE_SERVICE_KEY, type IDatabaseService } from '@/features/database/types/database.service.interface';
import type { DatabaseConfig, DatabaseEngineDto } from '@/features/database/types/database.types';

export function useDatabase() {
    // 1. Injection du service
    const service = inject(DATABASE_SERVICE_KEY);
    if (!service) {
        throw new Error('[useDatabase] IDatabaseService non fourni. Vérifiez le provide() dans le composant parent ou main.ts');
    }
    
    const svc = service as IDatabaseService;
    const store = useDatabaseStore();
    
    // 2. Exposition réactive de l'état du store
    const { 
        availableEngines, 
        isLoading, 
        connectionTestResult,
        hasEngines,
        isConnectionSuccessful,
        displayMode
    } = storeToRefs(store);

    // ✅ 3. LOGIQUE DE SÉLECTION ET COMPARAISON (Comme useFrameworks)
    const compare = useCompareSlots<DatabaseEngineDto>({
        slots: ['A', 'B', 'C', 'D'], // Ou juste ['A'] si tu ne veux qu'une seule sélection
        getId: (db) => db.id
    });

    const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

    const currentSelectedId = computed(() => {
        return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
    });

    const databaseSlotsMap = computed(() => {
        if (compareMode.value !== 'compare') return new Map<number, string>();
        const map = new Map<number, string>();
        for (const [slot, db] of Object.entries(compareSlots.value)) {
            if (db) map.set(db.id, slot);
        }
        return map;
    });

    // ═══════════════════════════════════════════════════════════
    // ACTIONS MÉTIER
    // ═══════════════════════════════════════════════════════════

    async function fetchAvailableEngines() {
        store.setLoading(true);
        try {
            const data = await svc.fetchDatabaseEngines();
            store.setAvailableEngines(data);
        } catch (error) {
            console.error('[useDatabase] Erreur lors du chargement des moteurs:', error);
        } finally {
            store.setLoading(false);
        }
    }

    async function testConnection(config: DatabaseConfig) {
        store.setLoading(true);
        try {
            const result = await svc.testDatabaseConnection(config);
            store.setConnectionTestResult(result);
            return result;
        } catch (error) {
            console.error('[useDatabase] Erreur lors du test de connexion:', error);
            const errorResult = { 
                success: false, 
                message: error instanceof Error ? error.message : 'Une erreur inconnue est survenue.' 
            };
            store.setConnectionTestResult(errorResult);
            return errorResult;
        } finally {
            store.setLoading(false);
        }
    }

    // ✅ 4. HANDLERS DE SÉLECTION
    function handleSelect(engine: DatabaseEngineDto, event?: MouseEvent) {
        const result = compare.handleSelect(engine);
        // Note: Contrairement aux frameworks, on n'a pas forcément besoin d'appeler 
        // svc.selectDatabaseEngine(id) ici, car la sélection est gérée par le store Generator.
        // On retourne juste le résultat pour que la vue puisse l'émettre.
        return { action: result.action, event, engine };
    }

    function handleReplace(slotId: string | number, engine: DatabaseEngineDto) {
        compare.replaceSlot(slotId, engine);
    }

    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }

    function toggleDisplayMode() { store.setDisplayMode(displayMode.value === 'grid' ? 'list' : 'grid'); }

    // ═══════════════════════════════════════════════════════════
    // RETOUR FINAL
    // ═══════════════════════════════════════════════════════════
    return {
        // État réactif
        engines: availableEngines, // Renommé pour correspondre à "frameworks" dans la vue
        selectedId: currentSelectedId,
        databaseSlots: databaseSlotsMap,
        compareMode,
        compare,
        isLoading,
        connectionTestResult,
        hasEngines,
        isConnectionSuccessful,
        displayMode,
        
        // Actions
        fetchAvailableEngines,
        testConnection,
        handleSelect,
        handleReplace,
        handleModeChange,
        reset: store.reset,
        toggleDisplayMode
    };
}