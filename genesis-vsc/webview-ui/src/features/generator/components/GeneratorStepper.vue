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

        <!-- Étape 6 : Gestion des relations mère-fille -->        
        <RelationConfigView v-else-if="currentStep === 6" />

        <!-- Étape 7 : Sélection du Framework frontEnd -->
        <FrontEndSelectionView 
            v-else-if="currentStep === 7" 
            @select="handleFrontendSelect"
            :showBackButton="false" 
        />

        <!-- ✅ Étape 8 : Configuration du layout frontEnd -->   
        <FrontendLayoutConfigView v-else-if="currentStep === 8" />

        <GitConfigView v-else-if="currentStep === 9" />

        
    </StepperPopup>
</template>

<script setup lang="ts">
import StepperPopup from '@/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import ProjectConfigView from './steps/ProjectConfigView.vue';
import DatabaseConfigView from './steps/DatabaseConfigView.vue';
import ScriptConfigView from './steps/ScriptConfigView.vue';
import TableSelectionView from './steps/TableSelectionView.vue';
import RelationConfigView from './steps/RelationConfigView.vue';
import FrontEndSelectionView from '@/features/frontend/views/FrontEndSelectionView.vue';
import FrontendLayoutConfigView from './steps/FrontendLayoutConfigView.vue';
import GitConfigView from './steps/GitConfigView.vue';

import { useGenerator } from '../composables/useGenerator';
import type { FrontendFramework } from '../types/generator.types';

const emit = defineEmits<{
    close: [];
    complete: [data: any];
}>();

const { 
    currentStep, 
    totalSteps,
    goToPreviousStep,
    goToNextStep,
    handleFrameworkSelect,
    setSelectedFrontendFramework
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

function handleFrontendSelect(framework: FrontendFramework) {
    setSelectedFrontendFramework(framework);
}
</script>