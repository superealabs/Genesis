import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { FrontendFramework } from '../types/frontend.types';

export const useFrontendStore = defineStore('frontend', () => {
    // ═══ État ═══
    const availableFrameworks = ref<FrontendFramework[]>([]);
    const selectedFramework = ref<FrontendFramework | null>(null);

    // ═══ Getters ═══
    const hasSelectedFramework = computed(() => selectedFramework.value !== null);

    // ═══ Actions ═══
    function setAvailableFrameworks(frameworks: FrontendFramework[]) {
        availableFrameworks.value = frameworks;
    }

    function selectFramework(framework: FrontendFramework) {
        selectedFramework.value = framework;
    }

    function reset() {
        availableFrameworks.value = [];
        selectedFramework.value = null;
    }

    return {
        availableFrameworks,
        selectedFramework,
        hasSelectedFramework,
        setAvailableFrameworks,
        selectFramework,
        reset
    };
});