// genesis-sdk-web/genesis-web-core/src/features/database/composables/useWizardDatabase.ts
import { computed } from 'vue';

// Types
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

// Composables
import { useDatabase } from './useDatabase';

/**
 * Composable adaptateur pour intégrer la logique des bases de données dans le Wizard.
 * 
 * Il fait le pont entre useDatabase (logique métier et état) et l'interface utilisateur 
 * du Stepper, en formatant les données et les événements spécifiquement pour ce contexte.
 */
export function useWizardDatabase(onSelect: (engine: DatabaseEngineDto, event?: MouseEvent) => void) {
  // ==========================================================================
  // 1. COMPOSABLE DE BASE
  // ==========================================================================
  const db = useDatabase();

  // ==========================================================================
  // 2. COMPUTEDS LOCAUX (Adaptation pour l'UI du Wizard)
  // ==========================================================================
  
  /**
   * Transforme les slots de comparaison actifs en une liste d'options 
   * exploitables par le popup de remplacement du Wizard.
   */
  const replaceOptions = computed<SelectionOption[]>(() => {
    if (!db.compare?.slots?.value) return [];
    
    return Object.entries(db.compare.slots.value)
      .filter(([, engine]) => engine !== null)
      .map(([slot, engine]) => ({
        id: slot,
        label: `Slot ${slot}`,
        description: (engine as DatabaseEngineDto).name
      }));
  });

  // ==========================================================================
  // 3. ACTIONS WRAPPERS (Adaptation pour le Stepper)
  // ==========================================================================

  /**
   * Wrapper autour de la sélection pour intercepter le résultat, 
   * gérer les erreurs potentielles et déclencher le callback du parent.
   */
  async function handleSelectWrapper(engine: DatabaseEngineDto, event?: MouseEvent) {
    try {
      await db.handleSelect(engine, event);
      onSelect(engine, event);
    } catch (error) {
      console.error("[WizardDatabase] Erreur lors de la sélection :", error);
    }
  }

  /**
   * Gère le remplacement d'un moteur dans un slot de comparaison, 
   * puis annule l'état d'attente du popup.
   */
  function handleReplaceSelection(slotId: string | number) {
    if (db.pendingEngine.value) {
      db.handleReplace(slotId, db.pendingEngine.value);
    }
    db.cancelReplace();
  }

  // ==========================================================================
  // 4. RETOUR (Ordonné par domaine)
  // ==========================================================================
  return {
    // Données
    engines: db.engines,
    selectedId: db.selectedId,
    databaseSlots: db.databaseSlots,
    
    // État UI
    displayMode: db.displayMode,
    compareMode: db.compareMode,
    showReplacePopup: db.showReplacePopup,
    pendingEngine: db.pendingEngine,
    mouseX: db.mouseX,
    mouseY: db.mouseY,
    isLoading: db.isLoading,
    replaceOptions,
    
    // Actions
    handleSelectWrapper,
    handleReplaceSelection,
    cancelReplace: db.cancelReplace,
    handleModeChange: db.handleModeChange,
    setDisplayMode: db.setDisplayMode
  };
}