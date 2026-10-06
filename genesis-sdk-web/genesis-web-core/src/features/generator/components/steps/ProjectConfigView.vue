<template>
  <main class="flex gap-4 h-full overflow-hidden">
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. CONFIGURATION DE BASE -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-neutral-light pb-2">
          Configuration du Projet
        </h3>
        
        <div ref="frameworkSelectorRef" class="flex items-end gap-3">
          <div class="flex-1">
            <template v-if="isEditingFramework">
              <GenesisInput
                type="combobox"
                :options="availableFrameworksOptions"
                :model-value="tempFrameworkName"
                @update:model-value="handleFrameworkSelect"
                label="Framework"
                placeholder="Rechercher un framework..."
                size="lg"
                fill-width
              />
            </template>
            <template v-else>
              <div class="flex flex-col gap-1">
                <label class="text-sm font-medium text-muted">Framework sélectionné</label>
                <div class="flex items-center gap-2 h-10 px-3 bg-bg-secondary/30 rounded text-text border border-transparent">
                  <span class="truncate font-medium">{{ framework?.name || 'Aucun framework sélectionné' }}</span>
                </div>
              </div>
            </template>
          </div>
          
          <div class="pb-0.5">
            <GenesisButton v-if="!isEditingFramework" variant="secondary" size="lg" @click="startEditingFramework">
              Modifier
            </GenesisButton>
            <GenesisButton v-else variant="tertiary" size="lg" @click="cancelEditingFramework">
              Annuler
            </GenesisButton>
          </div>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput v-model="config.projectName" label="Nom du projet" placeholder="mon-super-projet" size="lg" is-mandatory fill-width />
          <GenesisInput v-model="config.projectPort" type="number" size="lg" label="Port d'exécution" placeholder="ex: 8080" fill-width />
        </div>

        <div class="w-full">
          <GenesisInput v-model="config.projectDescription" label="Description du projet" size="lg" placeholder="Une brève description de l'application..." type="textarea" fill-width />
        </div>

        <div class="w-full">
          <GenesisInput v-model="config.projectLocation" type="path" label="Emplacement" fill-width size="lg" @request-folder-path="handleSelectFolderPath" />
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 2. CONFIGURATION DU FRAMEWORK (STACK TECHNIQUE) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Stack Technique ({{ framework?.name || 'Non sélectionné' }})
        </h3>
        
        <div class="flex flex-col lg:flex-row gap-6">
          <div class="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
            <GenesisInput v-model="config.languageVersion" type="select" size="lg" label="Version du Language" placeholder="Sélectionner..." fill-width>
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button v-for="v in availableLanguageVersions" :key="v" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.languageVersion === v }" @click="() => { updateConfig('languageVersion', v); close(); }">
                    {{ v }}
                  </button>
                </div>
              </template>
            </GenesisInput>

            <GenesisInput v-model="config.buildTool" type="select" size="lg" label="Build Tool" placeholder="Sélectionner..." fill-width>
              <template #default="{ close }">
                <div class="p-1 space-y-1">
                  <button v-for="tool in availableBuildTools" :key="tool" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.buildTool === tool }" @click="() => { updateConfig('buildTool', tool); close(); }">
                    {{ tool.charAt(0).toUpperCase() + tool.slice(1) }}
                  </button>
                </div>
              </template>
            </GenesisInput>

            <div v-if="showGroupId" class="space-y-1">
              <GenesisInput v-model="config.groupId" size="lg" label="Group ID" placeholder="com.example" fill-width />
            </div>

            <div class="space-y-1">
              <GenesisInput v-model="config.frameworkVersion" type="select" size="lg" label="Version du Framework" placeholder="Sélectionner..." fill-width>
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button v-for="v in availableFrameworkVersions" :key="v" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.frameworkVersion === v }" @click="() => { updateConfig('frameworkVersion', v); close(); }">
                      {{ v }}
                    </button>
                  </div>
                </template>
              </GenesisInput>
            </div>
          </div>

          <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
            <GenesisConfigurationPanel
              :configurations="techStackConfigManager.configurations.value"
              :selected-config-id="techStackConfigManager.selectedConfigId.value"
              :filtered-configs="techStackConfigManager.filteredConfigs.value"
              :search-query="techStackConfigManager.searchQuery.value"
              :can-move-up="techStackConfigManager.canMoveUp.value"
              :can-move-down="techStackConfigManager.canMoveDown.value"
              @update:search-query="(val) => techStackConfigManager.searchQuery.value = val"
              @add="techStackLogic.handleAdd"
              @delete="techStackLogic.handleDelete"
              @rename="techStackLogic.handleRename"
              @toggle-visibility="techStackConfigManager.toggleVisibility"
              @move-up="techStackConfigManager.moveUp"
              @move-down="techStackConfigManager.moveDown"
              @select-configuration="techStackLogic.handleSelect"
            />
            <div class="flex gap-2 justify-end">
              <GenesisButtonIcon title="Exporter la configuration stack technique sélectionnée" :disabled="!techStackConfigManager.selectedConfigId.value" @click="techStackLogic.handleExport">
                <IconUpload />
              </GenesisButtonIcon>
            </div>
          </div>
        </div>
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 3. CONFIGURATION AVANCÉE -->
      <div class="space-y-4">
        <GenesisDisclosure title="Configuration Avancée du Backend" :default-open="false">
          <div class="flex flex-col lg:flex-row gap-6">
            
            <div class="flex-1 flex flex-col gap-4">
              <GenesisInput v-model="config.loggingLevel" type="select" label="Niveau de Logging" placeholder="INFO" fill-width>
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button v-for="opt in availableLoggingLevels" :key="opt" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.loggingLevel === opt }" @click="() => { updateConfig('loggingLevel', opt); close(); }">
                      {{ opt }}
                    </button>
                  </div>
                </template>
              </GenesisInput>

              <GenesisInput v-model="config.securityType" type="select" label="Type de Sécurité" placeholder="Aucune" fill-width>
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': !config.securityType || config.securityType === 'NONE' }" @click="() => { updateConfig('securityType', 'NONE'); close(); }">
                      Aucune
                    </button>
                    <button v-for="opt in availableSecurityTypes" :key="opt" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.securityType === opt }" @click="() => { updateConfig('securityType', opt); close(); }">
                      {{ opt }}
                    </button>
                  </div>
                </template>
              </GenesisInput>

              <GenesisInput v-model="config.cacheProvider" type="select" label="Fournisseur de Cache" placeholder="Aucun" fill-width>
                <template #default="{ close }">
                  <div class="p-1 space-y-1">
                    <button v-for="opt in availableCacheProviders" :key="opt" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.cacheProvider === opt }" @click="() => { updateConfig('cacheProvider', opt); close(); }">
                      {{ opt }}
                    </button>
                  </div>
                </template>
              </GenesisInput>

              <div v-if="showHibernateDdl" class="space-y-1">
                <GenesisInput v-model="config.hibernateDdlAuto" type="select" label="Hibernate DDL Auto" placeholder="none" fill-width>
                  <template #default="{ close }">
                    <div class="p-1 space-y-1">
                      <button v-for="opt in availableHibernateDdlAutoOptions" :key="opt" type="button" class="w-full text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors" :class="{ 'text-accent font-medium': config.hibernateDdlAuto === opt }" @click="() => { updateConfig('hibernateDdlAuto', opt); close(); }">
                        {{ opt }}
                      </button>
                    </div>
                  </template>
                </GenesisInput>
              </div>
            </div>

            <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
              <GenesisConfigurationPanel
                :configurations="configManager.configurations.value"
                :selected-config-id="configManager.selectedConfigId.value"
                :filtered-configs="configManager.filteredConfigs.value"
                :search-query="configManager.searchQuery.value"
                :can-move-up="configManager.canMoveUp.value"
                :can-move-down="configManager.canMoveDown.value"
                @update:search-query="(val) => configManager.searchQuery.value = val"
                @add="advancedLogic.handleAdd"
                @delete="advancedLogic.handleDelete"
                @rename="advancedLogic.handleRename"
                @toggle-visibility="configManager.toggleVisibility"
                @move-up="configManager.moveUp"
                @move-down="configManager.moveDown"
                @select-configuration="advancedLogic.handleSelect"
              />

              <div class="flex gap-2 justify-end">
                <GenesisButtonIcon title="Exporter la configuration avancée sélectionnée" :disabled="!configManager.selectedConfigId.value" @click="advancedLogic.handleExport">
                  <IconUpload />
                </GenesisButtonIcon>
                <GenesisButtonIcon title="Exporter toutes les configurations avancées" @click="advancedLogic.handleExportAll">
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
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
import { useConfigManagerLogic } from '@genesis-labs/web-core/features/generator/composables/useConfigManagerLogic'; // ✅ NOUVEAU IMPORT

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ============================================================================
// 1. EMITS & CONSTANTES
// ============================================================================
const emit = defineEmits<{ 'request-folder-path': [] }>();

