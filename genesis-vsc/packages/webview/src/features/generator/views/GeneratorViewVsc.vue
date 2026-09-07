<script setup lang="ts">
import { GeneratorStepper } from '@genesis-labs/core/features/generator/manifest';
import { useGeneratorVsc } from '../composables/useGeneratorVsc';

// Si tu as des vues d'étapes spécifiques à VSC, tu les importes ici.
// Sinon, le GeneratorStepper du Core utilisera ses propres vues internes.

const {
    currentStep,
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    handleFrameworkSelect,
    setSelectedFrontendFramework,
    reset,
    // Actions spécifiques VSC
    handleSelectFolderPath,
    handleSelectScriptPath
} = useGeneratorVsc();

function handleClose() {
    reset();
    // Logique pour fermer le popup/panel de l'extension
}

function handleNextStep() {
    const finalData = goToNextStep();
    if (finalData) {
        // Logique de fin si nécessaire, ou le stepper du core gère l'émission
    }
}
</script>

<template>
    <!-- Nous utilisons le composant du Core -->
    <GeneratorStepper
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep"
        @select-framework="handleFrameworkSelect"
        @select-frontend="setSelectedFrontendFramework"
    >
        <!-- ✅ INJECTION VSC : Si le Core Stepper ou ses enfants exposent des slots, 
             c'est ici que nous injectons les boutons spécifiques à VS Code -->
        
        <!-- Exemple hypothétique si ProjectConfigView a un slot 'actions' -->
        <!-- <template #project-config-actions>
            <GenesisButton v-if="currentStep === 2" @click="handleSelectFolderPath" size="sm">
                Choisir le dossier local
            </GenesisButton>
        </template> -->
        
    </GeneratorStepper>
</template>