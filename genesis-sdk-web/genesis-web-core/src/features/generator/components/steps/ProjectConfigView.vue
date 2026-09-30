<template>
  <div class="p-6 space-y-6">
    
    <!-- 1. CONFIGURATION DE BASE -->
    <div class="space-y-4">
      <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
        Configuration du Projet
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput 
          v-model="config.projectName" 
          variant="secondary" 
          label="Nom du projet" 
          placeholder="mon-super-projet"
          size="lg"
          is-mandatory
          fill-width
        />
        <GenesisInput
          v-model="config.projectPort"
          type="number"
          variant="secondary"
          size="lg"
          label="Port d'exécution"
          placeholder="ex: 8080"
          fill-width
        />
      </div>

      <div class="w-full">
        <GenesisInput 
          v-model="config.projectDescription" 
          variant="secondary" 
          label="Description du projet"
          size="lg"
          placeholder="Une brève description de l'application..."
          type="textarea"
          fill-width
        />
      </div>

      <div class="w-full">
        <GenesisInput
          v-model="config.projectLocation"
          type="path"
          variant="secondary"
          label="Emplacement"
          fill-width
          size="lg"
          @request-folder-path="handleSelectFolderPath"
        />
      </div>
    </div>

    <div class="border-t border-secondary"></div>

    <!-- 2. CONFIGURATION DU FRAMEWORK -->
    <div class="space-y-4">
      <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
        Stack Technique ({{ framework?.name || 'Non sélectionné' }})
      </h3>
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <GenesisInput
          v-model="config.languageVersion"
          type="select"
          variant="secondary"
          label="Version du Language"
          placeholder="Sélectionner..."
          fill-width
        >
          <template #default="{ close }">
            <div class="p-1 space-y-1">
              <button
                v-for="v in availableLanguageVersions"
                :key="v"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.languageVersion === v }"
                @click="() => { updateConfig('languageVersion', v); close(); }"
              >
                {{ v }}
              </button>
            </div>
          </template>
        </GenesisInput>

        <GenesisInput
          v-model="config.buildTool"
          type="select"
          variant="secondary"
          label="Build Tool"
          placeholder="Sélectionner..."
          fill-width
        >
          <template #default="{ close }">
            <div class="p-1 space-y-1">
              <button
                v-for="tool in availableBuildTools"
                :key="tool"
                type="button"
                class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                :class="{ 'text-accent font-medium': config.buildTool === tool }"
                @click="() => { updateConfig('buildTool', tool); close(); }"
              >
                {{ tool.charAt(0).toUpperCase() + tool.slice(1) }}
              </button>
            </div>
          </template>
        </GenesisInput>

        <div v-if="showGroupId" class="space-y-1">
          <GenesisInput 
            v-model="config.groupId"
            variant="secondary"
            label="Group ID"
            placeholder="com.example"
            fill-width
          />
        </div>

        <div class="space-y-1">
          <GenesisInput 
            v-model="config.frameworkVersion"
            type="select"
            variant="secondary"
            label="Version du Framework"
            placeholder="Sélectionner..."
            fill-width
          >
            <template #default="{ close }">
              <div class="p-1 space-y-1">
                <button
                  v-for="v in availableFrameworkVersions"
                  :key="v"
                  type="button"
                  class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                  :class="{ 'text-accent font-medium': config.frameworkVersion === v }"
                  @click="() => { updateConfig('frameworkVersion', v); close(); }"
                >
                  {{ v }}
                </button>
              </div>
            </template>
          </GenesisInput>
        </div>
      </div>
    </div>

    <div class="border-t border-secondary"></div>

    <!-- 3. CONFIGURATION AVANCÉE -->
    <div class="space-y-4">
      <GenesisDisclosure 
        title="Configuration Avancée du Backend" 
        :default-open="false"
        variant="secondary"
      >
        <div class="flex flex-col lg:flex-row gap-6">
          
          <!-- ═══ COLONNE GAUCHE : INPUTS DE CONFIGURATION ═══ -->
          <div class="flex-1 flex flex-col gap-4">
            <GenesisInput
              v-model="config.loggingLevel"
              type="select"
              variant="secondary"
              label="Niveau de Logging"
              placeholder="INFO"
              fill-width
            >
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button
                    v-for="opt in availableLoggingLevels"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.loggingLevel === opt }"
                    @click="() => { updateConfig('loggingLevel', opt); close(); }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
            </GenesisInput>

            <GenesisInput
              v-model="config.securityType"
              type="select"
              variant="secondary"
              label="Type de Sécurité"
              placeholder="Aucune"
              fill-width
            >
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': !config.securityType || config.securityType === 'NONE' }"
                    @click="() => { updateConfig('securityType', 'NONE'); close(); }"
                  >
                    Aucune
                  </button>
                  <button
                    v-for="opt in availableSecurityTypes"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.securityType === opt }"
                    @click="() => { updateConfig('securityType', opt); close(); }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
            </GenesisInput>

            <GenesisInput
              v-model="config.cacheProvider"
              type="select"
              variant="secondary"
              label="Fournisseur de Cache"
              placeholder="Aucun"
              fill-width
            >
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button
                    v-for="opt in availableCacheProviders"
                    :key="opt"
                    type="button"
                    class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                    :class="{ 'text-accent font-medium': config.cacheProvider === opt }"
                    @click="() => { updateConfig('cacheProvider', opt); close(); }"
                  >
                    {{ opt }}
                  </button>
                </div>
              </template>
            </GenesisInput>

            <div v-if="showHibernateDdl" class="space-y-1">
              <GenesisInput
                v-model="config.hibernateDdlAuto"
                type="select"
                variant="secondary"
                label="Hibernate DDL Auto"
                placeholder="none"
                fill-width
              >
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button
                      v-for="opt in availableHibernateDdlAutoOptions"
                      :key="opt"
                      type="button"
                      class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                      :class="{ 'text-accent font-medium': config.hibernateDdlAuto === opt }"
                      @click="() => { updateConfig('hibernateDdlAuto', opt); close(); }"
                    >
                      {{ opt }}
                    </button>
                  </div>
                </template>
              </GenesisInput>
            </div>
          </div>

          <!-- ═══ COLONNE DROITE : PANNEAU ET ACTIONS ═══ -->
          <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
            
            <GenesisConfigurationPanel
              :configurations="configManager.configurations.value"
              :selected-config-id="configManager.selectedConfigId.value"
              :filtered-configs="configManager.filteredConfigs.value"
              :search-query="configManager.searchQuery.value"
              :can-move-up="configManager.canMoveUp.value"
              :can-move-down="configManager.canMoveDown.value"
              @update:search-query="(val) => configManager.searchQuery.value = val"
              @add="handleAddConfig"
              @delete="handleDeleteConfig"
              @rename="handleRenameConfig"
              @toggle-visibility="configManager.toggleVisibility"
              @move-up="configManager.moveUp"
              @move-down="configManager.moveDown"
              @select-configuration="handleSelectConfig"
            />

            <div class="flex gap-2 justify-end">
              <GenesisButtonIcon 
                variant="secondary" 
                title="Exporter la configuration sélectionnée"
                :disabled="!configManager.selectedConfigId.value"
                @click="handleExportConfig"
              >
                <IconUpload />
              </GenesisButtonIcon>

              <GenesisButtonIcon 
                variant="secondary" 
                title="Exporter toutes les configurations"
                @click="handleExportAllConfigs"
              >
                <IconSave />
              </GenesisButtonIcon>
            </div>

          </div>
        </div>
      </GenesisDisclosure>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, watch, ref, onMounted, nextTick } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';
