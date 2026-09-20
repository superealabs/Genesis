import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

import type { FrontendFramework } from '@genesis-labs/shared-types';
import { IFrontendService, FRONTEND_SERVICE_KEY } from '@genesis-labs/web-core/features/frontend/types/frontend.service.interface';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';
import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';

export function useFrontend() {
    const service = inject(FRONTEND_SERVICE_KEY);
    if (!service) {
        throw new Error('[useFrontend] IFrontendService non fourni. Vérifiez app.provide() dans main.ts');
    }

    const svc = service as IFrontendService;
    const store = useFrontendStore();
    
    // ✅ RENOMMÉ : availableFrameworks -> availableFrontendFrameworks
    const { 
        availableFrontendFrameworks, 
        displayMode,
        searchQuery
    } = storeToRefs(store);

    const compare = useCompareSlotsWithPopup<FrontendFramework>({
        slots: ['A', 'B', 'C', 'D'],
        getId: (fw) => fw.id
    });

    const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

    const currentSelectedId = computed(() => {
        return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
    });

    // ✅ RENOMMÉ : frameworkSlotsMap -> frontendFrameworkSlotsMap
    const frontendFrameworkSlotsMap = computed(() => {
        if (compareMode.value !== 'compare') return new Map<number, string>();
        const map = new Map<number, string>();
        for (const [slot, fw] of Object.entries(compareSlots.value)) {
            if (fw) map.set(fw.id, slot);
        }
        return map;
    });

    async function initialize() {
        try {
            const data = await svc.fetchFrontendFrameworks();
            store.setAvailableFrontendFrameworks(data);
        } catch (error) {
            console.error('[useFrontend] Erreur lors du chargement des frameworks frontend:', error);
        }
    }

    async function handleSelect(framework: FrontendFramework, event?: MouseEvent) {
        const result = compare.handleSelect(framework, event);
        if (result.action === 'pending-replace') {
            return { action: 'replace-needed' as const, event, framework };
        }
        return { action: result.action, event, framework };
    }

    async function handleReplace(slotId: string | number, framework: FrontendFramework) {
        compare.replaceSlot(slotId, framework);
    }

    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }    

    return {
        // ✅ RENOMMÉ dans le return
        availableFrontendFrameworks,
        selectedId: currentSelectedId,
        frontendFrameworkSlots: frontendFrameworkSlotsMap, // ✅ RENOMMÉ
        displayMode,
        searchQuery,
        compareMode,
        compare,
        initialize,
        handleSelect,
        handleReplace,
        handleModeChange,
        reset: store.reset,
        setSearch: store.setSearch,
        
        showReplacePopup: compare.showReplacePopup,
        pendingFramework: compare.pendingItem,
        mouseX: compare.mouseX,
        mouseY: compare.mouseY,
        cancelReplace: compare.cancelReplace,
        triggerReplace: compare.triggerReplace
    };
}