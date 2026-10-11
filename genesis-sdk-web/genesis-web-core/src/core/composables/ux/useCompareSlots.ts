// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useCompareSlots.ts
import { ref, computed, shallowRef, type Ref, type ShallowRef } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export type SlotKey = string | number;

export interface CompareSlotsConfig<T> {
  slots: SlotKey[];
  getId: (item: T) => string | number;
}

export interface UseCompareSlotsReturn<T> {
  slots: Ref<Record<SlotKey, T | null>>;
  mode: Ref<'selection' | 'compare'>;
  selectedItem: ShallowRef<T | null>;
  compareCount: Ref<number>;
  isFull: Ref<boolean>;
  selectedItems: Ref<T[]>;
  getSlot: (item: T) => SlotKey | null;
  isSelected: (item: T) => boolean;
  handleSelect: (item: T) => { action: 'select' | 'deselect' | 'replace-needed'; item: T };
  replaceSlot: (slot: SlotKey, item: T) => void;
  removeFromSlot: (item: T) => void;
  clearAll: () => void;
  switchMode: (newMode: 'selection' | 'compare') => void;
}

// ============================================================================
// FONCTIONS UTILITAIRES INTERNES
// ============================================================================

/**
 * Génère un objet représentant les slots initialisés à null.
 * Factorisé pour éviter la duplication de logique entre l'initialisation et clearAll.
 */
function createEmptySlots<T>(slotKeys: SlotKey[]): Record<SlotKey, T | null> {
  return slotKeys.reduce((acc, key) => {
    acc[key] = null;
    return acc;
  }, {} as Record<SlotKey, T | null>);
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

export function useCompareSlots<T>(config: CompareSlotsConfig<T>): UseCompareSlotsReturn<T> {
  const { slots: slotKeys, getId } = config;

  // --- 1. État Réactif ---
  const slots = ref<Record<SlotKey, T | null>>(createEmptySlots(slotKeys));
  const mode = ref<'selection' | 'compare'>('selection');
  
  // shallowRef est préféré ici pour éviter le surcoût de réactivité profonde 
  // sur des objets génériques potentiellement complexes.
  const selectedItem = shallowRef<T | null>(null);

  // --- 2. Computeds (Données dérivées) ---
  const compareCount = computed(() => 
    Object.values(slots.value).filter(item => item !== null).length
  );
  
  const isFull = computed(() => compareCount.value === slotKeys.length);
  
  const selectedItems = computed(() => 
    Object.values(slots.value).filter(item => item !== null) as T[]
  );

  // --- 3. Actions ---

  function getSlot(item: T): SlotKey | null {
    const itemId = getId(item);
    for (const [key, value] of Object.entries(slots.value)) {
      if (value && getId(value) === itemId) {
        return key;
      }
    }
    return null;
  }

  function isSelected(item: T): boolean {
    if (mode.value === 'selection') {
      return selectedItem.value !== null && getId(selectedItem.value) === getId(item);
    }
    return getSlot(item) !== null;
  }

  function addToNextSlot(item: T): boolean {
    for (const slotKey of slotKeys) {
      if (slots.value[slotKey] === null) {
        slots.value[slotKey] = item;
        return true;
      }
    }
    return false;
  }

  function replaceSlot(slot: SlotKey, item: T): void {
    if (!slotKeys.includes(slot)) return;
    slots.value[slot] = item;
  }

  function removeFromSlot(item: T): void {
    const itemId = getId(item);
    for (const key of slotKeys) {
      if (slots.value[key] && getId(slots.value[key]!) === itemId) {
        slots.value[key] = null;
        break;
      }
    }
  }

  function clearAll(): void {
    slots.value = createEmptySlots(slotKeys);
  }

  /**
   * Gère le clic sur un élément en fonction du mode actuel.
   * En mode 'selection' : bascule l'état de selectedItem.
   * En mode 'compare' : ajoute au premier slot libre, ou signale qu'un remplacement est nécessaire.
   */
  function handleSelect(item: T): { action: 'select' | 'deselect' | 'replace-needed'; item: T } {
    if (mode.value === 'selection') {
      const wasSelected = selectedItem.value !== null && getId(selectedItem.value) === getId(item);
      selectedItem.value = wasSelected ? null : item;
      return { action: wasSelected ? 'deselect' : 'select', item };
    } 
    
    // Mode compare
    const currentSlot = getSlot(item);
    if (currentSlot !== null) {
      removeFromSlot(item);
      return { action: 'deselect', item };
    }
    
    const added = addToNextSlot(item);
    return added 
      ? { action: 'select', item } 
      : { action: 'replace-needed', item };
  }

  /**
   * Gère la transition entre le mode de sélection unique et le mode de comparaison multiple.
   * 
   * Logique de persistance :
   * - Selection -> Compare : L'élément actuellement sélectionné est placé dans le premier slot (A).
   * - Compare -> Selection : Le premier slot (A) devient l'élément sélectionné (focus). 
   *   Les autres slots (B, C, D) ne sont PAS vidés. Ils restent en mémoire pour permettre 
   *   à l'utilisateur de revenir en mode comparaison sans perdre sa configuration.
   */
  function switchMode(newMode: 'selection' | 'compare'): void {
    if (mode.value === newMode) return;
    
    if (newMode === 'compare') {
      if (selectedItem.value) {
        slots.value[slotKeys[0]] = selectedItem.value;
      } else {
        slots.value[slotKeys[0]] = null;
      }
    } else {
      selectedItem.value = slots.value[slotKeys[0]];
      // Les autres slots restent intacts intentionnellement (voir JSDoc ci-dessus)
    }

    mode.value = newMode;
  }

  // --- 4. Retour ---
  return {
    mode,
    slots,
    selectedItem,
    compareCount,
    isFull,
    selectedItems,
    getSlot,
    isSelected,
    handleSelect,
    replaceSlot,
    removeFromSlot,
    clearAll,
    switchMode
  };
}