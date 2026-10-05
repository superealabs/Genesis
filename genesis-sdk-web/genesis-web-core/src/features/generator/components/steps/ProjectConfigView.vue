<template>
  <main class="flex gap-4 h-full overflow-hidden">
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. CONFIGURATION DE BASE -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Configuration du Projet
        </h3>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput 
            v-model="config.projectName" 
            
            label="Nom du projet" 
            placeholder="mon-super-projet"
            size="lg"
            is-mandatory
            fill-width
          />
          <GenesisInput
            v-model="config.projectPort"
            type="number"
            
            size="lg"
            label="Port d'exécution"
            placeholder="ex: 8080"
            fill-width
          />
        </div>

        <div class="w-full">
          <GenesisInput 
            v-model="config.projectDescription" 
            
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
            
            label="Emplacement"
            fill-width
            size="lg"
            @request-folder-path="handleSelectFolderPath"
          />
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 2. CONFIGURATION DU FRAMEWORK (STACK TECHNIQUE) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Stack Technique ({{ framework?.name || 'Non sélectionné' }})
        </h3>
        
        <div class="flex flex-row justify-between">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput
              v-model="config.languageVersion"
              type="select"
              :size="'lg'"
              
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
              :size="'lg'"
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
                :size="'lg'"
                label="Group ID"
                placeholder="com.example"
                fill-width
              />
            </div>

            <div class="space-y-1">
              <GenesisInput 
                v-model="config.frameworkVersion"
                type="select"
                :size="'lg'"
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

          <!-- Panneau de gestion des configurations Stack Technique -->
          <div class="mt-4 pt-4 border-t border-secondary/50">
            <div class="flex flex-col lg:flex-row gap-6">
              <div class="flex-1"></div> <!-- Spacer pour aligner avec la colonne de droite -->
              
              <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
                <GenesisConfigurationPanel
                  :configurations="techStackConfigManager.configurations.value"
                  :selected-config-id="techStackConfigManager.selectedConfigId.value"
                  :filtered-configs="techStackConfigManager.filteredConfigs.value"
                  :search-query="techStackConfigManager.searchQuery.value"
                  :can-move-up="techStackConfigManager.canMoveUp.value"
                  :can-move-down="techStackConfigManager.canMoveDown.value"
                  @update:search-query="(val) => techStackConfigManager.searchQuery.value = val"
                  @add="handleAddTechStackConfig"
                  @delete="handleDeleteTechStackConfig"
                  @rename="handleRenameTechStackConfig"
                  @toggle-visibility="techStackConfigManager.toggleVisibility"
                  @move-up="techStackConfigManager.moveUp"
                  @move-down="techStackConfigManager.moveDown"
                  @select-configuration="handleSelectTechStackConfig"
                />
                
                <div class="flex gap-2 justify-end">
                  <GenesisButtonIcon 
                    
                    title="Exporter la configuration stack technique sélectionnée"
                    :disabled="!techStackConfigManager.selectedConfigId.value"
                    @click="handleExportTechStackConfig"
                  >
                    <IconUpload />
                  </GenesisButtonIcon>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 3. CONFIGURATION AVANCÉE -->
      <div class="space-y-4">
        <GenesisDisclosure 
          title="Configuration Avancée du Backend" 
          :default-open="false"
          
        >
          <div class="flex flex-col lg:flex-row gap-6">
            
            <!-- COLONNE GAUCHE : INPUTS DE CONFIGURATION -->
            <div class="flex-1 flex flex-col gap-4">
              <GenesisInput
                v-model="config.loggingLevel"
                type="select"
                
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

            <!-- COLONNE DROITE : PANNEAU ET ACTIONS -->
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
                  
                  title="Exporter la configuration avancée sélectionnée"
                  :disabled="!configManager.selectedConfigId.value"
                  @click="handleExportConfig"
                >
                  <IconUpload />
                </GenesisButtonIcon>

                <GenesisButtonIcon 
                  
                  title="Exporter toutes les configurations avancées"
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
    <div class="w-1/2 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>
  </main>
</template>

<script setup lang="ts">
import { computed, watch, ref, onMounted, nextTick } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';
import type { GenesisConfig, FrameworkAdvancedConfigurationPayload, FrameworkTechnicalStackPayload } from '@genesis-labs/shared-types';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';

import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';

import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';


