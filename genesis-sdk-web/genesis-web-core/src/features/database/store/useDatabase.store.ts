import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { DatabaseEngineDto, DatabaseConnectionTestResult } from '../types/database.types';

export const useDatabaseStore = defineStore('database', () => {
    // ═══ État ═══
    const availableEngines = ref<DatabaseEngineDto[]>([]);
    const isLoading = ref(false);
    const connectionTestResult = ref<DatabaseConnectionTestResult | null>(null);
    const displayMode = ref<'grid' | 'list'>('grid');

    // ═══ Getters ═══
    const hasEngines = computed(() => availableEngines.value.length > 0);
    
    const isConnectionSuccessful = computed(() => 
        connectionTestResult.value?.success === true
    );

    // ═══ Actions (Mutations) ═══
    
    function setAvailableEngines(data: DatabaseEngineDto[]) { 
        availableEngines.value = data; 
    }
    
    function setLoading(state: boolean) { 
        isLoading.value = state; 
    }

    function setConnectionTestResult(result: DatabaseConnectionTestResult | null) {
        connectionTestResult.value = result;
    }

    function setDisplayMode(mode: 'grid' | 'list') { displayMode.value = mode; }

    function clearConnectionTestResult() {
        connectionTestResult.value = null;
    }

    function reset() {
        availableEngines.value = [];
        isLoading.value = false;
        connectionTestResult.value = null;
    }

    return {
        // État
        availableEngines,
        displayMode,
        isLoading,
        connectionTestResult,
        
        // Getters
        hasEngines,
        isConnectionSuccessful,
        
        // Actions
        setAvailableEngines,
        setLoading,
        setConnectionTestResult,
        clearConnectionTestResult,
        reset,
        setDisplayMode
    };
});