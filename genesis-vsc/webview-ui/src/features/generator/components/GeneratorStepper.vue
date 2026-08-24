<template>
    <StepperPopup
        title="Créer un nouveau projet"
        :currentStep="currentStep"
        :totalSteps="totalSteps"
        size="full"
        @close="handleClose"
        @previous="goToPreviousStep"
        @next="goToNextStep"
    >
        <!-- Étape 1 : Choix du framework -->
        <FrameworksView
            v-if="currentStep === 1"
            :showBackButton="false"
            @select="handleFrameworkSelect"
        />

        <!-- Étape 2 : Configuration du projet et du framework -->
        <ProjectConfigView v-else-if="currentStep === 2" />

        <!-- Étape 3 : Validation (à venir) -->
        <div v-else-if="currentStep === 3" class="p-8 text-center text-text-muted">
            <p>Étape 3 : Récapitulatif et Validation (à venir)</p>
        </div>
    </StepperPopup>
</template>

<script setup lang="ts">
import StepperPopup from '@/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import ProjectConfigView from './ProjectConfigView.vue'; // <-- Nouveau composant
import { useGenerator } from '../composables/useGenerator';

const emit = defineEmits<{
    close: [];
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
</script>