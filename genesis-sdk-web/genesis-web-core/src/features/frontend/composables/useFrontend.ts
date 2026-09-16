import { inject, computed } from 'vue';
import { storeToRefs } from 'pinia';

import type { FrontendFramework } from '@genesis-labs/shared-types';
//  CORRECT : Chemin relatif depuis le dossier 'composables' vers le dossier 'types'
import { IFrontendService, FRONTEND_SERVICE_KEY } from '@genesis-labs/web-core/features/frontend/types/frontend.service.interface';
import { useFrontendStore } from '@genesis-labs/web-core/features/frontend/store/useFrontend.store';

import { useCompareSlotsWithPopup } from '@genesis-labs/web-core/core/composables/ux/useCompareSlotsWithPopup';


export function useFrontend() {
    // 1. Récupération sécurisée du service via inject
    const service = inject(FRONTEND_SERVICE_KEY);
    if (!service) {
        throw new Error('[useFrontend] IFrontendService non fourni. Vérifiez app.provide() dans main.ts');
    }

    // 2.  Astuce TypeScript : variable locale fortement typée pour les closures asynchrones
    const svc = service as IFrontendService;

    const store = useFrontendStore();
    
    //  Exposition réactive de TOUT l'état nécessaire à la vue
    const { 
        availableFrameworks, 
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

    const frameworkSlotsMap = computed(() => {
        if (compareMode.value !== 'compare') return new Map<number, string>();
        const map = new Map<number, string>();
        for (const [slot, fw] of Object.entries(compareSlots.value)) {
            if (fw) map.set(fw.id, slot);
        }
        return map;
    });


    /**
     * À appeler au montage du composant pour charger les données.
     *  Le composable est le SEUL à muter le store avec les données du service.
     */
    async function initialize() {
        try {
            //  On attend la Promise et on récupère les données brutes
            const data = await svc.fetchFrontendFrameworks();
            
            //  Le composable met à jour le store (pas le service !)
            store.setAvailableFrameworks(data);
        } catch (error) {
            console.error('[useFrontend] Erreur lors du chargement des frameworks:', error);
        }
    }


    async function handleSelect(framework: FrontendFramework, event?: MouseEvent) {
        const result = compare.handleSelect(framework, event);
        
        if (result.action === 'pending-replace') {
            return { action: 'replace-needed' as const, event, framework };
        }

        try {
            await svc.selectFrontendFramework(framework);
        } catch (error) {
            console.error('[useFrontend] Erreur lors de la sélection du framework:', error);
        }
        return { action: result.action, event, framework };
    }

    async function handleReplace(slotId: string | number, framework: FrontendFramework) {
        compare.replaceSlot(slotId, framework);
        try {
            await svc.selectFrontendFramework(framework);
        } catch (error) {
            console.error('[useFrontend] Erreur lors du remplacement:', error);
        }
    }

    function handleModeChange(newMode: 'selection' | 'compare') {
        compare.switchMode(newMode);
    }    


    return {
        availableFrameworks,
        selectedId: currentSelectedId,
        frameworkSlots: frameworkSlotsMap,
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
        // ✅ 4. CORRECTION DU BUG DE TOGGLE
        toggleDisplayMode: () => store.setDisplayMode(displayMode.value === 'grid' ? 'list' : 'grid'),
        
        // ✅ 5. ÉTATS DU POPUP POUR LA VUE
        showReplacePopup: compare.showReplacePopup,
        pendingFramework: compare.pendingItem,
        mouseX: compare.mouseX,
        mouseY: compare.mouseY,
        cancelReplace: compare.cancelReplace,
        triggerReplace: compare.triggerReplace
    };
}