import type { GenesisConfig, FrameworkAdvancedConfigurationPayload } from '@genesis-labs/shared-types';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';

import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';

// ============================================================================
// 1. EMITS & CONSTANTES
// ============================================================================
const emit = defineEmits<{
  'request-folder-path': [];
}>();

const DEFAULT_VALUES = ['INFO', 'NONE', 'Aucun', 'none'];

// Flag pour éviter les faux positifs de l'auto-save lors du chargement d'une config
const isApplyingConfig = ref(false);

// ============================================================================
// 2. COMPOSABLES
// ============================================================================
const { 
  stepperData,
  updateConfig,
  availableLoggingLevels,
  availableSecurityTypes,
  availableCacheProviders,
  availableLanguageVersions,    
  availableFrameworkVersions,   
  availableBuildTools,
  availableHibernateDdlAutoOptions,
  fetchLoggingLevels, 
  fetchSecurityTypes,
  fetchCacheProviders,
  fetchLanguageVersions,        
  fetchFrameworkVersions,
  fetchBuildTools,
  fetchHibernateDdlAutoOptions
} = useGenerator();

const { 
  loadConfigurations, 
  saveConfiguration, 
  deleteConfiguration,
  configurations: persistedConfigs 
} = useConfig();

const configManager = useConfigurationManager([], { singleConfiguration: false });

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
const config = computed(() => stepperData.value.config);
const framework = computed(() => stepperData.value.framework);

