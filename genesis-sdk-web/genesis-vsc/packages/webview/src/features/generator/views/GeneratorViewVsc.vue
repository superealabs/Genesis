<!-- packages/webview/src/features/generator/views/GeneratorViewVsc.vue -->
<script setup lang="ts">
import { useGeneratorVsc } from '@/features/generator/composables/useGeneratorVsc';
import type { DatabaseEngineDto, RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import { GeneratorStepper } from '@genesis-labs/web-core/features/generator/manifest';
import { FrontendFramework } from '@genesis-labs/shared-types';
// ✅ Import des utilitaires de mapping pour la navigation
import { SUMMARY_TO_STEP_MAP, getStepNumberById, STEP_LABELS } from '@genesis-labs/web-core/features/generator/composables/wizard-step-config';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';

import { openProjectVsc } from '@/features/generator/services/vsc-generator.utils';

const emit = defineEmits<{
    close: [];
}>();

const {
    currentStep,
    totalSteps,
    isCurrentStepSkippable,
    goToPreviousStep,
    goToNextStep,
    reset,
    handleSelectFolderPath,
    handleFileRequest,
    skipCurrentStep,
    stepperData,
    setPendingFramework,
    setPendingDatabaseEngine,
    setPendingFrontendFramework,
    setPendingRepositoryProvider,
    visitedSteps,
    backToSummary,
    targetSummaryIndex,
    goToStep,
    hasReachedSummary
} = useGeneratorVsc();

function handleClose() {
    reset();
    emit('close'); 
}

async function handleNextStep() {
    const finalData = await goToNextStep();
    if (finalData) {
        console.log("Prêt à générer :", finalData);
    }
}

function onSelectFramework(framework: any) {
    setPendingFramework(framework);
}

function onSelectDatabase(engine: DatabaseEngineDto) {
    setPendingDatabaseEngine(engine);
}

function onSelectFrontend(framework: FrontendFramework) {
    setPendingFrontendFramework(framework);
}

function onSelectRepository(repoProvider: RepositoryProviderMetadata) {
    setPendingRepositoryProvider(repoProvider);
}

// GESTION DU RETOUR AU RÉSUMÉ
function handleBackToSummary() {
    backToSummary();
}

// GESTION DE LA DEMANDE D'ÉDITION DEPUIS LE RÉSUMÉ (Double-clic sur le rail)
function handleSummaryRequestEdit(summaryIndex: number) {
    const targetStepId = SUMMARY_TO_STEP_MAP[summaryIndex];
    if (targetStepId) {
        const stepNumber = getStepNumberById(targetStepId);
        goToStep(stepNumber);
    }
}

function handleOpenProject() {
    const location = stepperData.value.config.projectLocation;
    const name = stepperData.value.config.projectName;
    // Construction simple du chemin (fonctionne pour la plupart des OS)
    const projectPath = `${location}/${name}`; 
    
    openProjectVsc(projectPath);
}
</script>

<template>
    <GeneratorStepper
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        :is-skippable="isCurrentStepSkippable"
        :target-summary-index="targetSummaryIndex" 
        :has-reached-summary="hasReachedSummary"
        :visited-steps="visitedSteps"
        :step-labels="STEP_LABELS"
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep"
        @skip="skipCurrentStep"
        @back-to-summary="handleBackToSummary"
        @request-edit="handleSummaryRequestEdit"
        @go-to-step="goToStep"
        @select-framework="onSelectFramework"
        @select-database="onSelectDatabase"
        @select-frontend="onSelectFrontend"
        @select-repository="onSelectRepository" 
        @request-folder-path="handleSelectFolderPath"
        @request-file-path="handleFileRequest"
    >
        <template #success-actions>
            <div class="flex gap-3 justify-center w-full max-w-sm">
                <GenesisButton variant="primary" size="lg" @click="handleOpenProject">
                    Ouvrir le projet
                </GenesisButton>
                
                <GenesisButton :variant="'neutral'" size="lg" @click="handleClose">
                    Fermer
                </GenesisButton>
            </div>
        </template>
    </GeneratorStepper>
</template>