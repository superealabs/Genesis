<template>
    <StepperPopup
        title="Créer un nouveau projet"
        :currentStep="props.currentStep"
        :totalSteps="props.totalSteps"
        size="full"
        :content-class="stepContentClass"
        :is-skippable="props.isSkippable" 
        @close="handleClose"
        @previous="emit('previous')"
        @next="emit('next')"
        @skip="emit('skip')" 
    >
        <!-- ═══ ÉTAPE 1 : FRAMEWORK (Optimisé avec v-bind) ═══ -->
        <FrameworkLayout
            v-if="props.currentStep === 1"
            v-bind="frameworkLayoutProps"
            @back="emit('close')"
            @openFilter="openFilter"
            @closeFilter="closeFilter"
            @closeDetail="closeDetail"
            @select-replace="handleReplaceSelection"
            @close-replace="cancelReplace"
            @select="handleSelectWrapper"
            @info="handleInfo"
            @update:searchValue="setSearch"
            @update:displayMode="setDisplayMode"
            @update:mode="handleModeChange"
            @update:filters="setFilters"
        />

        <ProjectConfigView v-else-if="props.currentStep === 2" @request-folder-path="handleRequestFolderPath" />

        <!-- ═══ ÉTAPE 3 : BASE DE DONNÉES (Nouvelle architecture optimisée) ═══ -->
        <DatabaseLayout 
            v-else-if="props.currentStep === 3"
            v-bind="databaseLayoutProps"
            @back="emit('close')"
            @update:displayMode="setDisplayModeDb"
            @update:mode="handleModeChangeDb"
            @select-replace="handleReplaceSelectionDb"
            @close-replace="cancelReplaceDb"
            @select="handleSelectWrapperDb"
        />

        <DatabaseConfigView v-else-if="props.currentStep === 4" @test-connection-error="handleChildError" />
        <ScriptConfigView v-else-if="props.currentStep === 5" @request-file-path="handleRequestFilePath" />
        <GenerationConfigurationAlt v-else-if="props.currentStep === 6" />
        <RelationConfigView v-else-if="props.currentStep === 7" />


        <!-- ═══ ÉTAPE 8 : SÉLECTION FRONTEND (Nouvelle architecture optimisée) ═══ -->
        <FrontendLayout 
            v-else-if="props.currentStep === 8"
            v-bind="frontendLayoutProps"
            @back="emit('close')"
            @update:searchValue="setSearchFe"
            @update:displayMode="setDisplayModeFe"
            @update:mode="handleModeChangeFe"
            @select-replace="handleReplaceSelectionFe"
            @close-replace="cancelReplaceFe"
            @select="handleSelectWrapperFe"
            @info="handleInfoFe"
        />
        
        
        
        <FrontendLayoutConfigView v-else-if="props.currentStep === 9" @request-file-path="handleRequestFilePath" />
        <GitConfigView v-else-if="props.currentStep === 10" />
    </StepperPopup>

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
import StepperPopup from '@genesis-labs/web-core/core/components/layouts/Popup/StepperPopup.vue';
import ErrorPopup from '@genesis-labs/web-core/core/components/layouts/Popup/ErrorPopup.vue';

import DatabaseLayout, { type DatabaseLayoutProps } from '@genesis-labs/web-core/features/database/components/DatabaseLayout.vue';
import { useWizardDatabase } from '@genesis-labs/web-core/features/database/composables/useWizardDatabase';

// IMPORTS EXISTANTS
import FrameworkLayout, { type FrameworkLayoutProps } from '@genesis-labs/web-core/features/frameworks/components/FrameworkLayout.vue';
import { useWizardFramework } from '@genesis-labs/web-core/features/frameworks/composables/useWizardFramework';


import { 
    ProjectConfigView, DatabaseConfigView, ScriptConfigView, GenerationConfigurationAlt,
    RelationConfigView, FrontendLayoutConfigView, GitConfigView 
} from '@genesis-labs/web-core/features/generator/components/steps';

import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { DatabaseEngineDto, FileRequestPayload } from '@genesis-labs/shared-types';
import type { Framework, FrontendFramework } from '@genesis-labs/shared-types';


