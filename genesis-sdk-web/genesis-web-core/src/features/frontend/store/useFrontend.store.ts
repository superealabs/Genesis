// genesis-sdk-web/genesis-web-core/src/features/frontend/store/useFrontend.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework, InterfaceLanguage } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

export const useFrontendStore = defineStore('frontend', () => {
    // ═══ État Métier ═══
    const availableFrontendFrameworks = ref<FrontendFramework[]>([]);
    const selectedFrontendFramework = ref<FrontendFramework | null>(null);
    
    // ✅ AJOUT : Source de vérité pour les langues d'interface
    const availableInterfaceLanguages = ref<InterfaceLanguage[]>([]);

    // ═══ État UI ═══
    const displayMode = ref<DisplayMode>('grid');
    const searchQuery = ref('');

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

    // ✅ AJOUT : Action pour les langues d'interface
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

    function reset() {
        availableFrontendFrameworks.value = [];
        selectedFrontendFramework.value = null;
        availableInterfaceLanguages.value = []; // ✅ AJOUT
        displayMode.value = 'grid';
        searchQuery.value = '';
    }

    return {
        // État
        availableFrontendFrameworks,
        selectedFrontendFramework,
        availableInterfaceLanguages, // ✅ AJOUT
        displayMode,
        searchQuery,
        
        // Getters
        hasSelectedFrontendFramework,
        getAvailableInterfaceLanguages, // ✅ AJOUT
        
        // Actions
        setAvailableFrontendFrameworks,
        selectFrontendFramework,
        setAvailableInterfaceLanguages, // ✅ AJOUT
        setDisplayMode,
        setSearch,
        reset
    };
});