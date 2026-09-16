<script setup lang="ts">
import { GeneratorStepper } from '@genesis-labs/web-core/features/generator/manifest';
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';
import type { FileRequestPayload } from '@genesis-labs/web-core/features/generator/manifest';
import type { Framework } from '@genesis-labs/web-core/features/frameworks/manifest';
import type { FrontendFramework } from '@genesis-labs/web-core/features/frontend/manifest';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';

const {
    currentStep,
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    setFramework,
    setDatabaseEngine,
    setSelectedFrontendFramework,
    reset,
    handleSelectFolderPath,
    handleFileRequest
} = useGeneratorVsc();

function handleClose() {
    reset();
}

function handleNextStep() {
    const finalData = goToNextStep();
    if (finalData) {
        // Déclencher la génération du projet ici si nécessaire
        console.log("Prêt à générer :", finalData);
    }
}

// ═══ ORCHESTRATION UI (Sélection + Avancement) ═══
function onSelectFramework(framework: Framework) {
    setFramework(framework);
}

function onSelectDatabase(engine: DatabaseEngineDto) {
    setDatabaseEngine(engine);
}

function onSelectFrontend(framework: FrontendFramework) {
    setSelectedFrontendFramework(framework);
}
</script>

<template>
    <GeneratorStepper
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep"
        @select-framework="onSelectFramework"
        @select-database="onSelectDatabase"
        @select-frontend="onSelectFrontend" 
        @request-folder-path="handleSelectFolderPath" 
        @request-file-path="handleFileRequest" 
    />
</template>