// genesis-sdk-web/genesis-web-core/src/features/frameworks/store/useFramework.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework, FrameworkFilters, Language } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useFrameworkStore = defineStore('framework', () => {

  // ═══════════════════════════════════════════════════════════
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Domaine : Données métier
  const frameworks = ref<Framework[]>([]);
  const languages = ref<Language[]>([]);
  const filters = ref<FrameworkFilters>({});

  // Domaine : Interface Utilisateur (UI)
  const searchQuery = ref('');
  const displayMode = ref<DisplayMode>('grid');
  const isLoading = ref(false);
  const isFilterOpen = ref(false);
  const detailFramework = ref<Framework | null>(null);


  // ═══════════════════════════════════════════════════════════
  // 2. GETTERS (COMPUTED)
  // ═══════════════════════════════════════════════════════════
  
  const filteredFrameworks = computed(() => {
    let result = frameworks.value;

    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase();
      result = result.filter(f =>
        f.name.toLowerCase().includes(q) ||
        f.coreFramework.toLowerCase().includes(q) ||
        f.type.toLowerCase().includes(q)
      );
    }

    if (filters.value.type) result = result.filter(f => f.type === filters.value.type);
    if (filters.value.coreFramework) result = result.filter(f => f.coreFramework === filters.value.coreFramework);
    if (filters.value.isProd !== undefined) result = result.filter(f => f.isProd === filters.value.isProd);
    if (filters.value.useDB !== undefined) result = result.filter(f => f.useDB === filters.value.useDB);
    if (filters.value.useCloud !== undefined) result = result.filter(f => f.useCloud === filters.value.useCloud);
    if (filters.value.useEurekaServer !== undefined) result = result.filter(f => f.useEurekaServer === filters.value.useEurekaServer);
    if (filters.value.isGateway !== undefined) result = result.filter(f => f.isGateway === filters.value.isGateway);
    if (filters.value.useFrontendApp !== undefined) result = result.filter(f => f.useFrontendApp === filters.value.useFrontendApp);

    return result;
  });


  // ═══════════════════════════════════════════════════════════
  // 3. ACTIONS (MUTATIONS) - Groupées par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Actions : Données métier
  function setFrameworks(data: Framework[]) { frameworks.value = data; }
  function setLanguages(data: Language[]) { languages.value = data; }
  function setFilters(newFilters: FrameworkFilters) { filters.value = { ...filters.value, ...newFilters }; }

  // Actions : Interface Utilisateur (UI)
  function setSearch(query: string) { searchQuery.value = query; }
  function setDisplayMode(mode: DisplayMode) { displayMode.value = mode; }
  function setLoading(state: boolean) { isLoading.value = state; }
  
  // Action : Réinitialisation (Propreté)
  function reset() {
    frameworks.value = [];
    languages.value = [];
    filters.value = {};
    searchQuery.value = '';
    displayMode.value = 'grid';
    isLoading.value = false;
    isFilterOpen.value = false;
    detailFramework.value = null;
  }


  // ═══════════════════════════════════════════════════════════
  // 4. RETURN - Ordre identique à la déclaration
  // ═══════════════════════════════════════════════════════════
  return {
    // État
    frameworks, languages, filters, searchQuery, displayMode, isLoading, isFilterOpen, detailFramework,
    // Getters
    filteredFrameworks,
    // Actions
    setFrameworks, setLanguages, setFilters, setSearch, setDisplayMode, setLoading, reset
  };
});