import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Slide 1' },
  { color: '#EF4444', label: 'Slide 2' },
  { color: '#10B981', label: 'Slide 3' },
];
// ============================================================================
// 1. EMITS & CONSTANTES
// ============================================================================
const emit = defineEmits<{
  'request-folder-path': [];
}>();

const DEFAULT_VALUES_ADVANCED = ['INFO', 'NONE', 'Aucun', 'none'];
const DEFAULT_VALUES_TECH = ['', 'maven', 'com.example', ''];

const isApplyingConfig = ref(false);
const isApplyingTechStackConfig = ref(false);

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
  loadAdvancedConfigurations,
  loadTechnicalStackConfigurations,  
  saveConfiguration, 
  deleteConfiguration,
  configurations: persistedConfigs 
} = useConfig();

const configManager = useConfigurationManager([], { singleConfiguration: false });
const techStackConfigManager = useConfigurationManager([], { singleConfiguration: false });

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
const config = computed(() => stepperData.value.config);
const framework = computed(() => stepperData.value.framework);

const showGroupId = computed(() => framework.value?.withGroupId === true);
const showHibernateDdl = computed(() => framework.value?.withHibernateDdlAuto === true);

// ============================================================================
// 4. ACTIONS : CONFIGURATION AVANCÉE
// ============================================================================

function handleSelectFolderPath() {
  emit('request-folder-path');
}

function getCurrentConfigValues(): string[] {
  return [
    config.value.loggingLevel || DEFAULT_VALUES_ADVANCED[0],
    config.value.securityType || DEFAULT_VALUES_ADVANCED[1],
    config.value.cacheProvider || DEFAULT_VALUES_ADVANCED[2],
    config.value.hibernateDdlAuto || DEFAULT_VALUES_ADVANCED[3]
  ];
}

function applyConfigValues(values: string[]) {
  isApplyingConfig.value = true;
  updateConfig('loggingLevel', values[0]);
  updateConfig('securityType', values[1]);
  updateConfig('cacheProvider', values[2]);
  updateConfig('hibernateDdlAuto', values[3]);
  nextTick(() => { isApplyingConfig.value = false; });
}

async function handleAddConfig() {
  let valuesToUse: string[];
  if (configManager.selectedConfigId.value) {
    const selected = configManager.configurations.value.find(c => c.id === configManager.selectedConfigId.value);
    valuesToUse = selected ? [...selected.components] : [...DEFAULT_VALUES_ADVANCED];
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
      frameworkId: framework.value?.id || 0,
      frameworkName: framework.value?.name || 'Inconnu',
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
    applyConfigValues(DEFAULT_VALUES_ADVANCED);
  } else {
    configManager.selectConfiguration(id);
  }
}

async function handleRenameConfig(id: string | number, newName: string) {
  configManager.renameConfiguration(id, newName);
  const existingConfig = persistedConfigs.value.find(c => String(c.id) === String(id));
  
  if (existingConfig && framework.value) {
    const payload = existingConfig.payload as unknown as FrameworkAdvancedConfigurationPayload;
    const updatedConfig = {
      id: existingConfig.id,
      name: newName,
      configType: existingConfig.configType,
      schemaVersion: existingConfig.schemaVersion,
      createdAt: existingConfig.createdAt,
      updatedAt: existingConfig.updatedAt,
      payload: {
        frameworkId: framework.value.id,        
        frameworkName: framework.value.name,    
        loggingLevel: payload.loggingLevel,
        securityType: payload.securityType,
        cacheProvider: payload.cacheProvider,
        hibernateDdlAuto: payload.hibernateDdlAuto
      }
    };
    await saveConfiguration(updatedConfig as GenesisConfig<FrameworkAdvancedConfigurationPayload>);
  }
}

