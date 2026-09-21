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

        <!-- Le reste des étapes reste inchangé -->
        <ProjectConfigView v-else-if="props.currentStep === 2" @request-folder-path="handleRequestFolderPath" />
        <DatabaseSelection v-else-if="props.currentStep === 3" @select="handleDatabaseSelect" />
        <DatabaseConfigView v-else-if="props.currentStep === 4" @test-connection-error="handleChildError" />
        <ScriptConfigView v-else-if="props.currentStep === 5" @request-file-path="handleRequestFilePath" />
        <GenerationConfigurationAlt v-else-if="props.currentStep === 6" />
        <RelationConfigView v-else-if="props.currentStep === 7" />
        <FrontEndSelectionView v-else-if="props.currentStep === 8" :showBackButton="false" @select="handleFrontendSelect" />
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
import { ref, watch, computed } from 'vue'; // ✅ 'watch' reste uniquement pour wizardError
import StepperPopup from '@genesis-labs/web-core/core/components/layouts/Popup/StepperPopup.vue';
import ErrorPopup from '@genesis-labs/web-core/core/components/layouts/Popup/ErrorPopup.vue';
import DatabaseSelection from '@genesis-labs/web-core/features/database/views/DatabaseSelection.vue';
import FrontEndSelectionView from '@genesis-labs/web-core/features/frontend/views/FrontEndSelectionView.vue';

import FrameworkLayout, { type FrameworkLayoutProps } from '@genesis-labs/web-core/features/frameworks/components/FrameworkLayout.vue';
import { useWizardFramework } from '@genesis-labs/web-core/features/frameworks/composables/useWizardFramework';

import { 
    ProjectConfigView, DatabaseConfigView, ScriptConfigView, GenerationConfigurationAlt,
    RelationConfigView, FrontendLayoutConfigView, GitConfigView 
} from '@genesis-labs/web-core/features/generator/components/steps';

import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';
import { DatabaseEngineDto, FileRequestPayload } from '@genesis-labs/shared-types';
import type { Framework, FrontendFramework } from '@genesis-labs/shared-types';

const store = useGeneratorStore();
const stepContentClass = 'overflow-y-auto';

const props = defineProps<{ currentStep: number; totalSteps: number; isSkippable?: boolean }>();

const emit = defineEmits<{
    close: []; next: []; previous: []; skip: [];
    'select-framework': [framework: Framework];
    'select-frontend': [framework: FrontendFramework];
    'request-folder-path': [];
    'request-file-path': [payload: FileRequestPayload];
    'select-database': [engine: DatabaseEngineDto];
}>();

// ═══ 1. DÉSTRUCTURATION DU COMPOSABLE (Plus besoin d'appeler initialize manuellement) ═══
const {
    searchQuery, displayMode, compareMode, frameworks, selectedId, frameworkSlots,
    replaceOptions, showReplacePopup, mouseX, mouseY, filters, detailFramework,
    isFilterOpen, pendingFramework, isLoading, setSearch, setFilters,
    setDisplayMode, handleModeChange, handleSelectWrapper, handleReplaceSelection,
    cancelReplace, openFilter, closeFilter, closeDetail, handleInfo
} = useWizardFramework((framework: Framework) => {
    emit('select-framework', framework);
});

// ═══ 2. OPTIMISATION : Regroupement des props dans un objet réactif ═══
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
// Le chargement est désormais géré centralement par onStepEnter dans useGenerator.ts

// ═══ 3. HANDLERS & GESTION DES ERREURS ═══
function handleClose() { emit('close'); }
function handleFrontendSelect(result: { action: string; framework: FrontendFramework; event?: MouseEvent }) { emit('select-frontend', result.framework); }
function handleDatabaseSelect(result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }) { emit('select-database', result.engine); }
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