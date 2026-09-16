import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { DatabaseEngineDto, DatabaseConnectionTestResult } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';


export const useDatabaseStore = defineStore('database', () => {
    // ═══ État ═══
    const availableEngines = ref<DatabaseEngineDto[]>([]);
    const isLoading = ref(false);
    const connectionTestResult = ref<DatabaseConnectionTestResult | null>(null);
    const displayMode = ref<DisplayMode>('grid');

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

    function setDisplayMode(mode: DisplayMode) { 
        displayMode.value = mode; 
    }

    function clearConnectionTestResult() {
        connectionTestResult.value = null;
    }

    function reset() {
        availableEngines.value = [];
        isLoading.value = false;
        connectionTestResult.value = null;
        displayMode.value = 'grid'; // Reset à la valeur par défaut
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