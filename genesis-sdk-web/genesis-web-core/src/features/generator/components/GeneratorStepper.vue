<template>
  <StepperPopup
    title="Créer un nouveau projet"
    :current-step="props.currentStep"
    :total-steps="props.totalSteps"
    size="full"
    :content-class="stepContentClass"
    :is-skippable="props.isSkippable" 
    @close="handleClose"
    @previous="emit('previous')"
    @next="emit('next')"
    @skip="emit('skip')" 
  >
    <!-- ÉTAPE : FRAMEWORK -->
    <FrameworkLayout
      v-if="currentStepId === 'framework'"
      v-bind="frameworkLayoutProps"
      :group-by="frameworkGroupBy"
      :languages="languages"
      @update:group-by="frameworkGroupBy = $event"
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
    <GenerationConfigurationAlt v-else-if="currentStepId === 'tables'" />

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
      v-else-if="currentStepId === 'frontend_layout'" 
      @request-file-path="handleRequestFilePath" 
    />

    <!-- ÉTAPE : CONFIGURATION GIT -->
    <GitConfigView v-else-if="currentStepId === 'git_config'" />

    <SummaryView v-else-if="currentStepId === 'summary'" />

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
import SummaryView from './steps/SummaryView.vue';

import { 
  ProjectConfigView, DatabaseConfigView, ScriptConfigView, GenerationConfigurationAlt,
  RelationConfigView, FrontendLayoutConfigView, GitConfigView 
} from '@genesis-labs/web-core/features/generator/components/steps';

// Feature Composables (Ponts vers le Wizard)
import { useWizardFramework } from '@genesis-labs/web-core/features/frameworks/composables/useWizardFramework';
import { useWizardDatabase } from '@genesis-labs/web-core/features/database/composables/useWizardDatabase';
import { useWizardFrontend } from '@genesis-labs/web-core/features/frontend/composables/useWizardFrontend';

// Store
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';

// ✅ IMPORT POUR LE MAPPING SÉMANTIQUE
import { getStepIdByNumber } from '@genesis-labs/web-core/features/generator/composables/wizard-step-config';

// ============================================================================
// 1. PROPS & EMITS
// ============================================================================
const props = defineProps<{ 
  currentStep: number; 
  totalSteps: number; 
  isSkippable?: boolean; 
}>();

const emit = defineEmits<{
  close: []; 
  next: []; 
  previous: []; 
  skip: [];
  'select-framework': [framework: Framework];
  'select-frontend': [framework: FrontendFramework];
  'select-database': [engine: DatabaseEngineDto];
  'request-folder-path': [];
  'request-file-path': [payload: FileRequestPayload];
}>();

// ============================================================================
// 2. STORE & ÉTAT LOCAL
// ============================================================================
const store = useGeneratorStore();
const stepContentClass = 'overflow-y-auto';

const frameworkGroupBy = ref<keyof Framework | null>('coreFramework');

const feDetailFramework = ref<FrontendFramework | null>(null);
const dbDetailEngine = ref<DatabaseEngineDto | null>(null);

const showError = ref(false);
const errorMessage = ref('');
const errorStackTrace = ref('');
const isDevMode = import.meta.env.DEV;

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

// ============================================================================
// 5. ACTIONS & HANDLERS
// ============================================================================
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