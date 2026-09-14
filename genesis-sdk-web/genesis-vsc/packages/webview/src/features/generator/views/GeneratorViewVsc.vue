<script setup lang="ts">
import { GeneratorStepper } from '@genesis-labs/core/features/generator/manifest';
import { useGeneratorVsc } from '../composables/useGeneratorVsc';
import type { FileRequestPayload } from '@genesis-labs/core/features/generator/manifest';
import type { Framework } from '@genesis-labs/core/features/frameworks/manifest';  // ← ajouter
import { DatabaseEngineDto } from '@genesis-labs/shared-types';

const {
    currentStep,
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    setFramework,
    setSelectedFrontendFramework,
    reset,
    handleSelectFolderPath,
    handleSelectAnyFile,
    handleSelectSqlFile,
    setDatabaseEngine,
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

function onSelectDatabase(engine: DatabaseEngineDto) {
    // 1. Pré-remplit le store avec les métadonnées du moteur (port, driver, etc.)
    setDatabaseEngine(engine);
    console.log(`Selection depuis vsc : ${engine.name}`)
    
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
        @select-database="onSelectDatabase"
        @select-frontend="setSelectedFrontendFramework"
        @request-folder-path="onRequestFolderPath"
        @request-file-path="onRequestFilePath"
    />                                               <!-- ← retirer la balise fermante séparée -->
</template>