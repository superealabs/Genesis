<script setup lang="ts">
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';

import type { DatabaseEngineDto } from '@genesis-labs/shared-types';

import { GeneratorStepper } from '@genesis-labs/web-core/features/generator/manifest';
import { FrontendFramework } from '@genesis-labs/shared-types';

const {
    currentStep,
    totalSteps,
    isCurrentStepSkippable,
    goToPreviousStep,
    goToNextStep,
    setDatabaseEngine,
    setSelectedFrontendFramework,
    reset,
    handleSelectFolderPath,
    handleFileRequest,
    skipCurrentStep,
    stepperData,
    setPendingFramework,
    setPendingDatabaseEngine
} = useGeneratorVsc();

function handleClose() {
    reset();
}

async function handleNextStep() {
    const finalData = await goToNextStep();
    if (finalData) {
        // Déclencher la génération du projet ici si nécessaire
        console.log("Prêt à générer :", finalData);
    }
}

// ═══ ORCHESTRATION UI (Sélection + Avancement) ═══
// Dans packages/webview/src/features/generator/views/GeneratorViewVsc.vue

function onSelectFramework(framework: any) {
    // setFramework(framework);
    setPendingFramework(framework);
    // Vérifier immédiatement après
    console.log('[onSelectFramework] store après setFramework =', stepperData.value.framework);
}

function onSelectDatabase(engine: DatabaseEngineDto) {
    setPendingDatabaseEngine(engine);
}

function onSelectFrontend(framework: FrontendFramework) {
    setSelectedFrontendFramework(framework);
}
</script>

<template>
    <GeneratorStepper
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        :is-skippable="isCurrentStepSkippable" 
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep"
        @skip="skipCurrentStep" 
        @select-framework="onSelectFramework"
        @select-database="onSelectDatabase"
        @select-frontend="onSelectFrontend"
        @request-folder-path="handleSelectFolderPath"
        @request-file-path="handleFileRequest"
    />
</template>