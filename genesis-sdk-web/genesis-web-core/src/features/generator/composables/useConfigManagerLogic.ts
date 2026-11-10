// genesis-sdk-web/genesis-web-core/src/features/generator/composables/useConfigManagerLogic.ts
import { watch, type Ref } from 'vue';
import type { GenesisConfig, ConfigType } from '@genesis-labs/shared-types';
import type { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useGeneratorStore } from '../store/useGenerator.store';

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
  framework: Ref<{ id: number; name: string; coreFramework?: string } | null | undefined>;
  saveConfiguration: (config: GenesisConfig<TPayload>) => Promise<void>;
  deleteConfiguration: (id: string | number) => Promise<void>;
  persistedConfigs: Ref<GenesisConfig<any>[]>;
  /**
   * Inverse de `buildPayload` : extrait les valeurs d'un payload importé, dans le MÊME ordre que `getCurrentValues`.
   * Requis pour `handleImport` (sans lui, l'import est indisponible).
   */
  extractValues?: (payload: TPayload) => string[];
  onImported?: () => void | Promise<void>;
}

// ============================================================================
// HELPERS D'IMPORT
// ============================================================================

const isRecord = (v: unknown): v is Record<string, unknown> =>
  typeof v === 'object' && v !== null && !Array.isArray(v);

/** Ouvre le sélecteur de fichier JSON ; renvoie le fichier choisi (null si annulé). */
function pickJsonFile(): Promise<File | null> {
  return new Promise((resolve) => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = '.json,application/json';
    input.onchange = () => resolve(input.files?.[0] ?? null);
    input.oncancel = () => resolve(null);
    input.click();
  });
}

/**
 * Rend un nom unique parmi ceux déjà pris : "Nom" → "Nom-copy" → "Nom-copy-2"…
 * (comparaison insensible à la casse).
 */
function uniqueName(name: string, taken: Set<string>): string {
  const isTaken = (n: string) => taken.has(n.toLowerCase());
  if (!isTaken(name)) return name;

  let candidate = `${name}-copy`;
  for (let i = 2; isTaken(candidate); i++) candidate = `${name}-copy-${i}`;
  return candidate;
}

/** Libellés lisibles des types de configuration (messages d'erreur). */
const CONFIG_TYPE_LABELS: Record<string, string> = {
  'framework_technical-stack': 'Stack technique',
  'framework_advanced-configuration': 'Configuration avancée',
  'generator_generation-options': 'Options de génération',
  'frontend_languages-configuration': 'Langues du frontend',
};

/** « Stack technique » (framework_technical-stack) — ou juste « type » s'il est inconnu. */
const typeLabel = (type: string): string =>
  CONFIG_TYPE_LABELS[type] ? `« ${CONFIG_TYPE_LABELS[type]} »` : `« ${type} »`;

/** Nom d'un élément du fichier pour les messages : « Prod », ou "élément n°2" s'il n'a pas de nom. */
function describeItem(item: unknown, index: number): string {
  return isRecord(item) && typeof item.name === 'string' && item.name.trim()
    ? `« ${item.name.trim()} »`
    : `élément n°${index + 1}`;
}