const DEFAULT_VALUES_ADVANCED = ['INFO', 'NONE', 'Aucun', 'none'];
const DEFAULT_VALUES_TECH = ['', 'maven', 'com.example', ''];
const isApplyingConfig = ref(false);
const isApplyingTechStackConfig = ref(false);

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Slide 1' },
  { color: '#EF4444', label: 'Slide 2' },
  { color: '#10B981', label: 'Slide 3' },
];

// ============================================================================
// 2. COMPOSABLES & STORES
// ============================================================================
const { 
  stepperData, setFramework, updateConfig,
  availableLoggingLevels, availableSecurityTypes, availableCacheProviders,
  availableLanguageVersions, availableFrameworkVersions, availableBuildTools, availableHibernateDdlAutoOptions,
  fetchLoggingLevels, fetchSecurityTypes, fetchCacheProviders, fetchLanguageVersions,
  fetchFrameworkVersions, fetchBuildTools, fetchHibernateDdlAutoOptions
} = useGenerator();

const { saveConfiguration, deleteConfiguration, configurations: persistedConfigs, loadAdvancedConfigurations, loadTechnicalStackConfigurations } = useConfig();
const frameworkStore = useFrameworkStore();

const configManager = useConfigurationManager([], { singleConfiguration: false });
const techStackConfigManager = useConfigurationManager([], { singleConfiguration: false });