const showGroupId = computed(() => framework.value?.withGroupId === true);
const showHibernateDdl = computed(() => framework.value?.withHibernateDdlAuto === true);

// ============================================================================
// 4. ACTIONS
// ============================================================================

function handleSelectFolderPath() {
  emit('request-folder-path');
}

function getCurrentConfigValues(): string[] {
  return [
    config.value.loggingLevel || DEFAULT_VALUES[0],
    config.value.securityType || DEFAULT_VALUES[1],
    config.value.cacheProvider || DEFAULT_VALUES[2],
    config.value.hibernateDdlAuto || DEFAULT_VALUES[3]
  ];
}

function applyConfigValues(values: string[]) {
  isApplyingConfig.value = true;
  updateConfig('loggingLevel', values[0]);
  updateConfig('securityType', values[1]);
  updateConfig('cacheProvider', values[2]);
  updateConfig('hibernateDdlAuto', values[3]);
  
  nextTick(() => {
    isApplyingConfig.value = false;
  });
}

async function handleAddConfig() {
  let valuesToUse: string[];
  
  if (configManager.selectedConfigId.value) {
    const selected = configManager.configurations.value.find(c => c.id === configManager.selectedConfigId.value);
    valuesToUse = selected ? [...selected.components] : [...DEFAULT_VALUES];
  } else {
    valuesToUse = getCurrentConfigValues();
  }

  const newConfig = configManager.addConfiguration(valuesToUse);

  const genesisConfig: GenesisConfig<FrameworkAdvancedConfigurationPayload> = {
    id: String(newConfig.id),
    name: newConfig.name,
    configType: 'framework_advanced-configuration',
    schemaVersion: '1.0.0',
    createdAt: new Date().toISOString(),
    payload: {
      loggingLevel: valuesToUse[0],
      securityType: valuesToUse[1],
      cacheProvider: valuesToUse[2],
      hibernateDdlAuto: valuesToUse[3]
    }
  };
  
  await saveConfiguration(genesisConfig);
  configManager.searchQuery.value = '';
}

function handleSelectConfig(id: string | number) {
  if (configManager.selectedConfigId.value === id) {
    configManager.selectConfiguration(null);
    applyConfigValues(DEFAULT_VALUES);
  } else {
    configManager.selectConfiguration(id);
  }
}

/**
 * RENOMMAGE : Met à jour l'UI et sauvegarde immédiatement.
 */
async function handleRenameConfig(id: string | number, newName: string) {
  // 1. Mise à jour de l'UI locale
  configManager.renameConfiguration(id, newName);

  // 2. Récupération de la config existante dans le store de persistance
  const existingConfig = persistedConfigs.value.find(c => String(c.id) === String(id));
  
  if (existingConfig) {
    // 3. Création de l'objet mis à jour
    const updatedConfig = {
      ...existingConfig,
      name: newName
    } as GenesisConfig<FrameworkAdvancedConfigurationPayload>;

    try {
      console.log("=== DÉBUT DU DEBUG DE RENAME ===");
      
      // Valeur 1 : La config MAJ telle qu'elle est envoyée à la sauvegarde
      console.log("1. Config MAJ à sauvegarder (updatedConfig) :", JSON.parse(JSON.stringify(updatedConfig)));
      
      // Appel au service de sauvegarde
      await saveConfiguration(updatedConfig); 
      
      // Valeur 2 : La même config récupérée par son ID APRÈS la sauvegarde
      const configAfterSave = persistedConfigs.value.find(c => String(c.id) === String(id));
      console.log("2. Config récupérée par ID après save :", JSON.parse(JSON.stringify(configAfterSave)));
      
      console.log("=== FIN DU DEBUG DE RENAME ===");
      
    } catch (error) {
      console.error("❌ Erreur de sauvegarde lors du rename :", error);
    }
  } else {
    console.warn("⚠️ Configuration non trouvée dans persistedConfigs pour l'ID :", id);
  }
}

