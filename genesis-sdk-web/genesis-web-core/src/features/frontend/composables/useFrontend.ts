// genesis-sdk-web/genesis-web-core/src/features/frontend/composables/useFrontend.ts
import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

// Types
import type { FrontendFramework } from '@genesis-labs/shared-types';

// Services & Stores
import { FRONTEND_SERVICE_KEY, type IFrontendService } from '../types/frontend.service.interface';
import { useFrontendStore } from '../store/useFrontend.store';

// Composables
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';

/**
 * Composable métier pour la gestion des frameworks frontend.
 * 
 * Il orchestre le chargement des frameworks, la gestion de l'état via le store,
 * et la logique de sélection/comparaison multiple via un composable générique.
 */
export function useFrontend() {
  // ==========================================================================
  // 1. INJECTION & STORE
  // ==========================================================================
  const service = inject(FRONTEND_SERVICE_KEY);
  if (!service) {
    throw new Error('[useFrontend] IFrontendService non fourni. Vérifiez app.provide() dans main.ts');
  }

  const svc = service as IFrontendService;
  const store = useFrontendStore();

  // ==========================================================================
  // 2. ÉTAT RÉACTIF (Depuis le store)
  // ==========================================================================
  const { 
    availableFrontendFrameworks, 
    displayMode,
    searchQuery,
    isLoading
  } = storeToRefs(store);

  // ==========================================================================
  // 3. ÉTAT LOCAL & LOGIQUE DE COMPARAISON
  // ==========================================================================
  const compare = useCompareSlotsWithPopup<FrontendFramework>({
    slots: ['A', 'B', 'C', 'D'],
    getId: (fw) => fw.id
  });

  const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

  // ==========================================================================
  // 4. COMPUTEDS (Données dérivées)
  // ==========================================================================
  const currentSelectedId = computed(() => {
    return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
  });

  const frontendFrameworkSlotsMap = computed(() => {
    if (compareMode.value !== 'compare') return new Map<number, string>();
    const map = new Map<number, string>();
    for (const [slot, fw] of Object.entries(compareSlots.value)) {
      if (fw) map.set(fw.id, slot);
    }
    return map;
  });

  // ==========================================================================
  // 5. ACTIONS MÉTIER
  // ==========================================================================

  /**
   * Initialise les données en récupérant la liste des frameworks frontend
   * depuis le service et en mettant à jour le store.
   */
  async function initialize() {
    store.setLoading(true);
    try {
      const data = await svc.fetchFrontendFrameworks();
      store.setAvailableFrontendFrameworks(data);
    } catch (error) {
      console.error('[useFrontend] Erreur lors du chargement des frameworks frontend:', error);
    } finally {
      store.setLoading(false);
    }
  }

  /**
   * Wrapper autour de la logique de sélection du composable de comparaison.
   * Permet de gérer les cas où un remplacement de slot est nécessaire.
   */
  async function handleSelect(framework: FrontendFramework, event?: MouseEvent) {
    const result = compare.handleSelect(framework, event);
    if (result.action === 'pending-replace') {
      return { action: 'replace-needed' as const, event, framework };
    }
    return { action: result.action, event, framework };
  }

  function handleReplace(slotId: string | number, framework: FrontendFramework) {
    compare.replaceSlot(slotId, framework);
  }

  function handleModeChange(newMode: 'selection' | 'compare') {
    compare.switchMode(newMode);
  }

  function handleInfo(framework: FrontendFramework) {
    console.log('[useFrontend] Détails demandés pour :', framework.name);
  }

  // ==========================================================================
  // 6. RETOUR FINAL (Ordonné par domaine)
  // ==========================================================================
  return {
    // Données
    availableFrontendFrameworks,
    searchQuery,
    
    // État UI
    displayMode,
    isLoading,
    selectedId: currentSelectedId,
    frontendFrameworkSlots: frontendFrameworkSlotsMap,
    compareMode,
    
    // Comparaison & Popup
    compare,
    showReplacePopup: compare.showReplacePopup,
    pendingFramework: compare.pendingItem,
    mouseX: compare.mouseX,
    mouseY: compare.mouseY,
    
    // Actions
    initialize,
    handleSelect,
    handleReplace,
    handleModeChange,
    handleInfo,
    setSearch: store.setSearch,
    setDisplayMode: store.setDisplayMode,
    reset: store.reset,
    cancelReplace: compare.cancelReplace,
    triggerReplace: compare.triggerReplace
  };
}