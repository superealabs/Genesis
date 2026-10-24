// genesis-sdk-web/genesis-web-core/src/features/repository/composables/useRepository.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';

import { REPOSITORY_SERVICE_KEY, type IRepositoryService } from '../types/repository.service.interface';
import { useRepositoryStore } from '../store/useRepository.store';
import RepositoryDetails from '../components/RepositoryDetails.vue';
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';
import { useComparator } from '@genesis-labs/web-core/core/composables/ux/useComparator';

export function useRepository() {
  // ═══ 1. INJECTIONS & STORES ═══
  const service = inject(REPOSITORY_SERVICE_KEY);
  if (!service) {
    throw new Error('[useRepository] IRepositoryService non fourni. Vérifiez app.provide() dans main.ts');
  }
  const svc = service as IRepositoryService;
  const store = useRepositoryStore();

  // ═══ 2. ÉTAT RÉACTIF (Depuis le store) ═══
  const { 
    providers: allProviders, 
    filteredProviders, 
    displayMode, 
    searchQuery, 
    isLoading, 
    /* selectedProviderId */ 
  } = storeToRefs(store);

  // ═══ 3. ÉTAT LOCAL & LOGIQUE MÉTIER (Comparaison) ═══
  const compare = useCompareSlotsWithPopup<RepositoryProviderMetadata>({
    slots: ['A', 'B', 'C', 'D'],
    getId: (p) => p.id
  });

  const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

  // Vue de comparaison : lit les slots ci-dessus.
  const comparator = useComparator<RepositoryProviderMetadata>({
    compare,
    items: allProviders,
    getId: (p) => p.id,
    getLabel: (p) => p.name,
    component: RepositoryDetails,
    propName: 'provider'
  });

  // ═══ 4. COMPUTEDS (Getters locaux) ═══
  const currentSelectedId = computed(() => {
    return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
  });

  const providerSlotsMap = computed(() => {
    if (compareMode.value !== 'compare') return new Map<string, string>();
    const map = new Map<string, string>();
    for (const [slot, provider] of Object.entries(compareSlots.value)) {
      if (provider) map.set(provider.id, slot);
    }
    return map;
  });

  // ═══ 5. ACTIONS MÉTIER ═══
  async function initialize() {
    store.setLoading(true);
    try {
      const providersData = await svc.getSupportedProviders();
      store.setProviders(providersData);
    } catch (error) {
      console.error('[useRepository] Erreur lors du chargement:', error);
    } finally {
      store.setLoading(false);
    }
  }

  function setSearch(query: string) { store.setSearch(query); }
  
  function handleModeChange(newMode: 'selection' | 'compare') {
    compare.switchMode(newMode);
  }

  async function handleSelect(provider: RepositoryProviderMetadata, event?: MouseEvent) {
    const result = compare.handleSelect(provider, event);
    if (result.action === 'pending-replace') {
      return { action: 'replace-needed' as const, event, provider };
    }
    return { action: result.action, event, provider };
  }

  async function handleReplace(slotId: string | number, provider: RepositoryProviderMetadata) {
    compare.replaceSlot(slotId, provider);
  }

  // ═══ 6. RETURN (Ordonné logiquement) ═══
  return {
    // Données
    providers: filteredProviders,
    searchQuery,
    
    // UI & État
    displayMode,
    isLoading,
    selectedId: currentSelectedId,
    providerSlots: providerSlotsMap,
    compareMode,
    
    // Comparaison
    compare,
    comparator,
    showReplacePopup: compare.showReplacePopup,
    pendingProvider: compare.pendingItem,
    mouseX: compare.mouseX,
    mouseY: compare.mouseY,
    
    // Actions
    initialize,
    setSearch,
    setDisplayMode: store.setDisplayMode,
    handleModeChange,
    handleSelect,
    handleReplace,
    cancelReplace: compare.cancelReplace,
    triggerReplace: compare.triggerReplace
  };
}