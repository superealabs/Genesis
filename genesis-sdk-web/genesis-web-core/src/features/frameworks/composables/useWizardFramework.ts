// genesis-sdk-web/genesis-web-core/src/features/frameworks/composables/useWizardFramework.ts
import { ref, computed, watch } from 'vue';
import { useFrameworks } from './useFrameworks';
import type { Framework } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';


export function useWizardFramework(onSelect: (framework: Framework, event?: MouseEvent) => void) {
  // ═══ 1. COMPOSABLE DE BASE ═══
  const fw = useFrameworks();
  const generatorStore = useGeneratorStore();

  // ═══ 2. ÉTAT UI LOCAL (Spécifique au Wizard) ═══
  const isFilterOpen = ref(false);
  const detailFramework = ref<Framework | null>(null);

  // ═══ 3. COMPUTEDS LOCAUX ═══
  const replaceOptions = computed<SelectionOption[]>(() => {
    if (!fw.compare?.slots?.value) return [];
    return Object.entries(fw.compare.slots.value)
      .filter(([, framework]) => framework !== null)
      .map(([slot, framework]) => ({
        id: slot,
        label: `Slot ${slot}`,
        description: (framework as Framework).name
      }));
  });

  // ═══ 4. ACTIONS WRAPPERS (Adaptation pour le Stepper) ═══
  async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
    try {
      await fw.handleSelect(framework, event);
      onSelect(framework, event);
    } catch (error) {
      console.error("❌ [WizardFramework] Erreur lors de la sélection :", error);
    }
  }

  function handleReplaceSelection(slotId: string | number) {
    if (fw.pendingFramework.value) {
      fw.handleReplace(slotId, fw.pendingFramework.value);
    }
    fw.cancelReplace();
  }

  // Actions UI locales
  function openFilter() { isFilterOpen.value = true; }
  function closeFilter() { isFilterOpen.value = false; }
  function closeDetail() { detailFramework.value = null; }
  function handleInfo(fwItem: Framework) { detailFramework.value = fwItem; }


  watch(
    () => generatorStore.pendingFramework,
    (pending) => {
      if (pending && pending.id !== fw.selectedId.value) {
        // Aligne l'état de sélection visuelle sur le pending du wizard
        fw.compare.handleSelect(pending);  // ou une méthode dédiée
      }
    },
    { immediate: true }
  );

  // ═══ 5. RETURN (Ordonné) ═══
  return {
    // Données & État (délégués à useFrameworks)
    frameworks: fw.frameworks,
    languages: fw.languages,
    selectedId: fw.selectedId,
    displayMode: fw.displayMode,
    compareMode: fw.compareMode,
    frameworkSlots: fw.frameworkSlots,
    filters: fw.filters,
    searchQuery: fw.searchQuery,
    isLoading: fw.isLoading,
    showReplacePopup: fw.showReplacePopup,
    pendingFramework: fw.pendingFramework,
    mouseX: fw.mouseX,
    mouseY: fw.mouseY,
    compare: fw.compare,
    comparator: fw.comparator,

    // État UI local & Computeds
    isFilterOpen,
    detailFramework,
    replaceOptions,

    // Actions déléguées
    initialize: fw.initialize,
    setSearch: fw.setSearch,
    setFilters: fw.setFilters,
    setDisplayMode: fw.setDisplayMode,
    handleModeChange: fw.handleModeChange,
    cancelReplace: fw.cancelReplace,
    triggerReplace: fw.triggerReplace,
    
    // Actions wrappers & locales
    handleSelectWrapper,
    handleReplaceSelection,
    openFilter,
    closeFilter,
    closeDetail,
    handleInfo,
  };
}