import { ref, computed, type Ref, type ShallowRef } from 'vue';

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

export function useCompareSlots<T>(config: CompareSlotsConfig<T>): UseCompareSlotsReturn<T> {
    const { slots: slotKeys, getId } = config;

    const slots = ref<Record<SlotKey, T | null>>(
        slotKeys.reduce((acc, key) => {
            acc[key] = null;
            return acc;
        }, {} as Record<SlotKey, T | null>)
    );

    const mode = ref<'selection' | 'compare'>('selection');
    // ShallowRef évite les problèmes de typage complexes avec les génériques Vue
    const selectedItem = ref<T | null>(null) as ShallowRef<T | null>;

    const compareCount = computed(() => Object.values(slots.value).filter(item => item !== null).length);
    const isFull = computed(() => compareCount.value === slotKeys.length);
    const selectedItems = computed(() => Object.values(slots.value).filter(item => item !== null) as T[]);

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
        slots.value = slotKeys.reduce((acc, key) => {
            acc[key] = null;
            return acc;
        }, {} as Record<SlotKey, T | null>);
    }

    function handleSelect(item: T): { action: 'select' | 'deselect' | 'replace-needed'; item: T } {
        if (mode.value === 'selection') {
            const wasSelected = selectedItem.value !== null && getId(selectedItem.value) === getId(item);
            selectedItem.value = wasSelected ? null : item;
            return { action: wasSelected ? 'deselect' : 'select', item };
        } else {
            const currentSlot = getSlot(item);
            if (currentSlot !== null) {
                removeFromSlot(item);
                return { action: 'deselect', item };
            }
            const added = addToNextSlot(item);
            if (added) {
                return { action: 'select', item };
            } else {
                return { action: 'replace-needed', item };
            }
        }
    }

    // C'est ici que la magie de la persistance opère
    function switchMode(newMode: 'selection' | 'compare'): void {
        if (mode.value === newMode) return;
        
        if (newMode === 'compare') {
            // Selection → Compare : l'élément sélectionné devient le premier slot (s'il est libre)
            if (selectedItem.value && slots.value[slotKeys[0]] === null) {
                slots.value[slotKeys[0]] = selectedItem.value;
            }
        } else {
            // Compare → Selection : le premier slot devient l'élément sélectionné (focus)
            selectedItem.value = slots.value[slotKeys[0]];
            // On ne vide PAS les autres slots, ils restent en mémoire pour un retour futur en mode compare
        }

        mode.value = newMode;
    }

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