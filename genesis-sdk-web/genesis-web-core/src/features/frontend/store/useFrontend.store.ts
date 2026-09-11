import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework } from '../types/frontend.types';

export const useFrontendStore = defineStore('frontend', () => {
    // ═══ État Métier ═══
    const availableFrameworks = ref<FrontendFramework[]>([]);
    const selectedFramework = ref<FrontendFramework | null>(null);

    // ═══ État UI (Nécessaire pour l'autonomie de la vue) ═══
    const displayMode = ref<'grid' | 'list'>('grid');
    const searchQuery = ref('');

    // ═══ Getters ═══
    const hasSelectedFramework = computed(() => selectedFramework.value !== null);

    // ═══ Actions Métier ═══
    function setAvailableFrameworks(frameworks: FrontendFramework[]) {
        availableFrameworks.value = frameworks;
    }

    function selectFramework(framework: FrontendFramework) {
        selectedFramework.value = framework;
    }

    // ═══ Actions UI ═══
    function setDisplayMode(mode: 'grid' | 'list') {
        displayMode.value = mode;
    }

    function setSearch(query: string) {
        searchQuery.value = query;
    }

    function reset() {
        availableFrameworks.value = [];
        selectedFramework.value = null;
        displayMode.value = 'grid';
        searchQuery.value = '';
    }

    return {
        // État
        availableFrameworks,
        selectedFramework,
        displayMode,
        searchQuery,
        
        // Getters
        hasSelectedFramework,
        
        // Actions
        setAvailableFrameworks,
        selectFramework,
        setDisplayMode,
        setSearch,
        reset
    };
});