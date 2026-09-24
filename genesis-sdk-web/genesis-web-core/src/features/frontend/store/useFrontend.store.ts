import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework, InterfaceLanguage } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useFrontendStore = defineStore('frontend', () => {
    // ═══ État Métier ═══
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    const selectedFrontendFramework = ref<FrontendFramework | null>(null);
    const availableInterfaceLanguages = ref<InterfaceLanguage[]>([]);

    // ═══ État UI ═══
    const displayMode = ref<DisplayMode>('grid');
    const searchQuery = ref('');
    const isLoading = ref(false); // ✅ AJOUT : Pour gérer le spinner de chargement

    // ═══ Getters ═══
    const hasSelectedFrontendFramework = computed(() => selectedFrontendFramework.value !== null);
    const getAvailableInterfaceLanguages = computed(() => availableInterfaceLanguages.value);

    // ═══ Actions Métier ═══
    function setAvailableFrontendFrameworks(frameworks: FrontendFramework[]) {
        availableFrontendFrameworks.value = frameworks;
    }

    function selectFrontendFramework(framework: FrontendFramework) {
        selectedFrontendFramework.value = framework;
    }

    function setAvailableInterfaceLanguages(languages: InterfaceLanguage[]) {
        availableInterfaceLanguages.value = languages;
    }

    // ═══ Actions UI ═══
    function setDisplayMode(mode: DisplayMode) {
        displayMode.value = mode;
    }

    function setSearch(query: string) {
        searchQuery.value = query;
    }

    function setLoading(state: boolean) { // ✅ AJOUT
        isLoading.value = state;
    }

    function reset() {
        availableFrontendFrameworks.value = [];
        selectedFrontendFramework.value = null;
        availableInterfaceLanguages.value = [];
        displayMode.value = 'grid';
        searchQuery.value = '';
        isLoading.value = false; // ✅ AJOUT
    }

    return {
        // État
        availableFrontendFrameworks,
        selectedFrontendFramework,
        availableInterfaceLanguages,
        displayMode,
        searchQuery,
        isLoading, // ✅ AJOUT
        
        // Getters
        hasSelectedFrontendFramework,
        getAvailableInterfaceLanguages,
        
        // Actions
        setAvailableFrontendFrameworks,
        selectFrontendFramework,
        setAvailableInterfaceLanguages,
        setDisplayMode,
        setSearch,
        setLoading, // ✅ AJOUT
        reset
    };
});