async function handleDeleteConfig(id: string | number) {
  configManager.deleteConfiguration(id);
  await deleteConfiguration(String(id));

  if (!configManager.selectedConfigId.value) {
    applyConfigValues(DEFAULT_VALUES);
  }
}

async function handleExportConfig() {
  const configToExport = persistedConfigs.value.find(c => c.id === configManager.selectedConfigId.value);
  if (configToExport) {
    const blob = new Blob([JSON.stringify(configToExport, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${configToExport.name.replace(/\s+/g, '_')}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
}

async function handleExportAllConfigs() {
  const blob = new Blob([JSON.stringify(persistedConfigs.value, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'toutes_les_configurations.json';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// ============================================================================
// 5. LIFECYCLE & WATCHERS
// ============================================================================

onMounted(async () => {
  await loadConfigurations('framework_advanced-configuration');
  
  const uiConfigs = persistedConfigs.value.map(c => {
    const payload = c.payload as unknown as FrameworkAdvancedConfigurationPayload;
    return {
      id: c.id,
      name: c.name,
      isHidden: false,
      components: [
        payload.loggingLevel || DEFAULT_VALUES[0],
        payload.securityType || DEFAULT_VALUES[1],
        payload.cacheProvider || DEFAULT_VALUES[2],
        payload.hibernateDdlAuto || DEFAULT_VALUES[3]
      ]
    };
  });

  configManager.configurations.value = uiConfigs;
});

watch(
  () => framework.value?.id,
  async (newId) => {
    if (newId && framework.value) {
      await Promise.all([
        fetchBuildTools(newId),
        fetchLanguageVersions(framework.value.languageId),
        fetchFrameworkVersions(newId),
        fetchLoggingLevels(newId),
        fetchSecurityTypes(newId),
        fetchCacheProviders(newId),
        fetchHibernateDdlAutoOptions(newId)
      ]);
    }
  },
  { immediate: true }
);

// Watcher principal : Charge les valeurs dans l'UI dès que la sélection change
watch(
  () => configManager.selectedConfigId.value,
  (newId) => {
    if (newId) {
      const configToLoad = configManager.configurations.value.find(c => c.id === newId);
      if (configToLoad) {
        applyConfigValues(configToLoad.components);
      }
    }
  }
);

// Watcher d'auto-save : Déclenche la sauvegarde dès qu'un dropdown change (si une config est sélectionnée)
watch(
  () => [
    config.value.loggingLevel,
    config.value.securityType,
    config.value.cacheProvider,
    config.value.hibernateDdlAuto
  ],
  async (newValues, oldValues) => {
    // Ignore les changements provoqués par applyConfigValues (chargement d'une config)
    if (isApplyingConfig.value) return;
    
    // Ignore si aucune valeur n'a réellement changé
    if (oldValues && JSON.stringify(newValues) === JSON.stringify(oldValues)) return;

    const selectedId = configManager.selectedConfigId.value;
    if (selectedId) {
      const values = newValues as string[];
      
      // Mise à jour de l'UI locale
      configManager.editConfiguration(selectedId, values);
      
      // Sauvegarde Backend
      const existingConfig = persistedConfigs.value.find(c => String(c.id) === String(selectedId));
      if (existingConfig) {
        const updatedConfig: GenesisConfig<FrameworkAdvancedConfigurationPayload> = {
          ...existingConfig,
          payload: {
            loggingLevel: values[0],
            securityType: values[1],
            cacheProvider: values[2],
            hibernateDdlAuto: values[3]
          }
        };
        await saveConfiguration(updatedConfig);
      }
    }
  }
);
</script>