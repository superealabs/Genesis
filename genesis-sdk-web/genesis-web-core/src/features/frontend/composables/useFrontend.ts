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
    
    const { 
        availableFrontendFrameworks, 
        displayMode,
        searchQuery,
        isLoading // ✅ AJOUT
    } = storeToRefs(store);

    const compare = useCompareSlotsWithPopup<FrontendFramework>({
        slots: ['A', 'B', 'C', 'D'],
        getId: (fw) => fw.id
    });

    const { mode: compareMode, slots: compareSlots, selectedItem } = compare;

    const currentSelectedId = computed(() => {
        return compareMode.value === 'selection' ? selectedItem.value?.id : undefined;
    });

    const frontendFrameworkSlotsMap = computed(() => {
        if (compareMode.value !== 'compare') return new Map<number, string>();
        const map = new Map<number, string>();
        for (const [slot, fw] of Object.entries(compareSlots.value)) {
            if (fw) map.set(fw.id, slot);
        }
        return map;
    });

    async function initialize() {
        store.setLoading(true); // ✅ AJOUT
        try {
            const data = await svc.fetchFrontendFrameworks();
            store.setAvailableFrontendFrameworks(data);
        } catch (error) {
            console.error('[useFrontend] Erreur lors du chargement des frameworks frontend:', error);
        } finally {
            store.setLoading(false); // ✅ AJOUT
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

    // ✅ AJOUT : Pour satisfaire l'interface du Wizard (même si c'est un placeholder pour l'instant)
    function handleInfo(framework: FrontendFramework) {
        console.log("Détails demandés pour :", framework.name);
    }

    return {
        availableFrontendFrameworks,
        selectedId: currentSelectedId,
        frontendFrameworkSlots: frontendFrameworkSlotsMap,
        displayMode,
        searchQuery,
        isLoading, // ✅ AJOUT
        compareMode,
        compare,
        initialize,
        handleSelect,
        handleReplace,
        handleModeChange,
        handleInfo, // ✅ AJOUT
        reset: store.reset,
        setSearch: store.setSearch,
        setDisplayMode: store.setDisplayMode, // ✅ AJOUT
        
        showReplacePopup: compare.showReplacePopup,
        pendingFramework: compare.pendingItem,
        mouseX: compare.mouseX,
        mouseY: compare.mouseY,
        cancelReplace: compare.cancelReplace,
        triggerReplace: compare.triggerReplace
    };
}