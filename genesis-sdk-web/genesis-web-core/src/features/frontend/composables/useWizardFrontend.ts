// genesis-sdk-web/genesis-web-core/src/features/frontend/composables/useWizardFrontend.ts
import { computed } from 'vue';

// Types
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

// Composables
import { useFrontend } from './useFrontend';

/**
 * Composable adaptateur pour intégrer la logique des frameworks frontend dans le Wizard.
 * 
 * Il fait le pont entre useFrontend (logique métier et état) et l'interface utilisateur 
 * du Stepper, en formatant les données et les événements spécifiquement pour ce contexte.
 */
export function useWizardFrontend(onSelect: (framework: FrontendFramework, event?: MouseEvent) => void) {
  // ==========================================================================
  // 1. COMPOSABLE DE BASE
  // ==========================================================================
  const fe = useFrontend();

  // ==========================================================================
  // 2. COMPUTEDS LOCAUX (Adaptation pour l'UI du Wizard)
  // ==========================================================================
  
  /**
   * Transforme les slots de comparaison actifs en une liste d'options 
   * exploitables par le popup de remplacement du Wizard.
   */
  const replaceOptions = computed<SelectionOption[]>(() => {
    if (!fe.compare?.slots?.value) return [];
    
    return Object.entries(fe.compare.slots.value)
      .filter(([, fw]) => fw !== null)
      .map(([slot, fw]) => ({
        id: slot,
        label: `Slot ${slot}`,
        description: (fw as FrontendFramework).name
      }));
  });

  // ==========================================================================
  // 3. ACTIONS WRAPPERS (Adaptation pour le Stepper)
  // ==========================================================================

  /**
   * Wrapper autour de la sélection pour intercepter le résultat, 
   * gérer les erreurs potentielles et déclencher le callback du parent.
   */
  async function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
    try {
      await fe.handleSelect(framework, event);
      onSelect(framework, event);
    } catch (error) {
      console.error("[WizardFrontend] Erreur lors de la sélection :", error);
    }
  }

  /**
   * Gère le remplacement d'un framework dans un slot de comparaison, 
   * puis annule l'état d'attente du popup.
   */
  function handleReplaceSelection(slotId: string | number) {
    if (fe.pendingFramework.value) {
      fe.handleReplace(slotId, fe.pendingFramework.value);
    }
    fe.cancelReplace();
  }

  // ==========================================================================
  // 4. RETOUR (Ordonné par domaine)
  // ==========================================================================
  return {
    // Données
    frontends: fe.availableFrontendFrameworks,
    selectedId: fe.selectedId,
    frontendFrameworkSlots: fe.frontendFrameworkSlots,
    searchQuery: fe.searchQuery,
    
    // État UI
    displayMode: fe.displayMode,
    compareMode: fe.compareMode,
    showReplacePopup: fe.showReplacePopup,
    pendingFramework: fe.pendingFramework,
    mouseX: fe.mouseX,
    mouseY: fe.mouseY,
    isLoading: fe.isLoading,
    replaceOptions,
    
    // Actions
    handleSelectWrapper,
    handleReplaceSelection,
    cancelReplace: fe.cancelReplace,
    handleModeChange: fe.handleModeChange,
    setSearch: fe.setSearch,
    setDisplayMode: fe.setDisplayMode,
    handleInfo: fe.handleInfo
  };
}