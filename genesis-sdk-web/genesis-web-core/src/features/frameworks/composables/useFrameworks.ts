import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

import type { Framework, FrameworkFilters } from '@genesis-labs/shared-types';

import { FRAMEWORK_SERVICE_KEY, type IFrameworkService } from '@genesis-labs/web-core/features/frameworks/types/framework.service.interface';
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';


export function useFrameworks() {
    // 1. Récupération du service via inject
    const service = inject(FRAMEWORK_SERVICE_KEY);
    
    // 2. Vérification stricte
    if (!service) {
        throw new Error('[useFrameworks] IFrameworkService non fourni. Vérifiez app.provide() dans main.ts');
    }

    const svc = service as IFrameworkService;

    const store = useFrameworkStore();
    const { filteredFrameworks, displayMode, filters, searchQuery, isLoading, languages } = storeToRefs(store);


    const compare = useCompareSlotsWithPopup<Framework>({
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

    async function initialize() {
        store.setLoading(true);
        try {
            // 1. Récupérer les frameworks
            const frameworksData = await svc.fetchFrameworks();
            store.setFrameworks(frameworksData);
            

            const languagesData = await svc.fetchLanguages();
            store.setLanguages(languagesData);
            
        } catch (error) {
            console.error('[useFrameworks] Erreur lors du chargement:', error);
        } finally {
            store.setLoading(false);
        }
    }

    function setSearch(query: string) { store.setSearch(query); }
    function setFilters(newFilters: FrameworkFilters) { store.setFilters(newFilters); }
    
    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }

    async function handleSelect(framework: Framework, event?: MouseEvent) {
        const result = compare.handleSelect(framework, event);
        
        if (result.action === 'pending-replace') {
            return { action: 'replace-needed' as const, event, framework };
        }

        // NE JAMAIS APPELER LA FONCTION SELECTFRAMEWORK ICI(pas de communication avec api)
        return { action: result.action, event, framework };
    }

    async function handleReplace(slotId: string | number, framework: Framework) {
        compare.replaceSlot(slotId, framework);
    }

    return {
        frameworks: filteredFrameworks,
        languages,
        selectedId: currentSelectedId,
        displayMode,
        frameworkSlots: frameworkSlotsMap,
        compareMode,
        compare,
        filters,
        searchQuery,
        isLoading,
        initialize,
        setSearch,
        setFilters,
        handleModeChange,
        handleSelect,
        handleReplace,
        setDisplayMode: store.setDisplayMode,

        showReplacePopup: compare.showReplacePopup,
        pendingFramework: compare.pendingItem, // Alias pour rester cohérent avec le nom dans la vue
        mouseX: compare.mouseX,
        mouseY: compare.mouseY,
        cancelReplace: compare.cancelReplace,
        triggerReplace: compare.triggerReplace // Gardé pour le defineExpose
    };
}