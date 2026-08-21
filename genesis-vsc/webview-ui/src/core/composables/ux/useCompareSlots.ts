import { ref, shallowRef, computed, type Ref, type ShallowRef, type ComputedRef } from 'vue';

export type SlotKey = string | number;

export interface CompareSlotsConfig<T> {
    slots: SlotKey[];
    getId: (item: T) => string | number;
}

export interface UseCompareSlotsReturn<T> {
    // État (réactif)
    slots: Ref<Record<SlotKey, T | null>>;
    mode: Ref<'selection' | 'compare'>;
    selectedItem: ShallowRef<T | null>;  // ← ShallowRef au lieu de Ref

    // Computed (réactif)
    compareCount: ComputedRef<number>;
    isFull: ComputedRef<boolean>;
    selectedItems: ComputedRef<T[]>;

    // Méthodes
    getSlot: (item: T) => SlotKey | null;
    isSelected: (item: T) => boolean;
    handleSelect: (item: T) => {
        action: 'select' | 'deselect' | 'replace-needed';
        item: T;
    };
    replaceSlot: (slot: SlotKey, item: T) => void;
    removeFromSlot: (item: T) => void;
    clearAll: () => void;
    switchMode: (newMode: 'selection' | 'compare') => void;
}

export function useCompareSlots<T>(config: CompareSlotsConfig<T>): UseCompareSlotsReturn<T> {
    const { slots: slotKeys, getId } = config;

    // Initialiser les slots à null
    const slots = ref<Record<SlotKey, T | null>>(
        slotKeys.reduce((acc, key) => {
            acc[key] = null;
            return acc;
        }, {} as Record<SlotKey, T | null>)
    );

    const mode = ref<'selection' | 'compare'>('selection');

    // shallowRef : pas de deep reactivity, typage plus simple pour T générique
    const selectedItem = shallowRef<T | null>(null);

    const compareCount = computed(() => {
        return Object.values(slots.value).filter(item => item !== null).length;
    });

    const isFull = computed(() => compareCount.value === slotKeys.length);

    const selectedItems = computed(() => {
        return Object.values(slots.value).filter(item => item !== null) as T[];
    });

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
        if (!slotKeys.includes(slot)) {
            console.warn(`Slot "${slot}" n'existe pas dans la configuration`);
            return;
        }
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

    function handleSelect(item: T): {
        action: 'select' | 'deselect' | 'replace-needed';
        item: T;
    } {
        if (mode.value === 'selection') {
            const wasSelected = selectedItem.value !== null && getId(selectedItem.value) === getId(item);
            selectedItem.value = wasSelected ? null : item;
            return {
                action: wasSelected ? 'deselect' : 'select',
                item
            };
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

    function switchMode(newMode: 'selection' | 'compare'): void {
        if (mode.value === newMode) return;

        if (newMode === 'compare') {
            if (selectedItem.value && slots.value[slotKeys[0]] === null) {
                slots.value[slotKeys[0]] = selectedItem.value;
            }
        } else {
            selectedItem.value = slots.value[slotKeys[0]];
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