import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { Framework, FrameworkFilters } from '../types/framework.types';

export const useFrameworkStore = defineStore('framework', () => {
    // ═══ État ═══
    const frameworks = ref<Framework[]>([]);
    const searchQuery = ref('');
    const filters = ref<FrameworkFilters>({});
    const displayMode = ref<'grid' | 'list'>('grid');

    // ═══ Getters ═══
    const filteredFrameworks = computed(() => {
        let result = frameworks.value;

        if (searchQuery.value.trim()) {
            const q = searchQuery.value.toLowerCase();
            result = result.filter(f =>
                f.name.toLowerCase().includes(q) ||
                f.coreFramework.toLowerCase().includes(q) ||
                f.type.toLowerCase().includes(q)
            );
        }

        if (filters.value.type) result = result.filter(f => f.type === filters.value.type);
        if (filters.value.coreFramework) result = result.filter(f => f.coreFramework === filters.value.coreFramework);
        if (filters.value.isProd !== undefined) result = result.filter(f => f.isProd === filters.value.isProd);
        if (filters.value.useDB !== undefined) result = result.filter(f => f.useDB === filters.value.useDB);
        if (filters.value.useCloud !== undefined) result = result.filter(f => f.useCloud === filters.value.useCloud);
        if (filters.value.useEurekaServer !== undefined) result = result.filter(f => f.useEurekaServer === filters.value.useEurekaServer);
        if (filters.value.isGateway !== undefined) result = result.filter(f => f.isGateway === filters.value.isGateway);
        if (filters.value.useFrontendApp !== undefined) result = result.filter(f => f.useFrontendApp === filters.value.useFrontendApp);

        return result;
    });

    // ═══ Actions (Mutations) ═══
    function setFrameworks(data: Framework[]) { frameworks.value = data; }
    function setSearch(query: string) { searchQuery.value = query; }
    function setFilters(newFilters: FrameworkFilters) { filters.value = { ...filters.value, ...newFilters }; }
    function setDisplayMode(mode: 'grid' | 'list') { displayMode.value = mode; }

    return {
        frameworks, searchQuery, filters, displayMode, filteredFrameworks,
        setFrameworks, setSearch, setFilters, setDisplayMode
    };
});