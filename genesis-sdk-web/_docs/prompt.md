ça ne marche pas vraiment :
genesis-sdk-web\genesis-web-core\src\features\generator\components\steps\ProjectConfigView.vue

<template>
  <main class="flex gap-4 h-full overflow-hidden">
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. CONFIGURATION DE BASE -->
      <div class="space-y-4">
        <!-- ✅ AJOUT : En-tête avec le déclencheur du tutoriel -->
        <div class="flex items-center justify-between border-b border-neutral-light-genesis pb-2">
          <h3 class="text-xl font-semibold text-text">
            Configuration du Projet
          </h3>
          <GenesisButtonIcon variant="neutral" size="sm" @click="tutorial.restart">
              <IconHelp />
          </GenesisButtonIcon>
        </div>
        
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

        <!-- ✅ CORRECTION : Remplacement de v-model par :model-value + @update:model-value -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <GenesisInput 
            :model-value="config.projectName" 
            @update:model-value="(val) => updateConfig('projectName', val)"
            label="Nom du projet" 
            placeholder="mon-super-projet" 
            size="lg" 
            is-mandatory 
            fill-width 
          />
          <GenesisInput 
            :model-value="config.projectPort" 
            @update:model-value="(val) => updateConfig('projectPort', val)"
            type="number" 
            size="lg" 
            label="Port d'exécution" 
            placeholder="ex: 8080" 
            fill-width 
          />
        </div>

        <div class="w-full">
          <GenesisInput 
            :model-value="config.projectDescription" 
            @update:model-value="(val) => updateConfig('projectDescription', val)"
            label="Description du projet" 
            size="lg" 
            placeholder="Une brève description de l'application..." 
            type="textarea" 
            fill-width 
          />
        </div>

        <div class="w-full">
          <GenesisInput 
            :model-value="config.projectLocation" 
            @update:model-value="(val) => updateConfig('projectLocation', val)"
            type="path" 
            label="Emplacement" 
            fill-width 
            size="lg" 
            @request-folder-path="handleSelectFolderPath" 
          />
        </div>
      </div>

      <!-- 2. CONFIGURATION DU FRAMEWORK (STACK TECHNIQUE) -->
      <div class="space-y-6">
        <div class="flex items-center justify-between border-b border-neutral-light-genesis pb-2">
          <h3 class="text-xl font-semibold text-text">
            Stack Technique ({{ framework?.name || 'Non sélectionné' }})
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('tech-stack')"
            title="Aide sur la configuration de la stack technique"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>
        
        <div class="flex flex-col lg:flex-row gap-6">
          <div class="flex-1 flex flex-col gap-4">
            
            <GenesisInput 
              :model-value="config.languageVersion" 
              type="select" 
              :options="languageVersionOptions"
              @update:model-value="(val) => updateConfig('languageVersion', val)"
              size="lg" 
              label="Version du Language" 
              placeholder="Sélectionner..." 
            />

            <GenesisInput 
              :model-value="config.buildTool" 
              type="select" 
              :options="buildToolOptions"
              @update:model-value="(val) => updateConfig('buildTool', val)"
              size="lg" 
              label="Build Tool" 
              placeholder="Sélectionner..." 
            />

            <div v-if="showGroupId" class="space-y-1">
              <GenesisInput 
                :model-value="config.groupId" 
                @update:model-value="(val) => updateConfig('groupId', val)"
                size="lg" 
                label="Group ID" 
                placeholder="com.example" 
              />
            </div>

            <GenesisInput 
              :model-value="config.frameworkVersion" 
              type="select" 
              :options="frameworkVersionOptions"
              @update:model-value="(val) => updateConfig('frameworkVersion', val)"
              size="lg" 
              label="Version du Framework" 
              placeholder="Sélectionner..." 
            />

          </div>

          <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
            <GenesisTutorialItem id="tutorial-tech-stack-config">
              <GenesisConfigurationPanel
                :tutorial-id="'tutorial-tech-stack-config'"
                :include-sub-tutorial="true"
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
            </GenesisTutorialItem>
            <div class="flex gap-2 justify-end">
              <GenesisButtonIcon title="Exporter la configuration stack technique sélectionnée" :disabled="!techStackConfigManager.selectedConfigId.value" @click="techStackLogic.handleExport">
                <IconUpload />
              </GenesisButtonIcon>
            </div>
          </div>
        </div>
      </div>

      <!-- 3. CONFIGURATION AVANCÉE -->
      <div class="space-y-4">
        <GenesisDisclosure title="Configuration Avancée du Backend" :variant="'secondary'" :padding="'lg'" :default-open="false">

          <template #title>
              <span class="text-xl font-semibold">Configuration Avancée du Backend</span>
          </template>

          <div class="flex flex-col lg:flex-row gap-6">
            
            <div class="flex-1 flex flex-col gap-4">
              
              <GenesisInput 
                :model-value="config.loggingLevel" 
                type="select" 
                :options="loggingLevelOptions"
                @update:model-value="(val) => updateConfig('loggingLevel', val)"
                label="Niveau de Logging" 
                placeholder="INFO" 
                fill-width
                :size="'lg'"
              />

              <GenesisInput 
                :model-value="config.securityType" 
                type="select" 
                :options="securityTypeOptions"
                @update:model-value="(val) => updateConfig('securityType', val)"
                label="Type de Sécurité" 
                placeholder="Aucune" 
                fill-width
                :size="'lg'"
              />

              <GenesisInput 
                :model-value="config.cacheProvider" 
                type="select" 
                :options="cacheProviderOptions"
                @update:model-value="(val) => updateConfig('cacheProvider', val)"
                label="Fournisseur de Cache" 
                placeholder="Aucun" 
                fill-width
                :size="'lg'"
              />

              <div v-if="showHibernateDdl" class="space-y-1">
                <GenesisInput 
                  :model-value="config.hibernateDdlAuto" 
                  type="select" 
                  :options="hibernateDdlAutoOptions"
                  @update:model-value="(val) => updateConfig('hibernateDdlAuto', val)"
                  label="Hibernate DDL Auto" 
                  placeholder="none" 
                  fill-width 
                  :size="'lg'"
                />
              </div>
            </div>

            <div class="w-full lg:w-[350px] flex flex-col gap-4 flex-shrink-0">
              <div class="flex items-center justify-end gap-1.5">
                <span class="text-sm text-text-muted">How to use</span>
                <GenesisButtonIcon 
                  variant="neutral" 
                  size="sm" 
                  @click="tutorial.restart('advanced')"
                  title="Aide sur la configuration avancée"
                >
                  <IconHelp :size="16" />
                </GenesisButtonIcon>
              </div>

              <GenesisTutorialItem id="tutorial-advanced-config">
                <GenesisConfigurationPanel
                  :tutorial-id="'tutorial-advanced-config'"
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
              </GenesisTutorialItem>

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
import { Ref } from 'vue';
import { computed, watch, ref, onMounted, nextTick } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
import { useConfigManagerLogic } from '@genesis-labs/web-core/features/generator/composables/useConfigManagerLogic';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';

