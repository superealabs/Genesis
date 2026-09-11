import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';
import { useFrameworkStore } from '../store/useFramework.store';
import { useCompareSlots } from '@/core/composables/ux/useCompareSlots';
import { FRAMEWORK_SERVICE_KEY, type IFrameworkService } from '../types/framework.service.interface';
import type { Framework, FrameworkFilters } from '../types/framework.types';

export function useFrameworks() {
    // 1. Récupération du service via inject
    const service = inject(FRAMEWORK_SERVICE_KEY);
    
    // 2. Vérification stricte
    if (!service) {
        throw new Error('[useFrameworks] IFrameworkService non fourni. Vérifiez app.provide() dans main.ts');
    }

    const svc = service as IFrameworkService;

    const store = useFrameworkStore();
    const { filteredFrameworks, displayMode, filters, searchQuery, isLoading } = storeToRefs(store);

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

    async function initialize() {
        store.setLoading(true);
        try {
            const data = await svc.fetchFrameworks(); 
            store.setFrameworks(data);                    
        } catch (error) {
            console.error('[useFrameworks] Erreur lors du chargement:', error);
        } finally {
            store.setLoading(false);
        }
    }

    function setSearch(query: string) { store.setSearch(query); }
    function setFilters(newFilters: FrameworkFilters) { store.setFilters(newFilters); }
    function toggleDisplayMode() { store.setDisplayMode(displayMode.value === 'grid' ? 'list' : 'grid'); }
    
    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }

    async function handleSelect(framework: Framework, event?: MouseEvent) {
        const result = compare.handleSelect(framework);
        // IMPORTANT : On utilise 'svc' ici, PAS 'service'
        await svc.selectFramework(framework.id); 
        return { action: result.action, event, framework };
    }

    async function handleReplace(slotId: string | number, framework: Framework) {
        compare.replaceSlot(slotId, framework);
        //  IMPORTANT : On utilise 'svc' ici, PAS 'service'
        await svc.selectFramework(framework.id);
    }

    return {
        frameworks: filteredFrameworks,
        selectedId: currentSelectedId,
        displayMode,
        frameworkSlots: frameworkSlotsMap,
        compareMode,
        compare,
        filters,
        searchQuery,
        isLoading, // ✅ Ajouté pour que la vue puisse afficher un spinner si besoin
        initialize,
        setSearch,
        setFilters,
        toggleDisplayMode,
        handleModeChange,
        handleSelect,
        handleReplace
    };
}