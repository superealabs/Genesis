// genesis-sdk-web/genesis-web-core/src/features/database/store/useDatabase.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

// Types
import type { DatabaseEngineDto, DatabaseConnectionTestResult } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useDatabaseStore = defineStore('database', () => {

  // ==========================================================================
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ==========================================================================
  
  // Domaine : Données métier
  const availableEngines = ref<DatabaseEngineDto[]>([]);
  const connectionTestResult = ref<DatabaseConnectionTestResult | null>(null);

  // Domaine : Interface Utilisateur (UI)
  const displayMode = ref<DisplayMode>('grid');
  const isLoading = ref(false);


  // ==========================================================================
  // 2. GETTERS (COMPUTED) - Dérivés de l'état
  // ==========================================================================
  
  const hasEngines = computed(() => availableEngines.value.length > 0);
  
  const isConnectionSuccessful = computed(() => connectionTestResult.value?.success === true);


  // ==========================================================================
  // 3. ACTIONS (MUTATIONS) - Groupées par domaine
  // ==========================================================================
  
  // Actions : Données métier
  function setAvailableEngines(data: DatabaseEngineDto[]) { 
    availableEngines.value = data; 
  }
  
  function setConnectionTestResult(result: DatabaseConnectionTestResult | null) {
    connectionTestResult.value = result;
  }

  function clearConnectionTestResult() {
    connectionTestResult.value = null;
  }

  // Actions : Interface Utilisateur (UI)
  function setLoading(state: boolean) { 
    isLoading.value = state; 
  }

  function setDisplayMode(mode: DisplayMode) { 
    displayMode.value = mode; 
  }

  // Action : Réinitialisation
  function reset() {
    availableEngines.value = [];
    connectionTestResult.value = null;
    displayMode.value = 'grid';
    isLoading.value = false;
  }


  // ==========================================================================
  // 4. RETURN - Ordre identique à la déclaration
  // ==========================================================================
  return {
    // État
    availableEngines,
    connectionTestResult,
    displayMode,
    isLoading,
    
    // Getters
    hasEngines,
    isConnectionSuccessful,
    
    // Actions
    setAvailableEngines,
    setConnectionTestResult,
    clearConnectionTestResult,
    setLoading,
    setDisplayMode,
    reset
  };
});