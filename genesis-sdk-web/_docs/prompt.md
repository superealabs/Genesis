On a un bug général quand on active le prop includeSubTutorial en true, la webview crash et toute interaction est désactivée :
<template>
  <main class="flex gap-4 h-full overflow-hidden">
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. CONFIGURATION DE BASE -->
      <div class="space-y-4">
        <!-- ✅ AJOUT : En-tête avec le déclencheur du tutoriel -->
        <div class="flex items-center justify-between border-b border-neutral-light pb-2">
          <h3 class="text-lg font-semibold text-text">
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

      <div class="border-t border-secondary"></div>

      <!-- 2. CONFIGURATION DU FRAMEWORK (STACK TECHNIQUE) -->
      <div class="space-y-4">
        <h3 class="text-lg font-semibold text-text border-b border-secondary pb-2">
          Stack Technique ({{ framework?.name || 'Non sélectionné' }})
        </h3>
        
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

      <div class="border-t border-secondary"></div>

      <!-- 3. CONFIGURATION AVANCÉE -->
      <div class="space-y-4">
        <GenesisDisclosure title="Configuration Avancée du Backend" :default-open="false">
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
              <GenesisTutorialItem id="tutorial-advanced-config">
                <GenesisConfigurationPanel
                  :include-sub-tutorial="true"
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

const tutorial = useGenesisTutorial('project-config', [
  { id: 'tutorial-tech-stack-config', text: 'Tutoriel 1' },
  { id: 'tutorial-advanced-config', text: 'Tutoriel 2' }
]);

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

function handleSelectFolderPath() { emit('request-folder-path'); }
</script>

quel en est la cause selon toi ?
genesis-sdk-web\genesis-web-core\src\core\features\tutorial\composables\useGenesisTutorial.ts
import {
    computed, inject, onScopeDispose, provide, reactive, ref, shallowReactive,
    shallowRef, toValue, watch, watchEffect, markRaw,
    type ComputedRef, type InjectionKey, type MaybeRefOrGetter
} from 'vue';

export interface TutorialStep { id: string; text: string; }
export interface TutorialSavedState { enabled: boolean; completed: string[]; }
export interface TutorialPersistence {
    load: () => TutorialSavedState | null;
    save: (state: TutorialSavedState) => void;
}

export interface TutorialContext {
    screenId: string;
    steps: ComputedRef<TutorialStep[]>;
    current: ComputedRef<TutorialStep | null>;
    complete: (stepId: string) => void;
    attach: (stepId: string) => () => void;
    registerGroup: (afterId: string, steps: TutorialStep[]) => () => void;
}

// ── État global ──
const enabled = ref(false);
const completed = reactive(new Set<string>());
const activeScreenId = ref<string | null>(null);

const keyOf = (screenId: string, stepId: string) => `${screenId}:${stepId}`;

interface ScreenHandle { screenId: string; restart: () => void }
const screens = shallowReactive<ScreenHandle[]>([]);

let persistence: TutorialPersistence | null = null;

export function configureTutorialPersistence(adapter: TutorialPersistence) {
    persistence = adapter;
    const saved = adapter.load();
    if (!saved) return;
    completed.clear();
    saved.completed.forEach(key => completed.add(key));
}

watch([enabled, () => [...completed]], () => {
    persistence?.save({ enabled: enabled.value, completed: [...completed] });
});

export function useTutorialSwitch() {
    return {
        enabled: computed(() => enabled.value),
        available: computed(() => screens.length > 0),
        enable: () => { enabled.value = true; },
        disable: () => { 
            enabled.value = false; 
            activeScreenId.value = null;
        },
        toggle: () => { enabled.value = !enabled.value; },
        reset: () => { 
            completed.clear(); 
            enabled.value = true; 
        },
        restartActive: () => screens[screens.length - 1]?.restart()
    };
}

// ── Écran ──
const TUTORIAL_KEY: InjectionKey<TutorialContext> = Symbol('genesis-tutorial');

export function useGenesisTutorial(screenId: string, steps: MaybeRefOrGetter<TutorialStep[]>) {
    const groups = shallowRef<{ key: symbol; afterId: string; steps: TutorialStep[] }[]>([]);
    const present = reactive(new Set<string>());

    const list = computed(() =>
        toValue(steps).flatMap(step => [
            step,
            ...groups.value.filter(g => g.afterId === step.id).flatMap(g => g.steps)
        ])
    );

    const registerGroup = (afterId: string, groupSteps: TutorialStep[]) => {
        const key = Symbol();
        // On marque l'objet comme brut. Vue ne créera pas de Proxy dessus.
        // Cela empêche le système de réactivité de s'emballer quand on ajoute le groupe.
        const newGroup = markRaw({ key, afterId, steps: groupSteps });
        
        // On recrée le tableau avec le nouvel objet brut
        groups.value = [...groups.value, newGroup];
        
        return () => { 
            groups.value = groups.value.filter(g => g.key !== key); 
        };
    };

    // ✅ NOUVEAU : Auto-compléter les étapes qui ne sont pas dans le DOM.
    // Cela empêche une étape cachée (ex: dans un Disclosure fermé) de devenir 
    // soudainement "current" et de geler l'UI (scrollIntoView) lorsqu'elle est enfin montée.
    watch(
        () => list.value.map(s => s.id),
        (stepIds) => {
            if (!enabled.value || activeScreenId.value !== screenId) return;

            stepIds.forEach(stepId => {
                const fullId = keyOf(screenId, stepId);
                if (!completed.has(fullId) && !present.has(stepId)) {
                    completed.add(fullId);
                }
            });
        },
        { immediate: true }
    );

    const current = computed(() => {
        if (!enabled.value || activeScreenId.value !== screenId) return null;
        
        return list.value.find(s =>
            !completed.has(keyOf(screenId, s.id)) && present.has(s.id)
        ) ?? null;
    });

    const context: TutorialContext = {
        screenId,
        steps: list,
        current,
        complete: (stepId) => { completed.add(keyOf(screenId, stepId)); },
        attach: (stepId) => {
            present.add(stepId);
            return () => { present.delete(stepId); };
        },
        registerGroup
    };
    provide(TUTORIAL_KEY, context);

    const restart = () => {
        const prefix = `${screenId}:`;
        [...completed].filter(k => k.startsWith(prefix)).forEach(k => completed.delete(k));
        
        enabled.value = true;
        activeScreenId.value = screenId;
    };

    const handle: ScreenHandle = { screenId, restart };
    screens.push(handle);
    
    onScopeDispose(() => {
        const i = screens.indexOf(handle);
        if (i >= 0) screens.splice(i, 1);
        
        if (activeScreenId.value === screenId) {
            activeScreenId.value = null;
            enabled.value = false;
        }
    });

    return { ...context, restart };
}

export const useTutorialContext = () => inject(TUTORIAL_KEY, null);

export function useTutorialGroup(options: {
    anchorId: MaybeRefOrGetter<string | undefined>;
    include: MaybeRefOrGetter<boolean | undefined>;
    steps: { key: string; text: string }[];
}) {
    const ctx = useTutorialContext();
    const active = computed(() => !!ctx && !!toValue(options.anchorId) && !!toValue(options.include));

    const sub = (key: string) => active.value ? `${toValue(options.anchorId)}/${key}` : undefined;

    watchEffect((onCleanup) => {
        if (!active.value) return;
        onCleanup(ctx!.registerGroup(
            toValue(options.anchorId)!,
            options.steps.map(s => ({ id: sub(s.key)!, text: s.text }))
        ));
    });

    return { sub, active };
}