/** Nombre maximum de problèmes détaillés dans le message d'import (le reste est résumé). */
const MAX_ISSUE_LINES = 5;

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
    persistedConfigs,
    extractValues,
    onImported
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
    const name = newName.trim();
    if (!name) return;
    manager.renameConfiguration(id, name);
    const existing = persistedConfigs.value.find(c => String(c.id) === String(id));
    if (existing && framework.value) {
      await saveConfiguration({
        ...existing,
        name,
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

  /**
   * Importe un fichier JSON produit par handleExport (une configuration) ou handleExportAll (un tableau).
   *
   * Règles :
   * - seules les configurations du même `configType` sont importées ;
   * - si le payload est lié à un framework (frameworkId / frameworkName), il doit correspondre au framework courant ;
   * - chaque import crée une NOUVELLE configuration (jamais d'écrasement), re-liée au framework courant via `buildPayload` ;
   * - un nom déjà pris reçoit le suffixe "-copy" ;
   * - chaque configuration ignorée est détaillée dans le message d'erreur (nom + raison).
   */
  const handleImport = async () => {
    const store = useGeneratorStore();

    if (!extractValues) {
      store.setWizardError(`L'import n'est pas disponible pour les configurations ${typeLabel(configType)}.`);
      return;
    }

    const file = await pickJsonFile();
    if (!file) return;

    let data: unknown;
    try {
      data = JSON.parse(await file.text());
    } catch {
      store.setWizardError(
        `Le fichier « ${file.name} » n'est pas un JSON valide.\n`
        + `Type attendu : ${typeLabel(configType)} (fichier exporté depuis ce panneau).`
      );
      return;
    }

    const items: unknown[] = Array.isArray(data) ? data : [data];
    if (items.length === 0) {
      store.setWizardError(`Le fichier « ${file.name} » ne contient aucune configuration.`);
      return;
    }

    const issues: string[] = [];
    const skip = (item: unknown, index: number, reason: string) =>
      issues.push(`${describeItem(item, index)} : ${reason}`);
    const fw = framework.value;
    let imported = 0;

    for (const [index, item] of items.entries()) {
      if (!isRecord(item)) {
        skip(item, index, "ce n'est pas une configuration (objet JSON attendu)");
        continue;
      }
      if (typeof item.configType !== 'string') {
        skip(item, index, 'champ « configType » manquant');
        continue;
      }
      if (item.configType !== configType) {
        skip(item, index, `type ${typeLabel(item.configType)} — incompatible avec ${typeLabel(configType)}`);
        continue;
      }
      if (!isRecord(item.payload)) {
        skip(item, index, 'champ « payload » manquant ou invalide');
        continue;
      }

      const payload = item.payload;
      // APRÈS
      const isFrameworkBound = 'frameworkId' in payload || 'frameworkName' in payload;
      const sharedByCore = typeof payload.coreFramework === 'string';
      const matchesFramework = sharedByCore
        ? !!fw && payload.coreFramework === fw.coreFramework
        : !!fw && (payload.frameworkId === fw.id || payload.frameworkName === fw.name);

      if (isFrameworkBound && !matchesFramework) {
        const source = sharedByCore
          ? payload.coreFramework
          : typeof payload.frameworkName === 'string' ? payload.frameworkName : `id ${String(payload.frameworkId)}`;
        const current = sharedByCore ? fw?.coreFramework : fw?.name;
        skip(item, index, fw
          ? `créée pour ${sharedByCore ? 'le core framework' : 'le framework'} « ${source} », alors que ${sharedByCore ? 'le core framework courant' : 'le framework courant'} est « ${current} »`
          : `liée au ${sharedByCore ? 'core framework' : 'framework'} « ${source} », mais aucun framework n'est sélectionné`);
        continue;
      }

      let values: unknown[];
      try {
        values = extractValues(payload as TPayload);
      } catch (error) {
        skip(item, index, `payload illisible (${error instanceof Error ? error.message : 'erreur inconnue'})`);
        continue;
      }
      if (!Array.isArray(values)) {
        skip(item, index, 'payload illisible');
        continue;
      }
      const invalidCount = values.filter(v => typeof v !== 'string').length;
      if (invalidCount > 0) {
        skip(item, index, `${invalidCount} valeur(s) sur ${values.length} manquante(s) ou invalide(s) dans le payload`);
        continue;
      }

      // Noms pris AVANT l'ajout (sinon le nom par défaut de la nouvelle config en ferait partie)
      const takenNames = new Set(manager.configurations.value.map(c => c.name.toLowerCase()));

      const created = manager.addConfiguration(values as string[]);
      const wantedName = typeof item.name === 'string' && item.name.trim() ? item.name.trim() : created.name;
      const name = uniqueName(wantedName, takenNames);
      if (name !== created.name) manager.renameConfiguration(created.id, name);

      try {
        await saveConfiguration({
          id: String(created.id),
          name,
          configType,
          schemaVersion: '1.0.0',
          createdAt: new Date().toISOString(),
          payload: buildPayload(values as string[])
        } as GenesisConfig<TPayload>);
        imported++;
      } catch (error) {
        manager.deleteConfiguration(created.id); // pas de configuration "fantôme" non persistée
        skip(item, index, `échec de la sauvegarde (${error instanceof Error ? error.message : 'erreur inconnue'})`);
      }
    }

    if (issues.length > 0) {
        const hidden = issues.length - MAX_ISSUE_LINES;

        const headline = imported === 0
            ? 'Import échoué'
            : 'Import partiel';

        const total = imported + issues.length;
        const summary =
            `${imported === 0
                ? 'Aucune configuration importée'
                : `${imported} configuration${imported > 1 ? 's' : ''} importée${imported > 1 ? 's' : ''}`
            } sur ${total} (${issues.length} ignorée${issues.length > 1 ? 's' : ''}) — « ${file.name} »`;

        const details: string[] = [
            `Type attendu : ${typeLabel(configType)}`,
            ...issues.slice(0, MAX_ISSUE_LINES),
        ];
        if (hidden > 0) {
            details.push(`… et ${hidden} autre${hidden > 1 ? 's' : ''} problème${hidden > 1 ? 's' : ''}.`);
        }

        store.setWizardError({ title: headline, summary, details });
    }

    if (imported > 0) {
      await onImported?.();
    }
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
  }, { deep:true });

  return {
    handleSelect,
    handleAdd,
    handleRename,
    handleDelete,
    handleExport,
    handleExportAll,
    handleImport
  };
}