import FrontendLayout, { type FrontendLayoutProps } from '../../frontend/components/FrontendLayout.vue';
import { useWizardFrontend } from '../../frontend/composables/useWizardFrontend';

const store = useGeneratorStore();
const stepContentClass = 'overflow-y-auto';

const props = defineProps<{ currentStep: number; totalSteps: number; isSkippable?: boolean }>();

const emit = defineEmits<{
    close: []; next: []; previous: []; skip: [];
    'select-framework': [framework: Framework];
    'select-frontend': [framework: FrontendFramework];
    'select-database': [engine: DatabaseEngineDto];
    'request-folder-path': [];
    'request-file-path': [payload: FileRequestPayload];
}>();

// ═══ 1. DÉSTRUCTURATION FRAMEWORK ═══
const {
    searchQuery, displayMode, compareMode, frameworks, selectedId, frameworkSlots,
    replaceOptions, showReplacePopup, mouseX, mouseY, filters, detailFramework,
    isFilterOpen, pendingFramework, isLoading, setSearch, setFilters,
    setDisplayMode, handleModeChange, handleSelectWrapper, handleReplaceSelection,
    cancelReplace, openFilter, closeFilter, closeDetail, handleInfo
} = useWizardFramework((framework: Framework) => {
    emit('select-framework', framework);
});

// ═══ 2. DÉSTRUCTURATION DATABASE (NOUVEAU) ═══
const {
    engines,
    selectedId: dbSelectedId,
    databaseSlots,
    displayMode: dbDisplayMode,
    compareMode: dbCompareMode,
    showReplacePopup: dbShowReplacePopup,
    mouseX: dbMouseX,
    mouseY: dbMouseY,
    isLoading: dbIsLoading,
    replaceOptions: dbReplaceOptions,
    handleSelectWrapper: handleSelectWrapperDb,
    handleReplaceSelection: handleReplaceSelectionDb,
    cancelReplace: cancelReplaceDb,
    handleModeChange: handleModeChangeDb,
    setDisplayMode: setDisplayModeDb
} = useWizardDatabase((engine: DatabaseEngineDto) => {
    emit('select-database', engine);
});

// ═══ 3. DÉSTRUCTURATION FRONTEND (NOUVEAU) ═══
const {
    frontends,
    selectedId: feSelectedId,
    frontendFrameworkSlots: feSlots,
    displayMode: feDisplayMode,
    searchQuery: feSearchQuery,
    compareMode: feCompareMode,
    showReplacePopup: feShowReplacePopup,
    mouseX: feMouseX,
    mouseY: feMouseY,
    isLoading: feIsLoading,
    replaceOptions: feReplaceOptions,
    handleSelectWrapper: handleSelectWrapperFe,
    handleReplaceSelection: handleReplaceSelectionFe,
    cancelReplace: cancelReplaceFe,
    handleModeChange: handleModeChangeFe,
    setSearch: setSearchFe,
    setDisplayMode: setDisplayModeFe,
    handleInfo: handleInfoFe
} = useWizardFrontend((framework: FrontendFramework) => {
    emit('select-frontend', framework);
});

// ═══ 3. OPTIMISATION : Regroupement des props dans des objets réactifs ═══
const frameworkLayoutProps = computed<FrameworkLayoutProps>(() => ({
    searchQuery: searchQuery.value,
    displayMode: displayMode.value,
    compareMode: compareMode.value,
    searchPlaceholder: "Rechercher par nom, core, type...",
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
    isLoading: isLoading.value
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
    isLoading: dbIsLoading.value
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
    isLoading: feIsLoading.value
}));

// ═══ 4. HANDLERS & GESTION DES ERREURS ═══
function handleClose() { emit('close'); }
function handleRequestFolderPath() { emit('request-folder-path'); }
function handleRequestFilePath(payload: FileRequestPayload) { emit('request-file-path', payload); }

const showError = ref(false);
const errorMessage = ref('');
const errorStackTrace = ref('');
const isDevMode = import.meta.env.DEV;

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

watch(() => store.wizardError, (newError) => {
    if (newError) {
        errorMessage.value = newError;
        showError.value = true;
    }
}, { immediate: true });
</script>