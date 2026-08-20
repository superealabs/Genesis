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

        <!-- Étape 2 : Configuration du projet (à venir) -->
        <div v-else-if="currentStep === 2" class="p-8 text-center text-text-muted">
            <p>Étape 2 : Configuration du projet (à venir)</p>
        </div>

        <!-- Étape 3 : Validation (à venir) -->
        <div v-else-if="currentStep === 3" class="p-8 text-center text-text-muted">
            <p>Étape 3 : Validation (à venir)</p>
        </div>
    </StepperPopup>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import StepperPopup from '@/core/components/layouts/Popup/StepperPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import type { Framework } from '@/features/frameworks/types/framework.types';

const emit = defineEmits<{
    close: [];
    complete: [data: {
        framework: Framework;
        // Ajoute d'autres données d'étapes ici
    }];
}>();

// État du stepper
const currentStep = ref(1);
const totalSteps = 3;

// Données collectées à chaque étape
const stepperData = ref({
    framework: null as Framework | null,
    // Ajoute d'autres données ici
});

// ═══ Navigation ═══

function goToPreviousStep() {
    if (currentStep.value > 1) {
        currentStep.value--;
    }
}

function goToNextStep() {
    // Validation de l'étape actuelle
    if (!validateCurrentStep()) {
        return;
    }

    if (currentStep.value < totalSteps) {
        currentStep.value++;
    } else {
        // Dernière étape : compléter
        handleComplete();
    }
}

function validateCurrentStep(): boolean {
    switch (currentStep.value) {
        case 1:
            // L'étape 1 nécessite un framework sélectionné
            if (!stepperData.value.framework) {
                alert('Veuillez sélectionner un framework');
                return false;
            }
            return true;
        
        case 2:
            // Validation étape 2 (à définir)
            return true;
        
        case 3:
            // Validation étape 3 (à définir)
            return true;
        
        default:
            return true;
    }
}

// ═══ Handlers d'étapes ═══

function handleFrameworkSelect(framework: Framework) {
    stepperData.value.framework = framework;
}

function handleComplete() {
    emit('complete', {
        framework: stepperData.value.framework!,
        // Ajoute d'autres données collectées
    });
}

function handleClose() {
    emit('close');
}
</script>