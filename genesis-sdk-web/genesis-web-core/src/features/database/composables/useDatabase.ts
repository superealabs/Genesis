// genesis-sdk-web/genesis-web-core/src/features/database/composables/useDatabase.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

// Types
import type { DatabaseConfig, DatabaseEngineDto } from '@genesis-labs/shared-types';

// Services & Stores
import { DATABASE_SERVICE_KEY, type IDatabaseService } from '../types/database.service.interface';
import { useDatabaseStore } from '../store/useDatabase.store';

// Composables
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';

/**
 * Composable métier pour la gestion des bases de données.
 * 
 * Il orchestre le chargement des moteurs de base de données, la gestion de l'état 
 * via le store, et la logique de sélection/comparaison multiple via un composable générique.
 */
export function useDatabase() {
  // ==========================================================================
  // 1. INJECTION & STORE
  // ==========================================================================
  const service = inject(DATABASE_SERVICE_KEY);
  if (!service) {
    throw new Error('[useDatabase] IDatabaseService non fourni. Vérifiez le provide() dans le composant parent ou main.ts');
  }
  
  const svc = service as IDatabaseService;
  const store = useDatabaseStore();

  // ==========================================================================
  // 2. ÉTAT RÉACTIF (Depuis le store)
  // ==========================================================================
  const {
    availableEngines, 
    isLoading,
    connectionTestResult,
    hasEngines,
    isConnectionSuccessful,
    displayMode
  } = storeToRefs(store);

  // ==========================================================================
  // 3. ÉTAT LOCAL & LOGIQUE DE COMPARAISON
  // ==========================================================================
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

  // ==========================================================================
  // 4. ACTIONS MÉTIER
  // ==========================================================================

  /**
   * Récupère la liste des moteurs de base de données disponibles depuis le service
   * et met à jour le store. Gère l'état de chargement et les erreurs.
   */
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

  /**
   * Teste la connexion à une base de données avec la configuration fournie.
   * Retourne le résultat du test et met à jour l'état dans le store.
   */
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

  /**
   * Wrapper autour de la logique de sélection du composable de comparaison.
   * Permet de gérer les cas où un remplacement de slot est nécessaire.
   */
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

  // ==========================================================================
  // 5. RETOUR FINAL (Ordonné par domaine)
  // ==========================================================================
  return {
    // Données
    engines: availableEngines,
    
    // État UI
    displayMode,
    isLoading,
    selectedId: currentSelectedId,
    databaseSlots: databaseSlotsMap,
    compareMode,
    
    // État de connexion
    connectionTestResult,
    hasEngines,
    isConnectionSuccessful,
    
    // Comparaison & Popup
    compare,
    showReplacePopup: compare.showReplacePopup,
    pendingEngine: compare.pendingItem,
    mouseX: compare.mouseX,
    mouseY: compare.mouseY,
    
    // Actions
    fetchAvailableEngines,
    testConnection,
    handleSelect,
    handleReplace,
    handleModeChange,
    setDisplayMode: store.setDisplayMode,
    reset: store.reset,
    cancelReplace: compare.cancelReplace,
    triggerReplace: compare.triggerReplace
  };
}