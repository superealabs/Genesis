<template>
    <StepperPopup
        title="Créer un nouveau projet"
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        size="full"
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="handleNextStep" 
    >
        <!-- Étape 1 : Choix du framework -->
        <FrameworksView
            v-if="currentStep === 1"
            :showBackButton="false"
            @select="handleFrameworkSelect"
        />

        <!-- Étape 2 : Configuration du projet -->
        <ProjectConfigView v-else-if="currentStep === 2" />

        <!-- Étape 3 : Configuration de la base de données -->
        <DatabaseConfigView v-else-if="currentStep === 3" />

        <!-- Étape 4 : Configuration du Script -->
        <ScriptConfigView v-else-if="currentStep === 4" />

        <!-- Étape 5 : Sélection des tables et composants -->
        <TableSelectionView v-else-if="currentStep === 5" />

        <!-- Étape 6 : (Placeholder) -->
        <div v-else-if="currentStep === 6" class="p-8 text-center text-text-muted">
            <p>Étape 6 : À définir</p>
        </div>

        <!-- Étape 7 : (Placeholder) -->
        <div v-else-if="currentStep === 7" class="p-8 text-center text-text-muted">
            <p>Étape 7 : À définir</p>
        </div>
    </StepperPopup>
</template>

<script setup lang="ts">
import StepperPopup from '@/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import ProjectConfigView from './ProjectConfigView.vue';
import DatabaseConfigView from './DatabaseConfigView.vue';
import ScriptConfigView from './ScriptConfigView.vue';
import TableSelectionView from './TableSelectionView.vue';
import { useGenerator } from '../composables/useGenerator';

// ✅ defineEmits est ICI, dans le fichier .vue, c'est son endroit légitime
const emit = defineEmits<{
    close: [];
    complete: [data: any];
}>();

const { 
    currentStep, 
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    handleFrameworkSelect
} = useGenerator();

function handleClose() {
    emit('close');
}

// ✅ On intercepte le clic sur "Next" pour émettre 'complete' si c'est la dernière étape
function handleNextStep() {
    const finalData = goToNextStep();
    if (finalData) {
        // Si goToNextStep retourne des données, c'est qu'on est à la dernière étape
        emit('complete', finalData);
    }
}
</script>