async function handleDeleteConfig(id: string | number) {
  configManager.deleteConfiguration(id);
  await deleteConfiguration(String(id));
  if (!configManager.selectedConfigId.value) {
    applyConfigValues(DEFAULT_VALUES_ADVANCED);
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
  // Exporte uniquement les configs avancées pour ce framework (ou toutes si tu préfères)
  const advancedConfigs = persistedConfigs.value.filter(c => c.configType === 'framework_advanced-configuration');
  const blob = new Blob([JSON.stringify(advancedConfigs, null, 2)], { type: 'application/json' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'configurations_avancees.json';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(url);
}

// ============================================================================
// 5. ACTIONS : STACK TECHNIQUE
// ============================================================================

function getTechStackValues(): string[] {
  return [
    config.value.languageVersion || DEFAULT_VALUES_TECH[0],
    config.value.buildTool || DEFAULT_VALUES_TECH[1],
    config.value.groupId || DEFAULT_VALUES_TECH[2],
    config.value.frameworkVersion || DEFAULT_VALUES_TECH[3]
  ];
}

function applyTechStackValues(values: string[]) {
  isApplyingTechStackConfig.value = true;
  updateConfig('languageVersion', values[0]);
  updateConfig('buildTool', values[1]);
  updateConfig('groupId', values[2]);
  updateConfig('frameworkVersion', values[3]);
  nextTick(() => { isApplyingTechStackConfig.value = false; });
}

async function handleAddTechStackConfig() {
  let valuesToUse: string[];
  if (techStackConfigManager.selectedConfigId.value) {
    const selected = techStackConfigManager.configurations.value.find(c => c.id === techStackConfigManager.selectedConfigId.value);
    valuesToUse = selected ? [...selected.components] : [...DEFAULT_VALUES_TECH];
  } else {
    valuesToUse = getTechStackValues();
  }

  const newConfig = techStackConfigManager.addConfiguration(valuesToUse);
  const genesisConfig: GenesisConfig<FrameworkTechnicalStackPayload> = {
    id: String(newConfig.id),
    name: newConfig.name,
    configType: 'framework_technical-stack',
    schemaVersion: '1.0.0',
    createdAt: new Date().toISOString(),
    payload: {
      frameworkId: framework.value?.id || 0,
      frameworkName: framework.value?.name || 'Inconnu',
      languageVersion: valuesToUse[0],
      buildTool: valuesToUse[1],
      groupId: valuesToUse[2],
      frameworkVersion: valuesToUse[3]
    }
  };
  
  await saveConfiguration(genesisConfig);
  techStackConfigManager.searchQuery.value = '';
}

function handleSelectTechStackConfig(id: string | number) {
  if (techStackConfigManager.selectedConfigId.value === id) {
    techStackConfigManager.selectConfiguration(null);
    applyTechStackValues(getTechStackValues());
  } else {
    techStackConfigManager.selectConfiguration(id);
  }
}

async function handleRenameTechStackConfig(id: string | number, newName: string) {
  techStackConfigManager.renameConfiguration(id, newName);
  
  const localConfig = techStackConfigManager.configurations.value.find(c => c.id === id);
  if (localConfig && framework.value) {
    const updatedConfig = {
      id: localConfig.id,
      name: newName,
      configType: 'framework_technical-stack',
      schemaVersion: '1.0.0',
      createdAt: new Date().toISOString(),
      payload: {
        frameworkId: framework.value.id,
        frameworkName: framework.value.name,
        languageVersion: localConfig.components[0],
        buildTool: localConfig.components[1],
        groupId: localConfig.components[2],
        frameworkVersion: localConfig.components[3]
      }
    };
    await saveConfiguration(updatedConfig as GenesisConfig<FrameworkTechnicalStackPayload>);
  }
}

async function handleDeleteTechStackConfig(id: string | number) {
  techStackConfigManager.deleteConfiguration(id);
  await deleteConfiguration(String(id));
  if (!techStackConfigManager.selectedConfigId.value) {
    applyTechStackValues(DEFAULT_VALUES_TECH);
  }
}

async function handleExportTechStackConfig() {
  const configToExport = persistedConfigs.value.find(c => c.id === techStackConfigManager.selectedConfigId.value);
  if (configToExport) {
    const blob = new Blob([JSON.stringify(configToExport, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${configToExport.name.replace(/\s+/g, '_')}_tech_stack.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  }
}

// ============================================================================
// 6. LIFECYCLE & WATCHERS
// ============================================================================

async function syncConfigsToCurrentFramework() {
  if (!framework.value) return;
  
  const fwId = framework.value.id;
  const fwName = framework.value.name;

  // 1. Synchroniser Configuration Avancée
  const advancedData = await loadAdvancedConfigurations(fwId, fwName);
  configManager.configurations.value = advancedData.map(c => {
    const p = c.payload as unknown as FrameworkAdvancedConfigurationPayload;
    return {
      id: c.id, name: c.name, isHidden: false,
      components: [p.loggingLevel, p.securityType, p.cacheProvider, p.hibernateDdlAuto]
    };
  });
  configManager.selectConfiguration(null);
  applyConfigValues(DEFAULT_VALUES_ADVANCED);

  // 2. Synchroniser Stack Technique
  const techStackData = await loadTechnicalStackConfigurations(fwId, fwName);
  techStackConfigManager.configurations.value = techStackData.map(c => {
    const p = c.payload as unknown as FrameworkTechnicalStackPayload;
    return {
      id: c.id, name: c.name, isHidden: false,
      components: [p.languageVersion, p.buildTool, p.groupId, p.frameworkVersion]
    };
  });
  techStackConfigManager.selectConfiguration(null);
  applyTechStackValues(DEFAULT_VALUES_TECH);
}

onMounted(async () => {
  await syncConfigsToCurrentFramework();
});

watch(
  () => framework.value?.id,
  async (newId, oldId) => {
    if (newId && newId !== oldId) {
      await syncConfigsToCurrentFramework();
    }
  }
);

// Watcher : Chargement des valeurs Advanced lors de la sélection
watch(
  () => configManager.selectedConfigId.value,
  (newId) => {
    if (newId) {
      const configToLoad = configManager.configurations.value.find(c => c.id === newId);
      if (configToLoad) applyConfigValues(configToLoad.components);
    }
  }
);

// Watcher : Auto-save Advanced
watch(
  () => [config.value.loggingLevel, config.value.securityType, config.value.cacheProvider, config.value.hibernateDdlAuto],
  async (newValues, oldValues) => {
    if (isApplyingConfig.value) return;
    if (oldValues && JSON.stringify(newValues) === JSON.stringify(oldValues)) return;

    const selectedId = configManager.selectedConfigId.value;
    if (selectedId && framework.value) {
      const values = newValues as string[];
      configManager.editConfiguration(selectedId, values);
      
      const existingConfig = persistedConfigs.value.find(c => String(c.id) === String(selectedId));
      if (existingConfig) {
        const updatedConfig = {
          id: existingConfig.id,
          name: existingConfig.name,
          configType: existingConfig.configType,
          schemaVersion: existingConfig.schemaVersion,
          createdAt: existingConfig.createdAt,
          updatedAt: existingConfig.updatedAt,
          payload: {
            frameworkId: framework.value.id,
            frameworkName: framework.value.name,
            loggingLevel: values[0],
            securityType: values[1],
            cacheProvider: values[2],
            hibernateDdlAuto: values[3]
          }
        };
        await saveConfiguration(updatedConfig as GenesisConfig<FrameworkAdvancedConfigurationPayload>);
      }
    }
  }
);

// Watcher : Chargement des valeurs Tech Stack lors de la sélection
watch(
  () => techStackConfigManager.selectedConfigId.value,
  (newId) => {
    if (newId) {
      const configToLoad = techStackConfigManager.configurations.value.find(c => c.id === newId);
      if (configToLoad) applyTechStackValues(configToLoad.components);
    }
  }
);

// Watcher : Auto-save Tech Stack
watch(
  () => [config.value.languageVersion, config.value.buildTool, config.value.groupId, config.value.frameworkVersion],
  async (newValues, oldValues) => {
    if (isApplyingTechStackConfig.value) return;
    if (oldValues && JSON.stringify(newValues) === JSON.stringify(oldValues)) return;

    const selectedId = techStackConfigManager.selectedConfigId.value;
    if (selectedId && framework.value) {
      const values = newValues as string[];
      techStackConfigManager.editConfiguration(selectedId, values);
      
      const updatedConfig = {
        id: selectedId,
        name: techStackConfigManager.configurations.value.find(c => c.id === selectedId)?.name || 'Config',
        configType: 'framework_technical-stack',
        schemaVersion: '1.0.0',
        createdAt: new Date().toISOString(),
        payload: {
          frameworkId: framework.value.id,
          frameworkName: framework.value.name,
          languageVersion: values[0],
          buildTool: values[1],
          groupId: values[2],
          frameworkVersion: values[3]
        }
      };
      await saveConfiguration(updatedConfig as GenesisConfig<FrameworkTechnicalStackPayload>);
    }
  }
);

// Watcher existant pour le chargement des options du framework
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
</script>