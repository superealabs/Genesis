// genesis-sdk-web/genesis-web-core/src/features/frameworks/composables/useFrameworks.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import type { Framework, FrameworkFilters } from '@genesis-labs/shared-types';

import { FRAMEWORK_SERVICE_KEY, type IFrameworkService } from '../types/framework.service.interface';
import { useFrameworkStore } from '../store/useFramework.store';
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';

export function useFrameworks() {
  // ═══ 1. INJECTIONS & STORES ═══
  const service = inject(FRAMEWORK_SERVICE_KEY);
  if (!service) {
    throw new Error('[useFrameworks] IFrameworkService non fourni. Vérifiez app.provide() dans main.ts');
  }
  const svc = service as IFrameworkService;
  const store = useFrameworkStore();

  // ═══ 2. ÉTAT RÉACTIF (Depuis le store) ═══
  const { filteredFrameworks, displayMode, filters, searchQuery, isLoading, languages } = storeToRefs(store);

  // ═══ 3. ÉTAT LOCAL & LOGIQUE MÉTIER (Comparaison) ═══
  const compare = useCompareSlotsWithPopup<Framework>({
    slots: ['A', 'B', 'C', 'D'],
    getId: (f) => f.id
  });

  const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

  // ═══ 4. COMPUTEDS (Getters locaux) ═══
  const currentSelectedId = computed(() => {
    return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
  });

  const frameworkSlotsMap = computed(() => {
    if (compareMode.value !== 'compare') return new Map<number, string>();
    const map = new Map<number, string>();
    for (const [slot, framework] of Object.entries(compareSlots.value)) {
      if (framework) map.set(framework.id, slot);
    }
    return map;
  });

  // ═══ 5. ACTIONS MÉTIER ═══
  async function initialize() {
    store.setLoading(true);
    try {
      const [frameworksData, languagesData] = await Promise.all([
        svc.fetchFrameworks(),
        svc.fetchLanguages()
      ]);
      store.setFrameworks(frameworksData);
      store.setLanguages(languagesData);
    } catch (error) {
      console.error('[useFrameworks] Erreur lors du chargement:', error);
    } finally {
      store.setLoading(false);
    }
  }

  function setSearch(query: string) { store.setSearch(query); }
  function setFilters(newFilters: FrameworkFilters) { store.setFilters(newFilters); }
  
  function handleModeChange(newMode: 'selection' | 'compare') {
    compare.switchMode(newMode);
  }

  async function handleSelect(framework: Framework, event?: MouseEvent) {
    const result = compare.handleSelect(framework, event);
    if (result.action === 'pending-replace') {
      return { action: 'replace-needed' as const, event, framework };
    }
    return { action: result.action, event, framework };
  }

  async function handleReplace(slotId: string | number, framework: Framework) {
    compare.replaceSlot(slotId, framework);
  }

  // ═══ 6. RETURN (Ordonné logiquement) ═══
  return {
    // Données
    frameworks: filteredFrameworks,
    languages,
    filters,
    searchQuery,
    
    // UI & État
    displayMode,
    isLoading,
    selectedId: currentSelectedId,
    frameworkSlots: frameworkSlotsMap,
    compareMode,
    
    // Comparaison
    compare,
    showReplacePopup: compare.showReplacePopup,
    pendingFramework: compare.pendingItem,
    mouseX: compare.mouseX,
    mouseY: compare.mouseY,
    
    // Actions
    initialize,
    setSearch,
    setFilters,
    setDisplayMode: store.setDisplayMode,
    handleModeChange,
    handleSelect,
    handleReplace,
    cancelReplace: compare.cancelReplace,
    triggerReplace: compare.triggerReplace
  };
}