// ============================================================================
// 3. GESTION DE L'ÉDITION DU FRAMEWORK
// ============================================================================
const frameworkSelectorRef = ref<HTMLElement | null>(null);
const isEditingFramework = ref(false);
const tempFrameworkName = ref('');

const availableFrameworksOptions = computed(() => frameworkStore.frameworks.map(f => ({ label: f.name, value: f })));
const config = computed(() => stepperData.value.config);
const framework = computed(() => stepperData.value.framework);
const showGroupId = computed(() => framework.value?.withGroupId === true);
const showHibernateDdl = computed(() => framework.value?.withHibernateDdlAuto === true);

function startEditingFramework() { tempFrameworkName.value = ''; isEditingFramework.value = true; }
function cancelEditingFramework() { isEditingFramework.value = false; tempFrameworkName.value = ''; }

function handleFrameworkSelect(value: any) {
  if (typeof value === 'string') return; 
  if (value && value.id) { setFramework(value); isEditingFramework.value = false; }
}

function handleClickOutside(event: MouseEvent) {
  if (isEditingFramework.value && frameworkSelectorRef.value && !frameworkSelectorRef.value.contains(event.target as Node)) {
    cancelEditingFramework();
  }
}

watch(isEditingFramework, (isEditing) => {
  if (isEditing) setTimeout(() => window.addEventListener('mousedown', handleClickOutside), 0);
  else window.removeEventListener('mousedown', handleClickOutside);
});

