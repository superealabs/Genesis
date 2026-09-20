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
        <FrameworksView
            v-if="props.currentStep === 1"
            :showBackButton="false"
            @select="handleFrameworkSelect"
        />

        <ProjectConfigView
            v-else-if="props.currentStep === 2"
            @request-folder-path="handleRequestFolderPath"
        />

        <DatabaseSelection
            v-else-if="props.currentStep === 3"
            @select="handleDatabaseSelect"
        />

        <DatabaseConfigView
            v-else-if="props.currentStep === 4"
            @test-connection-error="handleChildError"    
        />

        <ScriptConfigView
            v-else-if="props.currentStep === 5"
            @request-file-path="handleRequestFilePath"
        />
        
        <!-- <GenerationConfiguration v-else-if="props.currentStep === 6" /> -->
        <GenerationConfigurationAlt v-else-if="props.currentStep === 6" />

        <RelationConfigView v-else-if="props.currentStep === 7" />
        <FrontEndSelectionView
            v-else-if="props.currentStep === 8"
            :showBackButton="false"
            @select="handleFrontendSelect" 
        />

        <FrontendLayoutConfigView
            v-else-if="props.currentStep === 9"
            @request-file-path="handleRequestFilePath"
        />
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
import { watch } from 'vue';
import StepperPopup from '@genesis-labs/web-core/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@genesis-labs/web-core/features/frameworks/views/FrameworksView.vue';
import FrontEndSelectionView from '@genesis-labs/web-core/features/frontend/views/FrontEndSelectionView.vue';
import { useGeneratorStore } from '@genesis-labs/web-core/features/generator/store/useGenerator.store';

import { 
    ProjectConfigView, 
    DatabaseConfigView, 
    ScriptConfigView, 
    // GenerationConfiguration,
    GenerationConfigurationAlt,
    RelationConfigView, 
    FrontendLayoutConfigView, 
    GitConfigView 
} from '@genesis-labs/web-core/features/generator/components/steps';

const store = useGeneratorStore();


import ErrorPopup from '@genesis-labs/web-core/core/components/layouts/Popup/ErrorPopup.vue';
import DatabaseSelection from '@genesis-labs/web-core/features/database/views/DatabaseSelection.vue';
import { DatabaseEngineDto, FileRequestPayload } from '@genesis-labs/shared-types';

import type { Framework } from '@genesis-labs/web-core/features/frameworks/types/framework.types';
import type { FrontendFramework } from '@genesis-labs/web-core/features/frontend/types/frontend.types.ts';
import { computed, ref } from 'vue';

// L'étape 4 (ScriptConfigView) gère son propre scroll interne
const stepContentClass = computed(() => 'overflow-y-auto');

const props = defineProps<{
    currentStep: number;
    totalSteps: number;
    isSkippable?: boolean;
}>();

const emit = defineEmits<{
    close: [];
    next: [];
    previous: [];
    skip: []; // NOUVEL EMIT RELAYÉ VERS LE PARENT
    'select-framework': [framework: Framework];
    'select-frontend': [framework: FrontendFramework];
    'request-folder-path': [];
    'request-file-path': [payload: FileRequestPayload];
    'select-database': [engine: DatabaseEngineDto];
}>();

// ← handleClose manquait
function handleClose() {
    emit('close');
}

// Dans genesis-sdk-web/genesis-web-core/src/features/generator/components/GeneratorStepper.vue

function handleFrameworkSelect(payload: any) {
    console.log("🔍 [Stepper] Payload brut reçu :", payload);
    
    // Extrait le framework, que l'enfant l'ait envoyé directement ou dans une propriété .framework
    const framework = payload?.framework ? payload.framework : payload;
    
    console.log("🚀 [Stepper] Framework extrait et émis vers le parent :", framework?.name);
    emit('select-framework', framework);
}

function handleFrontendSelect(result: { action: string; framework: FrontendFramework; event?: MouseEvent }) {
    emit('select-frontend', result.framework);
}

function handleDatabaseSelect(result: { action: string; engine: DatabaseEngineDto; event?: MouseEvent }) {
    emit('select-database', result.engine);
}

function handleRequestFolderPath() {
    emit('request-folder-path');
}

function handleRequestFilePath(payload: FileRequestPayload) {
    emit('request-file-path', payload);
}


const showError = ref(false);
const errorMessage = ref('');
const errorStackTrace = ref('');
const isDevMode = import.meta.env.DEV;

function handleChildError(message: string) {
    // Conserver pour les erreurs spécifiques de DatabaseConfigView
    errorMessage.value = message;
    errorStackTrace.value = ''; 
    showError.value = true;
}

function clearError() {
    showError.value = false;
    errorMessage.value = '';
    errorStackTrace.value = '';
    // ✅ IMPORTANT : On nettoie aussi le store pour éviter que l'erreur réapparaisse
    store.clearWizardError(); 
}


watch(() => store.wizardError, (newError) => {
    if (newError) {
        errorMessage.value = newError;
        showError.value = true;
    }
}, { immediate: true });


</script>