import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';

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

// deux choix, soit je l'associe à une variable boolean
// soit c'est la lecture qui ne le lis pas si il est caché

const tutorial = useGenesisTutorial('project-config', {
  'tech-stack': [
    { id: 'tutorial-tech-stack-config', text: 'Enregistrez ici vos stacks techniques favorites pour les réutiliser.' }
  ],
  'advanced': [
    { id: 'tutorial-advanced-config', text: 'Même principe pour la configuration avancée du backend.' }
  ]
});

console.log("✅ [ProjectConfigView] useGenesisTutorial initialisé. ScreenId: 'project-config'");

// ============================================================================
// 2. COMPOSABLES & STORES
// ============================================================================
const { 
  stepperData,
  setPendingFramework,
  pendingFramework, 
  updateConfig,
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
const framework = computed(() => pendingFramework.value || stepperData.value.framework);
const showGroupId = computed(() => framework.value?.withGroupId === true);
const showHibernateDdl = computed(() => framework.value?.withHibernateDdlAuto === true);

function startEditingFramework() { tempFrameworkName.value = ''; isEditingFramework.value = true; }
function cancelEditingFramework() { isEditingFramework.value = false; tempFrameworkName.value = ''; }

function handleFrameworkSelect(value: any) {
  if (typeof value === 'string') return; 
  if (value && value.id) { 
    setPendingFramework(value); 
    isEditingFramework.value = false; 
  }
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
// 3.5. MAPPING DES OPTIONS POUR LE BASCULEMENT AUTOMATIQUE
// ============================================================================
const languageVersionOptions = computed(() => 
  availableLanguageVersions.value.map(v => ({ label: v, value: v }))
);

const buildToolOptions = computed(() => 
  availableBuildTools.value.map(t => ({ label: t.charAt(0).toUpperCase() + t.slice(1), value: t }))
);

const frameworkVersionOptions = computed(() => 
  availableFrameworkVersions.value.map(v => ({ label: v, value: v }))
);

const loggingLevelOptions = computed(() => 
  availableLoggingLevels.value.map(opt => ({ label: opt, value: opt }))
);

const securityTypeOptions = computed(() => 
  [{ label: 'Aucune', value: 'NONE' }, ...availableSecurityTypes.value.map(opt => ({ label: opt, value: opt }))]
);

const cacheProviderOptions = computed(() => 
  availableCacheProviders.value.map(opt => ({ label: opt, value: opt }))
);

const hibernateDdlAutoOptions = computed(() => 
  availableHibernateDdlAutoOptions.value.map(opt => ({ label: opt, value: opt }))
);

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

function selectFirstOptionByDefault(
  options: Ref<{ label: string; value: any }[]>,
  key: 'languageVersion' | 'buildTool' | 'frameworkVersion'
) {
  watch(options, (opts) => {
    if (opts.length > 0 && !config.value[key]) {
      updateConfig(key, opts[0].value);
    }
  }, { immediate: true });
}

selectFirstOptionByDefault(languageVersionOptions, 'languageVersion');
selectFirstOptionByDefault(buildToolOptions, 'buildTool');
selectFirstOptionByDefault(frameworkVersionOptions, 'frameworkVersion');

function handleSelectFolderPath() { emit('request-folder-path'); }
</script>

genesis-sdk-web\genesis-web-core\src\features\generator\composables\wizard-step-config.ts
// genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts

// Types & Interfaces
import type { IGeneratorService } from '../types/generator.service.interface';
import type { IFrontendService } from '../../frontend/types/frontend.service.interface';
import type { IDatabaseService } from '../../database/types/database.service.interface';
import type { IFrameworkService, GenerationParameter, ComponentType, IRepositoryService } from '@genesis-labs/shared-types';

// Stores
import { useGeneratorStore } from '../store/useGenerator.store';
import { useFrameworkStore } from '../../frameworks/store/useFramework.store';
import { useDatabaseStore } from '../../database/store/useDatabase.store';
import { useFrontendStore } from '../../frontend/store/useFrontend.store';
import { useRepositoryStore } from '../../repository/manifest';

import { useRecentStore } from '@genesis-labs/web-core/core/features/recent/store/useRecent.store';
import { RecentItem } from '@genesis-labs/shared-types';
import type { IRecentService } from '@genesis-labs/web-core/core/features/recent/types/recent.service.interface';

import { toRaw } from 'vue';


// ============================================================================
// INTERFACES & TYPES
// ============================================================================

export interface WizardServices {
  svc: IGeneratorService;
  fdsvc: IFrontendService;
  dbsvc: IDatabaseService;
  fsvc: IFrameworkService;
  repoSvc: IRepositoryService;
  recentSvc: IRecentService;
}

export interface StepConfig {
  onEnter?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void;
  beforeNext?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<boolean> | boolean;
  onSkip?: (store: ReturnType<typeof useGeneratorStore>, services: WizardServices) => Promise<void> | void;

  /**
   * liste des éléments skippés après ce dernier
   */
  skipCascade?: StepId[];
}

// Identifiants sémantiques des étapes (Le contrat TypeScript)
export type StepId = 
  | 'framework_select'
  | 'project_config'
  | 'database_select'
  | 'database_config'
  | 'script'
  | 'generation'
  | 'relations'
  | 'frontend_select'
  | 'frontend_config'
  | 'repository_select'
  | 'repository_config'
  | 'summary';

export const STEP_LABELS: string[] = [
  'Framework',
  'Configuration Projet',
  'Sélection BDD',
  'Configuration BDD',
  'Script / IA',
  'Génération',
  'Relations',
  'Sélection Frontend',
  'Configuration Frontend',
  'Sélection Dépôt',
  'Configuration Dépôt',
  'Résumé'
];

// ============================================================================
// SOURCE DE VÉRITÉ : ORDRE DES ÉTAPES
// ============================================================================

/**
 * L'ordre de ce tableau définit l'ordre d'exécution du wizard.
 * Pour réorganiser le wizard, il suffit de réorganiser ce tableau.
 */
export const WIZARD_STEP_ORDER: StepId[] = [
  'framework_select',
  'project_config',
  'database_select',
  'database_config',
  'script',
  'generation',
  'relations',
  'frontend_select',
  'frontend_config',
  'repository_select',
  'repository_config',
  'summary'
];

// ============================================================================
// MAPPING WIZARD <-> SUMMARY
// ============================================================================
// Index du résumé : 0:Project, 1:Database, 2:Generation, 3:Relations, 4:Frontend, 5:Repository
export const STEP_TO_SUMMARY_MAP: Record<StepId, number | null> = {
  'framework_select': 0,
  'project_config': 0,
  'database_select': 1,
  'database_config': 1,
  'script': null, // Intentionnel : pas de résumé
  'generation': 2,
  'relations': 3,
  'frontend_select': 4,
  'frontend_config': 4,
  'repository_select': 5,
  'repository_config': 5,
  'summary': null
};

// Mapping inverse : quel est le premier step de config associé à un index de résumé ?
export const SUMMARY_TO_STEP_MAP: Record<number, StepId> = {
  0: 'project_config',
  1: 'database_config',
  2: 'generation',
  3: 'relations',
  4: 'frontend_config',
  5: 'repository_config'
};

// ============================================================================
// CONFIGURATION MÉTIER PAR ÉTAPE
// ============================================================================

export const WIZARD_STEP_CONFIG: Record<StepId, StepConfig> = {
  
  framework_select: {
    onEnter: async (_store, { fsvc, recentSvc }) => {
      console.log("[Wizard: framework] Chargement des frameworks, langues et données récentes...");
      
      const frameworkStore = useFrameworkStore();
      const recentStore = useRecentStore();
      
      try { 
        const [frameworksData, languagesData, recencyData] = await Promise.all([
          fsvc.fetchFrameworks(),
          fsvc.fetchLanguages(),
          recentSvc.getRecencyData() 
        ]);
        
        frameworkStore.setFrameworks(frameworksData);
        frameworkStore.setLanguages(languagesData);
        recentStore.setRecencyData(recencyData);
        
        console.log(`[Wizard: framework] Chargement réussi : ${frameworksData.length} frameworks, ${languagesData.length} langues, ${recencyData.frameworks.length} récents.`);
      } catch (error) {
        console.error("[Wizard: framework] Échec du chargement:", error);
        _store.setWizardError("Impossible de charger la liste des frameworks.");
      }
    },
    
    beforeNext: async (store) => {
      const framework = store.pendingFramework;
      
      if (!framework) {
        store.setWizardError("Veuillez sélectionner un framework avant de continuer.");
        return false;
      }

      console.log(`[Wizard: framework] Framework en attente de validation :`, framework.name);
      return true;
    }
  },

  project_config: {
    onEnter: async (store, { fsvc }) => {
      // On lit le pending en priorité pour charger les bonnes options
      const framework = store.pendingFramework || store.stepperData.framework;
      if (!framework) return;
      
      console.log("[Wizard: project_config] Chargement des options de configuration...");
      
      try {
        await Promise.all([
          fsvc.fetchLoggingLevels(framework.id).then(data => store.setAvailableLoggingLevels(data)),
          fsvc.fetchSecurityTypes(framework.id).then(data => store.setAvailableSecurityTypes(data)),
          fsvc.fetchCacheProviders(framework.id).then(data => store.setAvailableCacheProviders(data)),
          fsvc.fetchLanguageVersions(framework.languageId).then(data => store.setAvailableLanguageVersions(data)),
          fsvc.fetchFrameworkVersions(framework.id).then(data => store.setAvailableFrameworkVersions(data)),
          fsvc.fetchBuildTools(framework.id).then(data => store.setAvailableBuildTools(data)),
        ]);
        console.log("[Wizard: project_config] Options de configuration chargées");
      } catch (error) {
        console.error("[Wizard: project_config] Échec du chargement des options:", error);
      }
    },
    
    beforeNext: async (store) => {
      const config = store.stepperData.config;
      
      if (!config.projectName || !config.projectLocation) {
        store.setWizardError("Le nom du projet et la localisation sont obligatoires.");
        return false;
      }

      try {
        // COMMIT FINAL : C'est ici qu'on grave la sélection pending dans le stepperData
        if (store.pendingFramework) {
          store.setFramework(store.pendingFramework);
          console.log(`[Wizard: project_config] Framework validé et commit :`, store.pendingFramework.name);
        }

        console.log("[Wizard: project_config] Configuration du projet validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde de la configuration.");
        return false;
      }
    }
  },

  database_select: {
    onEnter: async (_store, { dbsvc }) => {
      console.log("[Wizard: database_select] Chargement des moteurs de base de données...");
      const databaseStore = useDatabaseStore();
      
      try {
        const engines = await dbsvc.fetchDatabaseEngines();
        databaseStore.setAvailableEngines(engines);
        console.log(`[Wizard: database_select] ${engines.length} moteurs chargés`);
      } catch (error) {
        console.error("[Wizard: database_select] Échec du chargement des moteurs DB:", error);
        _store.setWizardError("Impossible de charger la liste des bases de données.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingDatabaseEngine) {
        store.setWizardError("Veuillez sélectionner un moteur de base de données.");
        return false;
      }

      console.log(`[Wizard: database_select] Moteur BDD en attente de validation :`, store.pendingDatabaseEngine.name);
      return true;
    }
  },

  database_config: {
    beforeNext: async (store) => {
      const db = store.stepperData.database;
      
      // 1. Validation basique des champs obligatoires du formulaire
      if (!db.host || !db.port || !db.databaseName || !db.username) {
        store.setWizardError("L'hôte, le port, le nom de la base de données et l'utilisateur sont obligatoires.");
        return false;
      }

      try {
        // 2. COMMIT FINAL : C'est ici qu'on grave la sélection pending dans le stepperData
        if (store.pendingDatabaseEngine) {
          store.setDatabaseEngine(store.pendingDatabaseEngine);
          console.log(`[Wizard: database_config] Moteur BDD validé et commit :`, store.pendingDatabaseEngine.name);
        }

        console.log("[Wizard: database_config] Configuration BDD validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des credentials.");
        return false;
      }
    }
  },

  script: {
    beforeNext: async (/* store, { svc } */) => {
      // const script = store.stepperData.script;
      
      // if (script.path || script.content) {
      //   try { 
      //     await svc.saveScriptConfig(script); 
      //   } catch (error) { 
      //     console.warn("[Wizard: script] Échec sauvegarde script (non bloquant):", error); 
      //   }
      // }
      
      return true;
    },
    skipCascade: [] // Peut être sauté sans conséquence sur les autres
  },

  generation: {
    onEnter: async (store, { svc }) => {
      console.log("[Wizard: tables] Chargement des tables et vues...");
      
      try {
        const tables = await svc.fetchTablesMetadata();
        store.setAvailableTables(tables);
        console.log(`[Wizard: tables] ${tables.length} tables/vues chargées`);
      } catch (error) {
        console.error("[Wizard: tables] Échec du chargement des tables:", error);
      }
    },
    
    beforeNext: async (store) => {
      try {
        // 1. Récupérer les données sources
        const profiles = store.generationProfiles; // Type 1 : Les profils avec composants
        const associations = store.generationAssociations; // Type 2 : Les liens tables/vues ↔ profils
        
        // 2. Construire le tableau final (Type 3)
        const generationParameters: GenerationParameter[] = [];
        
        for (const profile of profiles) {
          // Trouver l'association correspondante
          const assoc = associations.find(a => a.configId === profile.id);
          
          const assignedTables = assoc?.associatedTables || [];
          const assignedViews = assoc?.associatedViews || [];
          
          // On ignore totalement les profils qui n'ont 
          // aucune table ou vue assignée. Cela garantit que seules les entités 
          // explicitement liées à une configuration sont envoyées au backend.
          if (assignedTables.length === 0 && assignedViews.length === 0) {
            console.log(`[Wizard: tables] Profil "${profile.name}" ignoré : aucune entité assignée.`);
            continue; // On passe au profil suivant sans l'ajouter au tableau
          }
          
          // Construire le GenerationParameter uniquement avec les données assignées
          const param: GenerationParameter = {
            profileName: profile.name,
            selectedTables: assignedTables,
            selectedViews: assignedViews,
            selectedComponents: profile.components as ComponentType[]
          };
          
          generationParameters.push(param);
        }
        
        // 3. Stocker dans stepperData pour l'envoi au backend
        store.stepperData.generationParameters = generationParameters;
        
        console.log("[Wizard: tables] Sélection validée. Seules les entités assignées sont incluses :", generationParameters);
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des tables.");
        return false;
      }
    }
  },

  relations: {
    beforeNext: async (store) => {
      try {
        console.log("[Wizard: relations] Configuration des relations validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde des relations.");
        return false;
      }
    }
  },

  frontend_select: {
    onEnter: async (_store, { fdsvc }) => {
      console.log("[Wizard: frontend_select] Chargement des options Frontend...");
      const frontendStore = useFrontendStore();
      
      try {
        await Promise.all([
          fdsvc.fetchFrontendFrameworks().then(data => frontendStore.setAvailableFrontendFrameworks(data)),
          fdsvc.fetchInterfaceLanguages().then(data => frontendStore.setAvailableInterfaceLanguages(data)),
        ]);
        console.log("[Wizard: frontend_select] Options Frontend chargées");
      } catch (error) {
        console.error("[Wizard: frontend_select] Échec du chargement des options Frontend:", error);
        _store.setWizardError("Impossible de charger les options frontend.");
      }
    },
    
    beforeNext: async (store) => {
      // Si aucun framework n'est sélectionné, c'est skippable, on passe à la suite
      if (!store.pendingFrontendFramework) {
        console.log("[Wizard: frontend_select] Frontend ignoré (skippable)");
        return true;
      }

      console.log(`[Wizard: frontend_select] Framework Frontend en attente de validation :`, store.pendingFrontendFramework.name);
      return true;
    },
    // CORRECTION MAJEURE : Si on saute le choix du frontend, on saute automatiquement sa configuration
    skipCascade: [
      'frontend_config'
    ] 
  },

  frontend_config: {
    beforeNext: async (store) => {
      try {
        if (store.pendingFrontendFramework) {
          store.setSelectedFrontendFramework(store.pendingFrontendFramework);
          console.log(`[Wizard: frontend_layout] Framework Frontend validé et commit :`, store.pendingFrontendFramework.name);
        }

        console.log("[Wizard: frontend_layout] Configuration layout Frontend validée");
        return true;
      } catch (error) {
        store.setWizardError(error instanceof Error ? error.message : "Échec de la sauvegarde du layout frontend.");
        return false;
      }
    }
  },

  repository_select: {
    onEnter: async (_store, { repoSvc }) => {
      console.log("[Wizard: repository_select] Chargement des fournisseurs de dépôt...");
      const repositoryStore = useRepositoryStore();
      
      try {
        const providersData = await repoSvc.getSupportedProviders();
        repositoryStore.setProviders(providersData);
        console.log(`[Wizard: repository_select] ${providersData.length} fournisseur(s) chargé(s)`);
      } catch (error) {
        console.error("[Wizard: repository_select] Échec du chargement des fournisseurs:", error);
        _store.setWizardError("Impossible de charger la liste des fournisseurs de dépôt.");
      }
    },
    
    beforeNext: async (store) => {
      if (!store.pendingRepositoryProvider) {
        store.setWizardError("Veuillez sélectionner un fournisseur de dépôt avant de continuer.");
        return false;
      }

      store.updateRepositoryConfig('provider', store.pendingRepositoryProvider.id);
      
      console.log(`[Wizard: repository_select] Fournisseur sélectionné et persisté :`, store.pendingRepositoryProvider.name);
      return true;
    },

    onSkip: (store) => {
      // L'utilisateur a cliqué sur "Passer" : on désactive Git et on nettoie
      store.skipGitConfig();
    },
    
    skipCascade: [
      'repository_config'
    ]
  },

  repository_config: {
    onEnter: (store) => {
      // Initialisation par défaut si l'objet n'existe pas encore
      if (!store.stepperData.repositoryConfig) {
        store.updateRepositoryConfig('provider', store.pendingRepositoryProvider?.id || 'github');
        store.updateRepositoryConfig('useRemote', false);
        store.updateRepositoryConfig('isNewRemote', true);
        store.updateRepositoryConfig('scope', 'monorepo');
        store.updateRepositoryConfig('authMethod', 'personal_access_token');
      }
      
      // On s'assure que les valeurs par défaut Git sont propres
      store.stepperData.git.useGit = true;
      if (!store.stepperData.git.useRemoteRepo) {
        store.stepperData.git.isNewRemoteRepo = true;
        store.stepperData.git.separateRepositories = false;
      }
    },
    
    beforeNext: (store) => {
      const repo = store.stepperData.repositoryConfig;
      
      // On active Git par défaut
      store.updateGitConfig('useGit', true);

      if (repo?.useRemote) {
        // 1. Validation des champs obligatoires du formulaire
        if (!repo.username) {
          store.setWizardError("Le nom d'utilisateur est obligatoire pour un dépôt distant.");
          return false;
        }
        if (repo.isNewRemote && !repo.token) {
          store.setWizardError("Le Personal Access Token est obligatoire pour créer un nouveau dépôt.");
          return false;
        }
        if (repo.scope === 'separated') {
          if (!repo.backendRepoName || !repo.frontendRepoName) {
            store.setWizardError("Les noms des dépôts Backend et Frontend sont obligatoires.");
            return false;
          }
        } else {
          if (!repo.monorepoName) {
            store.setWizardError("Le nom du dépôt unique est obligatoire.");
            return false;
          }
        }

        // 2. 🔄 MAPPING (FORCING) : Conversion de RepositoryConfig vers GitConfiguration
        // Le backend ne supporte que GitHub pour l'instant, on mappe donc les données génériques 
        // vers les champs spécifiques GitHub attendus par l'interface GitConfiguration.
        store.updateGitConfig('separateRepositories', repo.scope === 'separated');
        store.updateGitConfig('useRemoteRepo', true);
        store.updateGitConfig('isNewRemoteRepo', repo.isNewRemote);
        store.updateGitConfig('repositoryName', repo.monorepoName || '');
        store.updateGitConfig('backendRepositoryName', repo.backendRepoName || '');
        store.updateGitConfig('frontendRepositoryName', repo.frontendRepoName || '');
        store.updateGitConfig('githubUsername', repo.username || '');
        store.updateGitConfig('githubToken', repo.token || '');
        
      } else {
        // 3. Si pas de remote, on s'assure que Git est configuré en local uniquement
        store.updateGitConfig('useRemoteRepo', false);
        store.updateGitConfig('isNewRemoteRepo', true);
        store.updateGitConfig('separateRepositories', false);
        store.updateGitConfig('repositoryName', '');
        store.updateGitConfig('backendRepositoryName', '');
        store.updateGitConfig('frontendRepositoryName', '');
        store.updateGitConfig('githubUsername', '');
        store.updateGitConfig('githubToken', '');
      }
      
      console.log("[Wizard: git_config] Configuration Repository validée et mappée pour le backend.");
      return true;
    }
  },

  // genesis-sdk-web/genesis-web-core/src/features/generator/composables/wizard-step-config.ts
  summary: {
    onEnter: (store) => {
      console.log("[Wizard: summary] Affichage du résumé final de la configuration.");
      // On s'assure que l'état est réinitialisé si on revient en arrière (cas rare mais possible)
      store.setIsGenerationComplete(false); 
    },
    
    beforeNext: async (store, { recentSvc, svc }) => {
      try {
        console.log("[Wizard: summary] Résumé validé. Prêt pour la génération finale.");
        store.setIsGenerating(true);
        
        const startTime = Date.now();
        
        try {
          const data = toRaw(store.stepperData);
          await svc.launchGeneration(data);

          // 1. Sauvegarder le Framework Backend
          if (data.framework) {
            await recentSvc.addRecentItem('frameworks', {
              id: data.framework.id,
              name: data.framework.name,
              lastUsed: Date.now()
            } as RecentItem);
            console.log(`[Recency] Framework ajouté aux récents: ${data.framework.name}`);
          }

          // 2. Sauvegarder le Moteur de Base de Données
          if (data.database?.engine) {
            const databaseStore = useDatabaseStore();
            
            // 1. Retrouver l'objet moteur complet grâce à son nom (string)
            const matchedEngine = databaseStore.availableEngines.find(eng => {
              const normalizedName = eng.name.toLowerCase().replace(/\s+/g, '');
              const normalizedConfig = data.database!.engine.toLowerCase().replace(/\s+/g, '');
              return normalizedName === normalizedConfig;
            });
            
            // 2. Sauvegarder en utilisant le VRAI ID numérique du moteur
            if (matchedEngine) {
              await recentSvc.addRecentItem('databases', {
                id: matchedEngine.id,  // ✅ Maintenant c'est un number, comme e.id
                name: matchedEngine.name,
                lastUsed: Date.now()
              } as RecentItem);
              console.log(`[Recency] Database ajouté aux récents: ${matchedEngine.name} (ID: ${matchedEngine.id})`);
            }
          }

          // 3. Sauvegarder le Framework Frontend (si sélectionné)
          if (data.frontend) {
            await recentSvc.addRecentItem('frontends', {
              id: data.frontend.id,
              name: data.frontend.name,
              lastUsed: Date.now()
            } as RecentItem);
            console.log(`[Recency] Frontend ajouté aux récents: ${data.frontend.name}`);
          }

        } catch (error) {
          console.error("[Wizard: summary] Échec de la mise à jour de la récence:", error);
        } finally {
          const elapsedTime = Date.now() - startTime;
          const minDuration = 3000;
          const remainingTime = Math.max(0, minDuration - elapsedTime);

          if (remainingTime > 0) {
            await new Promise(resolve => setTimeout(resolve, remainingTime));
          }
          
          store.setIsGenerating(false);
          
          // ✅ NOUVEAU : Si on arrive ici sans erreur critique, on considère la génération comme réussie
          // (Tu pourras affiner cela plus tard en vérifiant la réponse de launchGeneration)
          store.setIsGenerationComplete(true);
        }

        return true;
      } catch (error) {
        store.setIsGenerating(false);
        store.setWizardError(error instanceof Error ? error.message : "Échec de la validation du résumé.");
        return false;
      }
    }
  }
};

// ============================================================================
// UTILITAIRES D'ADAPTATION (Pont entre Noms et Numéros)
// ============================================================================

/**
 * Retourne le nombre total d'étapes (dynamique, basé sur le tableau d'ordre)
 */
export const getTotalSteps = (): number => WIZARD_STEP_ORDER.length;

/**
 * Convertit un numéro d'étape (1-based) en identifiant sémantique
 */
export const getStepIdByNumber = (num: number): StepId | undefined => {
  return WIZARD_STEP_ORDER[num - 1];
};

/**
 * Convertit un identifiant sémantique en numéro d'étape (1-based)
 */
export const getStepNumberById = (id: StepId): number => {
  return WIZARD_STEP_ORDER.indexOf(id) + 1;
};

/**
 * Génère le format Record<number, number[]> attendu par useGenesisWizard
 * en se basant sur les dépendances (skipCascade) définies dans la config.
 */
export const getSkippableStepsConfig = (): Record<number, number[]> => {
  const config: Record<number, number[]> = {};
  
  WIZARD_STEP_ORDER.forEach((stepId, index) => {
    const stepConfig = WIZARD_STEP_CONFIG[stepId];
    const stepNum = index + 1;
    
    // On vérifie si la propriété 'skipCascade' existe explicitement dans la config de l'étape
    if (stepConfig && 'skipCascade' in stepConfig) {
      config[stepNum] = (stepConfig.skipCascade || []).map(cascadeId => getStepNumberById(cascadeId));
    }
  });
  
  return config;
};