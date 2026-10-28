<template>
  <StepperPopup
    title="Créer un nouveau projet"
    :current-step="props.currentStep"
    :total-steps="props.totalSteps"
    size="full"
    :content-class="stepContentClass"
    :is-skippable="props.isSkippable"
    :show-back-to-summary="props.hasReachedSummary && currentStepId !== 'summary'"
    :visited-steps="props.visitedSteps"
    :step-labels="props.stepLabels"

    :disable-next="currentStepId === 'summary' && !isSummaryComplete"
    :is-loading="store.isGenerating"
    :hide-footer="store.isGenerationComplete"

    :show-tutorial-trigger="true" 


    @close="handleClose"
    @previous="emit('previous')"
    @next="emit('next')"
    @skip="emit('skip')"
    @back-to-summary="emit('back-to-summary')"
    @go-to-step="(step) => emit('go-to-step', step)"
  >
    <!-- ÉTAPE : FRAMEWORK -->
    <FrameworkLayout
      v-if="currentStepId === 'framework_select'"
      v-bind="frameworkLayoutProps"
      :group-by="frameworkGroupBy"
      :languages="languages"
      @back="emit('close')"
      @open-filter="openFilter"
      @close-filter="closeFilter"
      @close-detail="closeDetail"
      @select-replace="handleReplaceSelection"
      @close-replace="cancelReplace"
      @select="handleSelectWrapper"
      @info="handleInfo"
      @update:search-value="setSearch"
      @update:display-mode="setDisplayMode"
      @update:mode="handleModeChange"
      @update:filters="setFilters"
      @select-framework="(fw: Framework) => emit('select-framework', fw)"
      @compare="comparator.open"
      @close-compare="comparator.close"
      @update:group-by="handleUpdateFrameworkGroupBy"
    />

    <!-- ÉTAPE : CONFIGURATION PROJET -->
    <ProjectConfigView 
      v-else-if="currentStepId === 'project_config'" 
      @request-folder-path="handleRequestFolderPath" 
    />

    <!-- ÉTAPE : SÉLECTION BASE DE DONNÉES -->
    <DatabaseLayout 
      v-else-if="currentStepId === 'database_select'"
      v-bind="databaseLayoutProps"
      @back="emit('close')"
      @update:display-mode="setDisplayModeDb"
      @update:mode="handleModeChangeDb"
      @select-replace="handleReplaceSelectionDb"
      @close-replace="cancelReplaceDb"
      @select="handleSelectWrapperDb"
      @select-database="(db: DatabaseEngineDto) => emit('select-database', db)"

      @info="(engine) => dbDetailEngine = engine"
      @close-detail="dbDetailEngine = null"

      @compare="dbComparator.open"
      @close-compare="dbComparator.close"
    />

    <!-- ÉTAPE : CONFIGURATION BASE DE DONNÉES -->
    <DatabaseConfigView 
      v-else-if="currentStepId === 'database_config'" 
      @test-connection-error="handleChildError" 
    />

    <!-- ÉTAPE : SCRIPT / IA -->
    <ScriptConfigView 
      v-else-if="currentStepId === 'script'" 
      @request-file-path="handleRequestFilePath" 
    />

    <!-- ÉTAPE : SÉLECTION TABLES/VUES -->
    <GenerationConfigurationAlt v-else-if="currentStepId === 'generation'" />

    <!-- ÉTAPE : RELATIONS -->
    <RelationConfigView v-else-if="currentStepId === 'relations'" />

    <!-- ÉTAPE : SÉLECTION FRONTEND -->
    <!-- ÉTAPE : SÉLECTION FRONTEND -->
    <FrontendLayout 
      v-else-if="currentStepId === 'frontend_select'"
      v-bind="frontendLayoutProps"
      @back="emit('close')"
      @update:search-value="setSearchFe"
      @update:display-mode="setDisplayModeFe"
      @update:mode="handleModeChangeFe"
      @select-replace="handleReplaceSelectionFe"
      @close-replace="cancelReplaceFe"
      @select="handleSelectWrapperFe"
      @select-frontend="(fe: FrontendFramework) => emit('select-frontend', fe)"
      
      @info="(fw) => feDetailFramework = fw"
      @close-detail="feDetailFramework = null"
      
      @compare="feComparator.open"
      @close-compare="feComparator.close"
    />
    
    <!-- ÉTAPE : CONFIGURATION LAYOUT FRONTEND -->
    <FrontendLayoutConfigView 
      v-else-if="currentStepId === 'frontend_config'" 
      @request-file-path="handleRequestFilePath" 
    />

    <RepositoryLayout 
      v-else-if="currentStepId === 'repository_select'"
      v-bind="repositoryLayoutProps"
      @back="emit('close')"
      @select-replace="handleReplaceSelectionRepo"
      @close-replace="cancelReplaceRepo"
      @select="handleSelectWrapperRepo"
      @info="handleInfoRepo"
      @close-detail="closeDetailRepo"
      @compare="repoComparator.open"
      @close-compare="repoComparator.close"
    />

    <!-- ÉTAPE : CONFIGURATION GIT -->
    <RepositoryConfigView 
      v-else-if="currentStepId === 'repository_config'" 
      @test-connection-error="handleChildError" 
    />

    <GenerationSuccessView 
      v-else-if="currentStepId === 'summary' && store.isGenerationComplete"
      @close="emit('close')"
    >
      <!-- ✅ On transmet le slot spécifique à l'environnement vers GenerationSuccessView -->
      <template #actions>
        <slot name="success-actions"></slot>
      </template>
    </GenerationSuccessView>



    <SummaryView 
      v-else-if="currentStepId === 'summary'" 
      :initial-index="props.targetSummaryIndex"
      @request-edit="(idx) => emit('request-edit', idx)"
      @update-summary-complete="(val) => isSummaryComplete = val"
    />

  </StepperPopup>

  <!-- POPUP D'ERREUR GLOBALE -->
  <ErrorPopup
    v-if="showError"
    title="Erreur de configuration"
    :message="errorMessage"
    :stack-trace="errorStackTrace"
    :show-stack-trace="isDevMode"
    size="md"
    @close="clearError"
  />
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import type { Framework, FrontendFramework, DatabaseEngineDto, FileRequestPayload } from '@genesis-labs/shared-types';


