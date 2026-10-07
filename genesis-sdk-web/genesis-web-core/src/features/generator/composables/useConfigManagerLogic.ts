// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useConfigManagerLogic.ts
import { watch, type Ref } from 'vue';
import type { GenesisConfig, ConfigType } from '@genesis-labs/shared-types';
import type { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';

export interface ConfigLogicParams<TPayload = any> {
  manager: ReturnType<typeof useConfigurationManager>;
  configType: ConfigType;
  defaultValues: string[];
  getCurrentValues: () => string[];
  buildPayload: (values?: string[]) => TPayload;
  fileNamePrefix: string;
  watchSource: () => (string | undefined)[];
  applyValues: (values: string[]) => void;
  isApplyingRef: Ref<boolean>;
  framework: Ref<{ id: number; name: string } | null | undefined>;
  saveConfiguration: (config: GenesisConfig<TPayload>) => Promise<void>;
  deleteConfiguration: (id: string | number) => Promise<void>;
  persistedConfigs: Ref<GenesisConfig<any>[]>;
}

/**
 * Composable DRY pour gérer la logique répétitive des panneaux de configuration 
 * (Ajout, Renommage, Suppression, Export, Auto-save).
 */
export function useConfigManagerLogic<TPayload = any>(params: ConfigLogicParams<TPayload>) {
  const {
    manager,
    configType,
    defaultValues,
    getCurrentValues,
    buildPayload,
    fileNamePrefix,
    watchSource,
    applyValues,
    isApplyingRef,
    framework,
    saveConfiguration,
    deleteConfiguration,
    persistedConfigs
  } = params;

  const handleSelect = (id: string | number | null) => {
    if (manager.selectedConfigId.value === id) {
      // Cas 1 : On clique sur la config déjà sélectionnée -> On désélectionne
      manager.selectConfiguration(null);
      applyValues(defaultValues);
    } else {
      // Cas 2 : On sélectionne une NOUVELLE config
      manager.selectConfiguration(id);
      
      // ✅ CORRECTION : On récupère la config et on applique ses valeurs aux dropdowns
      const selectedConfig = manager.configurations.value.find(
        c => String(c.id) === String(id)
      );
      
      if (selectedConfig) {
        applyValues(selectedConfig.components);
      } else {
        // Fallback de sécurité si la config n'est pas trouvée
        applyValues(defaultValues);
      }
    }
  };

  const handleAdd = async () => {
    let valuesToUse = manager.selectedConfigId.value
      ? (manager.configurations.value.find(c => c.id === manager.selectedConfigId.value)?.components ?? [...defaultValues])
      : getCurrentValues();

    const newConfig = manager.addConfiguration(valuesToUse);
    await saveConfiguration({
      id: String(newConfig.id),
      name: newConfig.name,
      configType,
      schemaVersion: '1.0.0',
      createdAt: new Date().toISOString(),
      payload: buildPayload(valuesToUse)
    } as GenesisConfig<TPayload>);
    manager.searchQuery.value = '';
  };

  const handleRename = async (id: string | number, newName: string) => {
    manager.renameConfiguration(id, newName);
    const existing = persistedConfigs.value.find(c => String(c.id) === String(id));
    if (existing && framework.value) {
      await saveConfiguration({
        ...existing,
        name: newName,
        payload: {
          ...(existing.payload as any),
          frameworkId: framework.value.id,
          frameworkName: framework.value.name
        }
      } as GenesisConfig<TPayload>);
    }
  };

  const handleDelete = async (id: string | number) => {
    manager.deleteConfiguration(id);
    await deleteConfiguration(String(id));
    if (!manager.selectedConfigId.value) {
      applyValues(defaultValues);
    }
  };

  const handleExport = async () => {
    const cfg = persistedConfigs.value.find(c => c.id === manager.selectedConfigId.value);
    if (cfg) {
      const blob = new Blob([JSON.stringify(cfg, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${cfg.name.replace(/\s+/g, '_')}_${fileNamePrefix}.json`;
      document.body.appendChild(a);
      a.click();
      document.body.removeChild(a);
      URL.revokeObjectURL(url);
    }
  };

  const handleExportAll = async () => {
    const cfgs = persistedConfigs.value.filter(c => c.configType === configType);
    const blob = new Blob([JSON.stringify(cfgs, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `configurations_${fileNamePrefix}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  // Watcher pour l'auto-save
  watch(watchSource, async (newValues, oldValues) => {
    if (isApplyingRef.value || (oldValues && JSON.stringify(newValues) === JSON.stringify(oldValues))) return;
    
    const selectedId = manager.selectedConfigId.value;
    if (selectedId && framework.value) {
      manager.editConfiguration(selectedId, newValues as string[]);
      const existing = persistedConfigs.value.find(c => String(c.id) === String(selectedId));
      if (existing) {
        await saveConfiguration({
          ...existing,
          payload: buildPayload()
        } as GenesisConfig<TPayload>);
      }
    }
  });

  return {
    handleSelect,
    handleAdd,
    handleRename,
    handleDelete,
    handleExport,
    handleExportAll
  };
}