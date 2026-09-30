// genesis-sdk-web/genesis-web-core/src/core/features/config/store/useConfig.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { GenesisConfig, ConfigType } from '../types/config.service.interface';

export const useConfigStore = defineStore('config', () => {
    // ═══ 1. État (State) ═══
    const configurations = ref<GenesisConfig[]>([]);
    const selectedConfigId = ref<string | number | null>(null);
    const isLoading = ref(false);
    const error = ref<string | null>(null);

    // ═══ 2. Getters (Données dérivées) ═══
    const selectedConfig = computed(() => 
        configurations.value.find(c => c.id === selectedConfigId.value) || null
    );

    const getFilteredConfigs = computed(() => (configType?: ConfigType) => {
        if (!configType) return configurations.value;
        return configurations.value.filter(c => c.configType === configType);
    });

    // ═══ 3. Actions (Mutations) ═══
    function setConfigurations(configs: GenesisConfig[]) {
        configurations.value = configs;
    }

    function addOrUpdateConfiguration(config: GenesisConfig) {
        const index = configurations.value.findIndex(c => String(c.id) === String(config.id));
        if (index >= 0) {
            configurations.value[index] = config;
        } else {
            configurations.value.push(config);
        }
    }

    function removeConfiguration(id: string | number) {
        configurations.value = configurations.value.filter(c => c.id !== id);
        if (selectedConfigId.value === id) {
            selectedConfigId.value = configurations.value.length > 0 ? configurations.value[0].id : null;
        }
    }

    function setSelectedConfig(id: string | number | null) {
        selectedConfigId.value = id;
    }

    function setLoading(state: boolean) {
        isLoading.value = state;
    }

    function setError(message: string | null) {
        error.value = message;
    }

    function clearError() {
        error.value = null;
    }

    function reset() {
        configurations.value = [];
        selectedConfigId.value = null;
        isLoading.value = false;
        error.value = null;
    }

    // ═══ 4. Retour ═══
    return {
        // État
        configurations,
        selectedConfigId,
        isLoading,
        error,
        
        // Getters
        selectedConfig,
        getFilteredConfigs,
        
        // Actions
        setConfigurations,
        addOrUpdateConfiguration,
        removeConfiguration,
        setSelectedConfig,
        setLoading,
        setError,
        clearError,
        reset
    };
});