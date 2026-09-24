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
    <!-- ÉTAPE 1 : FRAMEWORK -->
    <FrameworkLayout
      v-if="props.currentStep === 1"
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
    />

    <!-- ÉTAPE 2 : CONFIGURATION PROJET -->
    <ProjectConfigView 
      v-else-if="props.currentStep === 2" 
      @request-folder-path="handleRequestFolderPath" 
    />

    <!-- ÉTAPE 3 : SÉLECTION BASE DE DONNÉES -->
    <DatabaseLayout 
      v-else-if="props.currentStep === 3"
      v-bind="databaseLayoutProps"
      @back="emit('close')"
      @update:display-mode="setDisplayModeDb"
      @update:mode="handleModeChangeDb"
      @select-replace="handleReplaceSelectionDb"
      @close-replace="cancelReplaceDb"
      @select="handleSelectWrapperDb"
    />

    <!-- ÉTAPE 4 : CONFIGURATION BASE DE DONNÉES -->
    <DatabaseConfigView 
      v-else-if="props.currentStep === 4" 
      @test-connection-error="handleChildError" 
    />

    <!-- ÉTAPE 5 : SCRIPT / IA -->
    <ScriptConfigView 
      v-else-if="props.currentStep === 5" 
      @request-file-path="handleRequestFilePath" 
    />

    <!-- ÉTAPE 6 : SÉLECTION TABLES/VUES -->
    <GenerationConfigurationAlt v-else-if="props.currentStep === 6" />

    <!-- ÉTAPE 7 : RELATIONS -->
    <RelationConfigView v-else-if="props.currentStep === 7" />

    <!-- ÉTAPE 8 : SÉLECTION FRONTEND -->
    <FrontendLayout 
      v-else-if="props.currentStep === 8"
      v-bind="frontendLayoutProps"
      @back="emit('close')"
      @update:search-value="setSearchFe"
      @update:display-mode="setDisplayModeFe"
      @update:mode="handleModeChangeFe"
      @select-replace="handleReplaceSelectionFe"
      @close-replace="cancelReplaceFe"
      @select="handleSelectWrapperFe"
      @info="handleInfoFe"
    />
    
    <!-- ÉTAPE 9 : CONFIGURATION LAYOUT FRONTEND -->
    <FrontendLayoutConfigView 
      v-else-if="props.currentStep === 9" 
      @request-file-path="handleRequestFilePath" 
    />

    <!-- ÉTAPE 10 : CONFIGURATION GIT -->
    <GitConfigView v-else-if="props.currentStep === 10" />

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

// État de regroupement spécifique au framework (persisté pendant la session du wizard)
const frameworkGroupBy = ref<keyof Framework | null>('coreFramework'); 

// État de gestion des erreurs globales du wizard
const showError = ref(false);
const errorMessage = ref('');
const errorStackTrace = ref('');
const isDevMode = import.meta.env.DEV;

// ============================================================================
// 3. INJECTION DES COMPOSABLES (PONTS WIZARD)
// ============================================================================

/**
 * Pont Framework : Adaptation de la logique métier des frameworks pour le Stepper.
 * Déclenche l'émission 'select-framework' lors de la validation.
 */
const {
  searchQuery, displayMode, compareMode, frameworks, selectedId, frameworkSlots,
  replaceOptions, showReplacePopup, mouseX, mouseY, filters, detailFramework,
  isFilterOpen, pendingFramework, isLoading, setSearch, setFilters,
  setDisplayMode, handleModeChange, handleSelectWrapper, handleReplaceSelection,
  cancelReplace, openFilter, closeFilter, closeDetail, handleInfo, languages
} = useWizardFramework((framework: Framework) => {
  emit('select-framework', framework);
});

/**
 * Pont Database : Adaptation de la logique métier des bases de données pour le Stepper.
 * Déclenche l'émission 'select-database' lors de la validation.
 */
const {
  engines, selectedId: dbSelectedId, databaseSlots, displayMode: dbDisplayMode,
  compareMode: dbCompareMode, showReplacePopup: dbShowReplacePopup, mouseX: dbMouseX,
  mouseY: dbMouseY, isLoading: dbIsLoading, replaceOptions: dbReplaceOptions,
  handleSelectWrapper: handleSelectWrapperDb, handleReplaceSelection: handleReplaceSelectionDb,
  cancelReplace: cancelReplaceDb, handleModeChange: handleModeChangeDb, setDisplayMode: setDisplayModeDb
} = useWizardDatabase((engine: DatabaseEngineDto) => {
  emit('select-database', engine);
});

/**
 * Pont Frontend : Adaptation de la logique métier du frontend pour le Stepper.
 * Déclenche l'émission 'select-frontend' lors de la validation.
 */
const {
  frontends, selectedId: feSelectedId, frontendFrameworkSlots: feSlots, displayMode: feDisplayMode,
  searchQuery: feSearchQuery, compareMode: feCompareMode, showReplacePopup: feShowReplacePopup,
  mouseX: feMouseX, mouseY: feMouseY, isLoading: feIsLoading, replaceOptions: feReplaceOptions,
  handleSelectWrapper: handleSelectWrapperFe, handleReplaceSelection: handleReplaceSelectionFe,
  cancelReplace: cancelReplaceFe, handleModeChange: handleModeChangeFe, setSearch: setSearchFe,
  setDisplayMode: setDisplayModeFe, handleInfo: handleInfoFe
} = useWizardFrontend((framework: FrontendFramework) => {
  emit('select-frontend', framework);
});

// ============================================================================
// 4. COMPUTEDS (CONSTRUCTEURS DE PROPS)
// ============================================================================

/**
 * Regroupe toutes les propriétés réactives nécessaires au FrameworkLayout.
 * Cela évite une imbrication excessive de props dans le template et garantit 
 * que toutes les dépendances sont correctement suivies par Vue.
 */
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
}));

/**
 * Regroupe toutes les propriétés réactives nécessaires au DatabaseLayout.
 */
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
  isLoading: dbIsLoading.value
}));

/**
 * Regroupe toutes les propriétés réactives nécessaires au FrontendLayout.
 */
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
  isLoading: feIsLoading.value
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

/**
 * Capture les erreurs remontées par les composants enfants (ex: échec de test de connexion BDD)
 * et les affiche via le popup d'erreur global.
 */
function handleChildError(message: string) {
  errorMessage.value = message;
  errorStackTrace.value = ''; 
  showError.value = true;
}

/**
 * Réinitialise l'état d'erreur global et nettoie l'erreur stockée dans le store.
 */
function clearError() {
  showError.value = false;
  errorMessage.value = '';
  errorStackTrace.value = '';
  store.clearWizardError(); 
}

// ============================================================================
// 6. WATCHERS
// ============================================================================

/**
 * Surveille les erreurs définies dans le store du générateur (ex: erreurs de validation 
 * lors du clic sur "Suivant") et déclenche l'affichage du popup d'erreur global.
 */
watch(() => store.wizardError, (newError) => {
  if (newError) {
    errorMessage.value = newError;
    showError.value = true;
  }
}, { immediate: true });
</script>