// ============================================================================
// 4. INITIALISATION DES LOGIQUES DE CONFIGURATION (Via le Composable DRY)
// ============================================================================
const advancedLogic = useConfigManagerLogic({
  manager: configManager,
  configType: 'framework_advanced-configuration',
  defaultValues: DEFAULT_VALUES_ADVANCED,
  getCurrentValues: () => [config.value.loggingLevel || DEFAULT_VALUES_ADVANCED[0], config.value.securityType || DEFAULT_VALUES_ADVANCED[1], config.value.cacheProvider || DEFAULT_VALUES_ADVANCED[2], config.value.hibernateDdlAuto || DEFAULT_VALUES_ADVANCED[3]],
  buildPayload: (values) => ({
    frameworkId: framework.value?.id || 0, frameworkName: framework.value?.name || 'Inconnu',
    loggingLevel: values ? values[0] : config.value.loggingLevel,
    securityType: values ? values[1] : config.value.securityType,
    cacheProvider: values ? values[2] : config.value.cacheProvider,
    hibernateDdlAuto: values ? values[3] : config.value.hibernateDdlAuto
  }),
  fileNamePrefix: 'advanced',
  watchSource: () => [config.value.loggingLevel, config.value.securityType, config.value.cacheProvider, config.value.hibernateDdlAuto],
  applyValues: (vals) => {
    isApplyingConfig.value = true;
    updateConfig('loggingLevel', vals[0]); updateConfig('securityType', vals[1]);
    updateConfig('cacheProvider', vals[2]); updateConfig('hibernateDdlAuto', vals[3]);
    nextTick(() => { isApplyingConfig.value = false; });
  },
  isApplyingRef: isApplyingConfig,
  framework,
  saveConfiguration,
  deleteConfiguration,
  persistedConfigs
});

const techStackLogic = useConfigManagerLogic({
  manager: techStackConfigManager,
  configType: 'framework_technical-stack',
  defaultValues: DEFAULT_VALUES_TECH,
  getCurrentValues: () => [config.value.languageVersion || DEFAULT_VALUES_TECH[0], config.value.buildTool || DEFAULT_VALUES_TECH[1], config.value.groupId || DEFAULT_VALUES_TECH[2], config.value.frameworkVersion || DEFAULT_VALUES_TECH[3]],
  buildPayload: (values) => ({
    frameworkId: framework.value?.id || 0, frameworkName: framework.value?.name || 'Inconnu',
    languageVersion: values ? values[0] : config.value.languageVersion,
    buildTool: values ? values[1] : config.value.buildTool,
    groupId: values ? values[2] : config.value.groupId,
    frameworkVersion: values ? values[3] : config.value.frameworkVersion
  }),
  fileNamePrefix: 'tech_stack',
  watchSource: () => [config.value.languageVersion, config.value.buildTool, config.value.groupId, config.value.frameworkVersion],
  applyValues: (vals) => {
    isApplyingTechStackConfig.value = true;
    updateConfig('languageVersion', vals[0]); updateConfig('buildTool', vals[1]);
    updateConfig('groupId', vals[2]); updateConfig('frameworkVersion', vals[3]);
    nextTick(() => { isApplyingTechStackConfig.value = false; });
  },
  isApplyingRef: isApplyingTechStackConfig,
  framework,
  saveConfiguration,
  deleteConfiguration,
  persistedConfigs
});

// ============================================================================
// 5. LIFECYCLE & WATCHERS GLOBAUX
// ============================================================================
async function syncConfigsToCurrentFramework() {
  if (!framework.value) return;
  const { id: fwId, name: fwName } = framework.value;

  const advancedData = await loadAdvancedConfigurations(fwId, fwName);
  configManager.configurations.value = advancedData.map(c => {
    const p = c.payload as any;
    return { id: c.id, name: c.name, isHidden: false, components: [p.loggingLevel, p.securityType, p.cacheProvider, p.hibernateDdlAuto] };
  });
  configManager.selectConfiguration(null);
  advancedLogic.handleSelect(null);

  const techStackData = await loadTechnicalStackConfigurations(fwId, fwName);
  techStackConfigManager.configurations.value = techStackData.map(c => {
    const p = c.payload as any;
    return { id: c.id, name: c.name, isHidden: false, components: [p.languageVersion, p.buildTool, p.groupId, p.frameworkVersion] };
  });
  techStackConfigManager.selectConfiguration(null);
  techStackLogic.handleSelect(null);
}

onMounted(async () => { await syncConfigsToCurrentFramework(); });

watch(() => framework.value?.id, async (newId, oldId) => {
  if (newId && newId !== oldId && framework.value) {
    await syncConfigsToCurrentFramework();
    await Promise.all([
      fetchBuildTools(newId), fetchLanguageVersions(framework.value.languageId),
      fetchFrameworkVersions(newId), fetchLoggingLevels(newId),
      fetchSecurityTypes(newId), fetchCacheProviders(newId), fetchHibernateDdlAutoOptions(newId)
    ]);
  }
});

function handleSelectFolderPath() { emit('request-folder-path'); }
</script>