// Core Components
import StepperPopup from '@genesis-labs/web-core/core/components/layouts/Popup/StepperPopup.vue';
import ErrorPopup from '@genesis-labs/web-core/core/components/layouts/Popup/ErrorPopup.vue';

// Feature Components & Types
import FrameworkLayout, { type FrameworkLayoutProps } from '@genesis-labs/web-core/features/frameworks/components/FrameworkLayout.vue';
import DatabaseLayout, { type DatabaseLayoutProps } from '@genesis-labs/web-core/features/database/components/DatabaseLayout.vue';
import FrontendLayout, { type FrontendLayoutProps } from '@genesis-labs/web-core/features/frontend/components/FrontendLayout.vue';
import RepositoryLayout, { type RepositoryLayoutProps } from '@genesis-labs/web-core/features/repository/components/RepositoryLayout.vue';
import GenerationSuccessView from './steps/GenerationSuccessView.vue';

import SummaryView from './steps/SummaryView.vue';

import { 
  ProjectConfigView, DatabaseConfigView, ScriptConfigView, GenerationConfigurationAlt,
  RelationConfigView, FrontendLayoutConfigView, RepositoryConfigView
} from '@genesis-labs/web-core/features/generator/components/steps';

// Feature Composables (Ponts vers le Wizard)
import { useWizardFramework } from '@genesis-labs/web-core/features/frameworks/composables/useWizardFramework';
import { useWizardDatabase } from '@genesis-labs/web-core/features/database/composables/useWizardDatabase';
import { useWizardFrontend } from '@genesis-labs/web-core/features/frontend/composables/useWizardFrontend';

// Store
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';

