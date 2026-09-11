import { storeToRefs } from 'pinia';
import { computed } from 'vue';
import { useFrameworkStore } from '../store/useFramework.store';
import { frameworkService } from '../services/framework.service';
import { useCompareSlots } from '@/core/composables/ux/useCompareSlots';
import type { Framework, FrameworkFilters } from '../types/framework.types';

export function useFrameworks() {
    const store = useFrameworkStore();
    
    // ✅ EXPOSITION DE TOUS LES ÉTATS NÉCESSAIRES
    const { filteredFrameworks, displayMode, filters, searchQuery } = storeToRefs(store);

    const compare = useCompareSlots<Framework>({
        slots: ['A', 'B', 'C', 'D'],
        getId: (f) => f.id
    });

    const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

    const currentSelectedId = computed(() => {
        return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
    });

    const frameworkSlotsMap = computed(() => {
        if (compareMode.value !== 'compare') return new Map<number, string>();
        const map = new Map<number, string>();
        for (const [slot, framework] of Object.entries(compareSlots.value)) {
            if (framework) map.set(framework.id, slot);
        }
        return map;
    });

    function initialize() {
        frameworkService.init();
        frameworkService.fetchFrameworks();
    }

    function setSearch(query: string) { store.setSearch(query); }
    function setFilters(newFilters: FrameworkFilters) { store.setFilters(newFilters); }
    function toggleDisplayMode() { store.setDisplayMode(displayMode.value === 'grid' ? 'grid' : 'list'); }
    
    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }

    function handleSelect(framework: Framework, event?: MouseEvent) {
        const result = compare.handleSelect(framework);
        frameworkService.selectFramework(framework.id);
        return { action: result.action, event, framework };
    }

    function handleReplace(slotId: string | number, framework: Framework) {
        compare.replaceSlot(slotId, framework);
        frameworkService.selectFramework(framework.id);
    }

    return {
        // État (readonly)
        frameworks: filteredFrameworks,
        selectedId: currentSelectedId,
        displayMode,
        frameworkSlots: frameworkSlotsMap,
        compareMode,
        compare,
        
        filters,
        searchQuery,
        
        // Actions
        initialize,
        setSearch,
        setFilters,
        toggleDisplayMode,
        handleModeChange,
        handleSelect,
        handleReplace
    };
}