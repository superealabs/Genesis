// genesis-sdk-web/genesis-web-core/src/features/repository/composables/useWizardRepository.ts
import { ref, computed } from 'vue';
import { useRepository } from './useRepository';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

export function useWizardRepository(onSelect: (provider: RepositoryProviderMetadata, event?: MouseEvent) => void) {
  // ═══ 1. COMPOSABLE DE BASE ═══
  const repo = useRepository();

  // ═══ 2. ÉTAT UI LOCAL (Spécifique au Wizard) ═══
  // Les filtres étant désactivés pour cette feature, on ne garde que le détail.
  const detailProvider = ref<RepositoryProviderMetadata | null>(null);

  // ═══ 3. COMPUTEDS LOCAUX ═══
  const replaceOptions = computed<SelectionOption[]>(() => {
    if (!repo.compare?.slots?.value) return [];
    return Object.entries(repo.compare.slots.value)
      .filter(([, provider]) => provider !== null)
      .map(([slot, provider]) => ({
        id: slot,
        label: `Slot ${slot}`,
        description: (provider as RepositoryProviderMetadata).name
      }));
  });

  // ═══ 4. ACTIONS WRAPPERS (Adaptation pour le Stepper) ═══
  async function handleSelectWrapper(provider: RepositoryProviderMetadata, event?: MouseEvent) {
    try {
      await repo.handleSelect(provider, event);
      onSelect(provider, event);
    } catch (error) {
      console.error("❌ [WizardRepository] Erreur lors de la sélection :", error);
    }
  }

  function handleReplaceSelection(slotId: string | number) {
    if (repo.pendingProvider.value) {
      repo.handleReplace(slotId, repo.pendingProvider.value);
    }
    repo.cancelReplace();
  }

  // Actions UI locales
  function closeDetail() { detailProvider.value = null; }
  function handleInfo(providerItem: RepositoryProviderMetadata) { detailProvider.value = providerItem; }

  // ═══ 5. RETURN (Ordonné) ═══
  return {
    // Données & État (délégués à useRepository)
    providers: repo.providers,
    selectedId: repo.selectedId,
    displayMode: repo.displayMode,
    compareMode: repo.compareMode,
    providerSlots: repo.providerSlots,
    searchQuery: repo.searchQuery,
    isLoading: repo.isLoading,
    showReplacePopup: repo.showReplacePopup,
    pendingProvider: repo.pendingProvider,
    mouseX: repo.mouseX,
    mouseY: repo.mouseY,
    compare: repo.compare,
    comparator: repo.comparator,

    // État UI local & Computeds
    detailProvider,
    replaceOptions,

    // Actions déléguées
    initialize: repo.initialize,
    setSearch: repo.setSearch,
    setDisplayMode: repo.setDisplayMode,
    handleModeChange: repo.handleModeChange,
    cancelReplace: repo.cancelReplace,
    triggerReplace: repo.triggerReplace,
    
    // Actions wrappers & locales
    handleSelectWrapper,
    handleReplaceSelection,
    closeDetail,
    handleInfo,
  };
}