// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useCompareSlotsWithPopup.ts
import { useCompareSlots, type CompareSlotsConfig } from './useCompareSlots';
import { useReplacePopup } from './useReplacePopup';

/**
 * Compose la logique de comparaison par slots avec un popup de remplacement.
 * 
 * Ce composable étend useCompareSlots en interceptant l'action 'replace-needed'.
 * Lorsque tous les slots sont occupés, il déclenche automatiquement le popup 
 * via useReplacePopup, offrant une expérience utilisateur fluide sans rejeter 
 * silencieusement l'action de l'utilisateur.
 */
export function useCompareSlotsWithPopup<T>(config: CompareSlotsConfig<T>) {
  // 1. Initialisation des composables de base
  const compare = useCompareSlots(config);
  const popup = useReplacePopup<T>();

  // 2. Surcharge de la méthode pour intégrer les deux logiques
  /**
   * Gère la sélection d'un élément et déclenche le popup de remplacement 
   * si aucun slot n'est disponible.
   */
  function handleSelect(item: T, event?: MouseEvent) {
    const result = compare.handleSelect(item);

    if (result.action === 'replace-needed') {
      popup.triggerReplace(item, event);
      return { action: 'pending-replace' as const, item, event };
    }

    return { ...result, event };
  }

  // 3. Retour combiné (le type est inféré naturellement par TypeScript)
  return {
    ...compare,
    ...popup,
    handleSelect
  };
}