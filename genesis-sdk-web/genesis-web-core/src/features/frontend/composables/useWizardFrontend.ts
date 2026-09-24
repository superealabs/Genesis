import { computed } from 'vue';
import { useFrontend } from './useFrontend';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';


export function useWizardFrontend(onSelect: (framework: FrontendFramework, event?: MouseEvent) => void) {
    const fe = useFrontend();

    const replaceOptions = computed<SelectionOption[]>(() => {
        if (!fe.compare?.slots?.value) return [];
        return Object.entries(fe.compare.slots.value)
            .filter(([, fw]) => fw !== null)
            .map(([slot, fw]) => ({
                id: slot,
                label: `Slot ${slot}`,
                description: (fw as FrontendFramework).name
            }));
    });

    async function handleSelectWrapper(framework: FrontendFramework, event?: MouseEvent) {
        try {
            await fe.handleSelect(framework, event);
            onSelect(framework, event);
        } catch (error) {
            console.error("❌ [WizardFrontend] Erreur lors de la sélection :", error);
        }
    }

    function handleReplaceSelection(slotId: string | number) {
        if (fe.pendingFramework.value) {
            fe.handleReplace(slotId, fe.pendingFramework.value);
        }
        fe.cancelReplace();
    }

    return {
        // Données réactives
        frontends: fe.availableFrontendFrameworks,
        selectedId: fe.selectedId,
        frontendFrameworkSlots: fe.frontendFrameworkSlots,
        displayMode: fe.displayMode,
        searchQuery: fe.searchQuery,
        compareMode: fe.compareMode,
        showReplacePopup: fe.showReplacePopup,
        pendingFramework: fe.pendingFramework,
        mouseX: fe.mouseX,
        mouseY: fe.mouseY,
        isLoading: fe.isLoading,
        replaceOptions,
        
        // Actions
        handleSelectWrapper,
        handleReplaceSelection,
        cancelReplace: fe.cancelReplace,
        handleModeChange: fe.handleModeChange,
        setSearch: fe.setSearch,
        setDisplayMode: fe.setDisplayMode,
        handleInfo: fe.handleInfo
    };
}