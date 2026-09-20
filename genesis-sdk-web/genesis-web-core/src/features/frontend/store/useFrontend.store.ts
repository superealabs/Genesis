import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useFrontendStore = defineStore('frontend', () => {
    // ═══ État Métier ═══
    // ✅ RENOMMÉ : availableFrameworks -> availableFrontendFrameworks
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    // ✅ RENOMMÉ : selectedFramework -> selectedFrontendFramework
    const selectedFrontendFramework = ref<FrontendFramework | null>(null);

    // ═══ État UI ═══
    const displayMode = ref<DisplayMode>('grid');
    const searchQuery = ref('');

    // ═══ Getters ═══
    // ✅ RENOMMÉ
    const hasSelectedFrontendFramework = computed(() => selectedFrontendFramework.value !== null);

    // ═══ Actions Métier ═══
    // ✅ RENOMMÉ
    function setAvailableFrontendFrameworks(frameworks: FrontendFramework[]) {
        availableFrontendFrameworks.value = frameworks;
    }

    // ✅ RENOMMÉ
    function selectFrontendFramework(framework: FrontendFramework) {
        selectedFrontendFramework.value = framework;
    }

    // ═══ Actions UI ═══
    function setDisplayMode(mode: DisplayMode) {
        displayMode.value = mode;
    }

    function setSearch(query: string) {
        searchQuery.value = query;
    }

    function reset() {
        availableFrontendFrameworks.value = [];
        selectedFrontendFramework.value = null;
        displayMode.value = 'grid';
        searchQuery.value = '';
    }

    return {
        // État
        availableFrontendFrameworks,
        selectedFrontendFramework,
        displayMode,
        searchQuery,
        
        // Getters
        hasSelectedFrontendFramework,
        
        // Actions
        setAvailableFrontendFrameworks,
        selectFrontendFramework,
        setDisplayMode,
        setSearch,
        reset
    };
});