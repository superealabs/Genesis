// genesis-sdk-web/genesis-web-core/src/features/frameworks/store/useFramework.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework, Language } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';
import type { CheckboxState } from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckbox.vue';

export interface FrameworkFilterState {
  languages: Record<string, CheckboxState>;
  types: Record<string, CheckboxState>;
  coreFrameworks: Record<string, CheckboxState>;
  integrations: Record<string, CheckboxState>;
}

export const useFrameworkStore = defineStore('framework', () => {

  

  // ═══════════════════════════════════════════════════════════
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Domaine : Données métier
  const frameworks = ref<Framework[]>([]);
  const languages = ref<Language[]>([]);
  const CHECKED: CheckboxState = 'checked';
  const EXCLUDED: CheckboxState = 'refused';

  const filters = ref<Partial<FrameworkFilterState>>({});

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

    const f = filters.value;
    const langName = (fw: Framework) => languages.value.find(l => l.id === fw.languageId)?.name;

    result = result.filter(fw =>
      matchGroup(f.languages, langName(fw)) &&
      matchGroup(f.types, fw.type) &&
      matchGroup(f.coreFrameworks, fw.coreFramework) &&
      Object.entries(f.integrations ?? {}).every(([key, state]) => {
        const v = (fw as any)[key] === true;
        if (state === CHECKED) return v;
        if (state === EXCLUDED) return !v;
        return true;
      })
    );
    return result;
  });


  // ═══════════════════════════════════════════════════════════
  // 3. ACTIONS (MUTATIONS) - Groupées par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Actions : Données métier
  function setFrameworks(data: Framework[]) { frameworks.value = data; }
  function setLanguages(data: Language[]) { languages.value = data; }
  function setFilters(newFilters: FrameworkFilterState) { filters.value = newFilters; }

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

  function matchGroup(state: Record<string, CheckboxState> | undefined, value: string | undefined): boolean {
    if (!state) return true;
    const entries = Object.entries(state);
    const checked = entries.filter(([, s]) => s === CHECKED).map(([k]) => k);
    const excluded = entries.filter(([, s]) => s === EXCLUDED).map(([k]) => k);
    if (value !== undefined && excluded.includes(value)) return false;
    if (checked.length > 0) return value !== undefined && checked.includes(value);
    return true;
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