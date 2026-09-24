import { ref, computed } from 'vue';
import { useFrameworks } from './useFrameworks';
import type { Framework } from '@genesis-labs/shared-types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

export function useWizardFramework(onSelect: (framework: Framework, event?: MouseEvent) => void) {
    const fw = useFrameworks();

    // États UI locaux
    const isFilterOpen = ref(false);
    const detailFramework = ref<Framework | null>(null);

    const replaceOptions = computed<SelectionOption[]>(() => {
        if (!fw.compare?.slots?.value) return [];
        return Object.entries(fw.compare.slots.value)
            .filter(([, framework]) => framework !== null)
            .map(([slot, framework]) => ({
                id: slot,
                label: `Slot ${slot}`,
                description: (framework as Framework).name
            }));
    });

    async function handleSelectWrapper(framework: Framework, event?: MouseEvent) {
        try {
            await fw.handleSelect(framework, event);
            onSelect(framework, event);
        } catch (error) {
            console.error("❌ [WizardFramework] Erreur lors de la sélection :", error);
        }
    }

    function handleReplaceSelection(slotId: string | number) {
        if (fw.pendingFramework.value) {
            fw.handleReplace(slotId, fw.pendingFramework.value);
        }
        fw.cancelReplace();
    }

    return {
        // Retourne les refs directement (Vue les déballera dans le template)
        frameworks: fw.frameworks,
        languages: fw.languages,
        selectedId: fw.selectedId,
        displayMode: fw.displayMode,
        compareMode: fw.compareMode,
        frameworkSlots: fw.frameworkSlots,
        filters: fw.filters,
        searchQuery: fw.searchQuery,
        isLoading: fw.isLoading,
        showReplacePopup: fw.showReplacePopup,
        pendingFramework: fw.pendingFramework,
        mouseX: fw.mouseX,
        mouseY: fw.mouseY,

        // États UI locaux
        isFilterOpen,
        detailFramework,
        replaceOptions,

        // Actions
        initialize: fw.initialize,
        setSearch: fw.setSearch,
        setFilters: fw.setFilters, // ✅ AJOUTÉ
        setDisplayMode: fw.setDisplayMode,
        handleModeChange: fw.handleModeChange,
        handleSelectWrapper,
        handleReplaceSelection,
        cancelReplace: fw.cancelReplace,
        triggerReplace: fw.triggerReplace,
        
        // Actions UI locales
        openFilter: () => { isFilterOpen.value = true; },
        closeFilter: () => { isFilterOpen.value = false; },
        closeDetail: () => { detailFramework.value = null; },
        handleInfo: (fwItem: Framework) => { detailFramework.value = fwItem; }
    };
}