import { getStepIdByNumber } from '@genesis-labs/web-core/features/generator/composables/wizard-step-config';
import { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import { useWizardRepository } from '@genesis-labs/web-core/features/repository/composables/useWizardRepository';


// ============================================================================
// 1. PROPS & EMITS
// ============================================================================
const props = defineProps<{ 
  currentStep: number; 
  totalSteps: number; 
  isSkippable?: boolean;
  targetSummaryIndex?: number;
  hasReachedSummary?: boolean;
  visitedSteps?: Set<number>;
  stepLabels?: string[];
  isGenerating?: boolean;
}>();

const emit = defineEmits<{
  close: []; 
  next: []; 
  previous: []; 
  skip: [];
  'back-to-summary': [];
  'request-edit': [index: number];
  'request-tutorial': [];
  'go-to-step': [step: number];
  'open-project': [];
  'select-framework': [framework: Framework];
  'select-frontend': [framework: FrontendFramework];
  'select-database': [engine: DatabaseEngineDto];
  'select-repository': [provider: RepositoryProviderMetadata];
  'request-folder-path': [];
  'request-file-path': [payload: FileRequestPayload];
}>();

// ============================================================================
// 2. STORE & ÉTAT LOCAL
// ============================================================================
const store = useGeneratorStore();
const stepContentClass = 'overflow-y-auto';

const frameworkGroupBy = ref<keyof Framework | null>(null);

const feDetailFramework = ref<FrontendFramework | null>(null);
const dbDetailEngine = ref<DatabaseEngineDto | null>(null);
const repoDetailProvider = ref<RepositoryProviderMetadata | null>(null);


const showError = ref(false);
const errorMessage = ref('');
const errorStackTrace = ref('');
const isDevMode = import.meta.env.DEV;
const isSummaryComplete = ref(false);

// ============================================================================
// 3. INJECTION DES COMPOSABLES (PONTS WIZARD)
// ============================================================================
const {
  searchQuery, displayMode, compareMode, frameworks, selectedId, frameworkSlots,
  replaceOptions, showReplacePopup, mouseX, mouseY, filters, detailFramework,
  isFilterOpen, pendingFramework, isLoading, setSearch, setFilters,
  setDisplayMode, handleModeChange, handleSelectWrapper, handleReplaceSelection,
  cancelReplace, openFilter, closeFilter, closeDetail, handleInfo, languages,
  compare, comparator
} = useWizardFramework((framework: Framework) => {
  emit('select-framework', framework);
});

const {
  engines, selectedId: dbSelectedId, databaseSlots, displayMode: dbDisplayMode,
  compareMode: dbCompareMode, showReplacePopup: dbShowReplacePopup, mouseX: dbMouseX,
  mouseY: dbMouseY, isLoading: dbIsLoading, replaceOptions: dbReplaceOptions,
  handleSelectWrapper: handleSelectWrapperDb, handleReplaceSelection: handleReplaceSelectionDb,
  cancelReplace: cancelReplaceDb, handleModeChange: handleModeChangeDb, setDisplayMode: setDisplayModeDb,
  compare: dbCompare,
  comparator: dbComparator
} = useWizardDatabase((engine: DatabaseEngineDto) => {
  emit('select-database', engine);
});

const {
  frontends, selectedId: feSelectedId, frontendFrameworkSlots: feSlots, displayMode: feDisplayMode,
  searchQuery: feSearchQuery, compareMode: feCompareMode, showReplacePopup: feShowReplacePopup,
  mouseX: feMouseX, mouseY: feMouseY, isLoading: feIsLoading, replaceOptions: feReplaceOptions,
  handleSelectWrapper: handleSelectWrapperFe, handleReplaceSelection: handleReplaceSelectionFe,
  cancelReplace: cancelReplaceFe, handleModeChange: handleModeChangeFe, setSearch: setSearchFe,
  setDisplayMode: setDisplayModeFe,
  // ✅ AJOUT : Comparaison pour le frontend
  compare: feCompare,
  comparator: feComparator
} = useWizardFrontend((framework: FrontendFramework) => {
  emit('select-frontend', framework);
});

const {
  providers: repoProviders,
  selectedId: repoSelectedId,
  displayMode: repoDisplayMode,
  compareMode: repoCompareMode,
  providerSlots: repoProviderSlots,
  searchQuery: repoSearchQuery,
  isLoading: repoIsLoading,
  showReplacePopup: repoShowReplacePopup,
  pendingProvider: repoPendingProvider,
  mouseX: repoMouseX,
  mouseY: repoMouseY,
  compare: repoCompare,
  comparator: repoComparator,
  handleSelectWrapper: handleSelectWrapperRepo,
  handleReplaceSelection: handleReplaceSelectionRepo,
  cancelReplace: cancelReplaceRepo,
  closeDetail: closeDetailRepo,
  handleInfo: handleInfoRepo
} = useWizardRepository((provider: RepositoryProviderMetadata) => {
  emit('select-repository', provider);
});

// ============================================================================
// 4. COMPUTEDS (IDENTIFIANT D'ÉTAPE & PROPS)
// ============================================================================

const currentStepId = computed(() => getStepIdByNumber(props.currentStep));

const frameworkLayoutProps = computed<FrameworkLayoutProps>(() => ({
  searchQuery: searchQuery.value,
  displayMode: displayMode.value,
  compareMode: compareMode.value,
  searchPlaceholder: 'Rechercher par nom, core, type...',
  showBackButton: false,
  frameworks: frameworks.value,
  selectedId: selectedId.value,
  frameworkSlots: frameworkSlots.value,
  replaceOptions: replaceOptions.value,
  showReplacePopup: showReplacePopup.value,
  mouseX: mouseX.value,
  mouseY: mouseY.value,
  filters: filters.value,
  detailFramework: detailFramework.value,
  isFilterOpen: isFilterOpen.value,
  pendingFramework: pendingFramework.value,
  isLoading: isLoading.value,
  groupBy: frameworkGroupBy.value,

  canCompare: comparator.canOpen.value,
  compareCount: compare.compareCount.value,
  comparing: comparator.isOpen.value,
  comparatorEditors: comparator.editors.value,
  comparatorEditorIds: comparator.initialEditorIds.value
}));

const databaseLayoutProps = computed<DatabaseLayoutProps>(() => ({
  engines: engines.value,
  selectedId: dbSelectedId.value,
  databaseSlots: databaseSlots.value,
  displayMode: dbDisplayMode.value,
  compareMode: dbCompareMode.value,
  showBackButton: false,
  replaceOptions: dbReplaceOptions.value,
  showReplacePopup: dbShowReplacePopup.value,
  mouseX: dbMouseX.value,
  mouseY: dbMouseY.value,
  isLoading: dbIsLoading.value,
  
  detailEngine: dbDetailEngine.value,

  canCompare: dbComparator.canOpen.value,
  compareCount: dbCompare.compareCount.value,
  comparing: dbComparator.isOpen.value,
  comparatorEditors: dbComparator.editors.value,
  comparatorEditorIds: dbComparator.initialEditorIds.value
}));

const frontendLayoutProps = computed<FrontendLayoutProps>(() => ({
  frontends: frontends.value,
  selectedId: feSelectedId.value,
  frontendFrameworkSlots: feSlots.value,
  displayMode: feDisplayMode.value,
  searchQuery: feSearchQuery.value,
  compareMode: feCompareMode.value,
  showBackButton: false,
  replaceOptions: feReplaceOptions.value,
  showReplacePopup: feShowReplacePopup.value,
  mouseX: feMouseX.value,
  mouseY: feMouseY.value,
  isLoading: feIsLoading.value,
  
  detailFramework: feDetailFramework.value,

  // ✅ AJOUT : Props de comparaison pour le frontend
  canCompare: feComparator.canOpen.value,
  compareCount: feCompare.compareCount.value,
  comparing: feComparator.isOpen.value,
  comparatorEditors: feComparator.editors.value,
  comparatorEditorIds: feComparator.initialEditorIds.value
}));

const repositoryLayoutProps = computed<RepositoryLayoutProps>(() => ({
  providers: repoProviders.value,
  selectedId: repoSelectedId.value,
  displayMode: repoDisplayMode.value,
  compareMode: repoCompareMode.value,
  providerSlots: repoProviderSlots.value,
  searchQuery: repoSearchQuery.value,
  showReplacePopup: repoShowReplacePopup.value,
  replaceOptions: [], // À adapter si tu utilises le replace pour les repos
  mouseX: repoMouseX.value,
  mouseY: repoMouseY.value,
  isLoading: repoIsLoading.value,
  detailProvider: repoDetailProvider.value,
  pendingProvider: repoPendingProvider.value,
  canCompare: repoComparator.canOpen.value,
  compareCount: repoCompare.compareCount.value,
  comparing: repoComparator.isOpen.value,
  comparatorEditors: repoComparator.editors.value,
  comparatorEditorIds: repoComparator.initialEditorIds.value
}));

// ============================================================================
// 5. ACTIONS & HANDLERS
// ============================================================================
function handleUpdateFrameworkGroupBy(value: string | null) {
  frameworkGroupBy.value = value as keyof Framework | null;
}

function handleClose() { 
  emit('close'); 
}

function handleRequestFolderPath() {
  emit('request-folder-path'); 
}

function handleRequestFilePath(payload: FileRequestPayload) { 
  emit('request-file-path', payload); 
}

function handleChildError(message: string) {
  errorMessage.value = message;
  errorStackTrace.value = ''; 
  showError.value = true;
}

function clearError() {
  showError.value = false;
  errorMessage.value = '';
  errorStackTrace.value = '';
  store.clearWizardError(); 
}

// ============================================================================
// 6. WATCHERS
// ============================================================================
watch(() => store.wizardError, (newError) => {
  if (newError) {
    errorMessage.value = newError;
    showError.value = true;
  }
}, { immediate: true });
</script>