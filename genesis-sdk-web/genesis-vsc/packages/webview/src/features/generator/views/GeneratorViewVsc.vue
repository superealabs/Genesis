<script setup lang="ts">
import { GeneratorStepper } from '@genesis-labs/core/features/generator/manifest';
import { useGeneratorVsc } from '../composables/useGeneratorVsc';
import type { FileRequestPayload } from '@genesis-labs/core/features/generator/manifest';
import type { Framework } from '@genesis-labs/core/features/frameworks/manifest';  // ← ajouter

const {
    currentStep,
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    setFramework,                  // ← ajouter
    setSelectedFrontendFramework,
    reset,
    handleSelectFolderPath,
    handleSelectAnyFile,
    handleSelectSqlFile
} = useGeneratorVsc();

function handleClose() {
    reset();
}

function handleNextStep() {
    const finalData = goToNextStep();
    if (finalData) {
        // Logique de fin si nécessaire
    }
}

function onSelectFramework(framework: Framework) {   // ← ajouter
    setFramework(framework);
}

function onRequestFolderPath() {
    handleSelectFolderPath();
}

function onRequestFilePath(payload: FileRequestPayload) {
    console.log("handle request filePath from composable of vsc")
    if (payload.field === 'script') {
        handleSelectSqlFile();
    } else {
        handleSelectAnyFile(payload.field, payload.extensions);
    }
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
        @select-frontend="setSelectedFrontendFramework"
        @request-folder-path="onRequestFolderPath"
        @request-file-path="onRequestFilePath"
    />                                               <!-- ← retirer la balise fermante séparée -->
</template>