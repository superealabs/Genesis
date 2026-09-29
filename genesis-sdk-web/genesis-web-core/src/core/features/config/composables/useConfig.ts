// genesis-sdk-web/genesis-web-core/src/core/features/config/composables/useConfig.ts
import { inject } from 'vue';
import { storeToRefs } from 'pinia';
import { CONFIG_SERVICE_KEY, IConfigService, type ConfigType, type GenesisConfig } from '../types/config.service.interface';
import { useConfigStore } from '../store/useConfig.store';

/**
 * Composable pour la gestion des configurations.
 * 
 * Ce composable est sans état : il délègue la persistance au service injecté 
 * et la réactivité au store Pinia.
 */
export function useConfig() {
    // 1. Injection du service (doit être fourni par le composant racine ou main.ts)
    const configService = inject(CONFIG_SERVICE_KEY);
    if (!configService) {
        throw new Error('[useConfig] IConfigService non fourni. Vérifiez le provide() dans l\'application.');
    }

    const svc = configService as IConfigService;

    // 2. Accès au store
    const store = useConfigStore();
    const { configurations, selectedConfigId, isLoading, error, selectedConfig, getFilteredConfigs } = storeToRefs(store);

    // 3. Actions orchestrées (Service + Store)
    
    async function loadConfigurations(configType?: ConfigType) {
        store.setLoading(true);
        store.clearError();
        try {
            const data = await svc.getAll(configType);
            // Si on charge un type spécifique, on pourrait vouloir fusionner ou remplacer. 
            // Ici, on remplace pour simplifier, ou on filtre côté store.
            store.setConfigurations(data);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors du chargement des configurations.');
        } finally {
            store.setLoading(false);
        }
    }

    async function saveConfiguration(config: GenesisConfig) {
        store.setLoading(true);
        store.clearError();
        try {
            await svc.save(config);
            store.addOrUpdateConfiguration(config);
            store.setSelectedConfig(config.id);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de la sauvegarde.');
        } finally {
            store.setLoading(false);
        }
    }

    async function deleteConfiguration(id: string | number) {
        store.setLoading(true);
        store.clearError();
        try {
            await svc.delete(id);
            store.removeConfiguration(id);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de la suppression.');
        } finally {
            store.setLoading(false);
        }
    }

    async function importConfiguration() {
        store.setLoading(true);
        store.clearError();
        try {
            const result = await svc.import();
            if (result) {
                const configsToImport = Array.isArray(result) ? result : [result];
                configsToImport.forEach(c => store.addOrUpdateConfiguration(c));
            }
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de l\'import.');
        } finally {
            store.setLoading(false);
        }
    }

    async function exportConfiguration(config: GenesisConfig) {
        store.setLoading(true);
        store.clearError();
        try {
            await svc.export(config);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de l\'export.');
        } finally {
            store.setLoading(false);
        }
    }

    async function exportAllConfigurations(configs: GenesisConfig[]) {
        store.setLoading(true);
        store.clearError();
        try {
            await svc.exportAll(configs);
        } catch (err) {
            store.setError((err as Error).message || 'Erreur lors de l\'export global.');
        } finally {
            store.setLoading(false);
        }
    }

    // 4. Retour
    return {
        // État réactif (depuis le store)
        configurations,
        selectedConfigId,
        selectedConfig,
        getFilteredConfigs,
        isLoading,
        error,
        
        // Actions
        loadConfigurations,
        saveConfiguration,
        deleteConfiguration,
        importConfiguration,
        exportConfiguration,
        exportAllConfigurations,
        setSelectedConfig: store.setSelectedConfig,
        clearError: store.clearError
    };
}