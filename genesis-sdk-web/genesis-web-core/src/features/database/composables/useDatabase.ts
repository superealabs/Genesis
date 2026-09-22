import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useDatabaseStore } from '@genesis-labs/web-core/features/database/store/useDatabase.store';
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';
import { DATABASE_SERVICE_KEY, type IDatabaseService } from '@genesis-labs/web-core/features/database/types/database.service.interface';
import type { DatabaseConfig, DatabaseEngineDto } from '@genesis-labs/shared-types';

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

    // 3. LOGIQUE DE SÉLECTION ET COMPARAISON (Comme useFrameworks)
    const compare = useCompareSlotsWithPopup<DatabaseEngineDto>({
        slots: ['A', 'B', 'C', 'D'],
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

    function handleSelect(engine: DatabaseEngineDto, event?: MouseEvent) {
        const result = compare.handleSelect(engine, event); 
        return { action: result.action, event, engine };
    }

    function handleReplace(slotId: string | number, engine: DatabaseEngineDto) {
        compare.replaceSlot(slotId, engine);
    }

    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }

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
        setDisplayMode: store.setDisplayMode,
        testConnection,
        handleSelect,
        handleReplace,
        handleModeChange,
        reset: store.reset,
        
        showReplacePopup: compare.showReplacePopup,
        pendingEngine: compare.pendingItem, // Alias pour la clarté
        mouseX: compare.mouseX,
        mouseY: compare.mouseY,
        cancelReplace: compare.cancelReplace,
        triggerReplace: compare.triggerReplace
    };
}