// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useReplacePopup.ts
import { ref, shallowRef, type Ref, type ShallowRef } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface UseReplacePopupReturn<T> {
  showReplacePopup: Ref<boolean>;
  pendingItem: ShallowRef<T | null>;
  mouseX: Ref<number | null>;
  mouseY: Ref<number | null>;
  triggerReplace: (item: T, event?: MouseEvent) => void;
  cancelReplace: () => void;
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

/**
 * Composable générique pour gérer l'état et le déclenchement d'un popup de remplacement.
 * 
 * @typeParam T - Le type de l'objet à remplacer (ex: Framework, DatabaseEngineDto).
 */
export function useReplacePopup<T>(): UseReplacePopupReturn<T> {
  // --- 1. État Réactif ---
  const showReplacePopup = ref(false);
  const mouseX = ref<number | null>(null);
  const mouseY = ref<number | null>(null);
  
  /**
   * Utilisation de shallowRef pour l'élément en attente.
   * Cela évite les erreurs de typage TypeScript liées à UnwrapRef<T> sur les types génériques,
   * et optimise les performances en évitant la réactivité profonde sur des objets complexes.
   */
  const pendingItem = shallowRef<T | null>(null);

  // --- 2. Actions ---

  /**
   * Déclenche l'affichage du popup et enregistre l'élément en attente.
   * Si aucun événement de souris n'est fourni, les coordonnées sont définies au centre de l'écran.
   */
  function triggerReplace(item: T, event?: MouseEvent) {
    pendingItem.value = item;
    mouseX.value = event ? event.clientX : window.innerWidth / 2;
    mouseY.value = event ? event.clientY : window.innerHeight / 2;
    showReplacePopup.value = true;
  }

  /**
   * Annule l'opération de remplacement et réinitialise complètement l'état du popup.
   */
  function cancelReplace() {
    showReplacePopup.value = false;
    pendingItem.value = null;
    mouseX.value = null;
    mouseY.value = null;
  }

  // --- 3. Retour ---
  return {
    showReplacePopup,
    pendingItem,
    mouseX,
    mouseY,
    triggerReplace,
    cancelReplace
  };
}