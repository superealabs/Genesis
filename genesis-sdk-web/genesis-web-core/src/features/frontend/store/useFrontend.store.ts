// genesis-sdk-web/genesis-web-core/src/features/frontend/store/useFrontend.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';

// Types
import type { FrontendFramework, InterfaceLanguage } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useFrontendStore = defineStore('frontend', () => {

  // ==========================================================================
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ==========================================================================
  
  // Domaine : Données métier
  const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
  const selectedFrontendFramework = ref<FrontendFramework | null>(null);
  const availableInterfaceLanguages = ref<InterfaceLanguage[]>([]);

  // Domaine : Interface Utilisateur (UI)
  const displayMode = ref<DisplayMode>('grid');
  const searchQuery = ref('');
  const isLoading = ref(false);


  // ==========================================================================
  // 2. GETTERS (COMPUTED) - Dérivés de l'état
  // ==========================================================================
  
  const hasSelectedFrontendFramework = computed(() => selectedFrontendFramework.value !== null);
  
  const getAvailableInterfaceLanguages = computed(() => availableInterfaceLanguages.value);


  // ==========================================================================
  // 3. ACTIONS - Groupées par domaine
  // ==========================================================================
  
  // Actions : Données métier
  function setAvailableFrontendFrameworks(frameworks: FrontendFramework[]) {
    availableFrontendFrameworks.value = frameworks;
  }

  function selectFrontendFramework(framework: FrontendFramework) {
    selectedFrontendFramework.value = framework;
  }

  function setAvailableInterfaceLanguages(languages: InterfaceLanguage[]) {
    availableInterfaceLanguages.value = languages;
  }

  // Actions : Interface Utilisateur (UI)
  function setDisplayMode(mode: DisplayMode) {
    displayMode.value = mode;
  }

  function setSearch(query: string) {
    searchQuery.value = query;
  }

  function setLoading(state: boolean) {
    isLoading.value = state;
  }

  // Action : Réinitialisation complète du store
  function reset() {
    availableFrontendFrameworks.value = [];
    selectedFrontendFramework.value = null;
    availableInterfaceLanguages.value = [];
    displayMode.value = 'grid';
    searchQuery.value = '';
    isLoading.value = false;
  }


  // ==========================================================================
  // 4. RETURN - Ordre identique à la déclaration
  // ==========================================================================
  return {
    // État
    availableFrontendFrameworks,
    selectedFrontendFramework,
    availableInterfaceLanguages,
    displayMode,
    searchQuery,
    isLoading,
    
    // Getters
    hasSelectedFrontendFramework,
    getAvailableInterfaceLanguages,
    
    // Actions
    setAvailableFrontendFrameworks,
    selectFrontendFramework,
    setAvailableInterfaceLanguages,
    setDisplayMode,
    setSearch,
    setLoading,
    reset
  };
});