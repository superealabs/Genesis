<!-- genesis-sdk-web/genesis-web-core/src/core/components/layouts/Popup/StepperPopup.vue -->
<template>
    <BaseFormPopup
        :title="title"
        :size="size"
        :isClosable="isClosable"
        :draggable="draggable"
        @close="$emit('close')"
        :position="position"
    >
        <!-- OVERLAY DE CHARGEMENT -->
        <div 
            v-if="isLoading" 
            class="absolute inset-0 z-50 flex items-center justify-center bg-bg-dark/90 backdrop-blur-sm rounded-lg"
        >
            <GenesisLoading 
                message="Génération du projet en cours..."
                sub-message="Veuillez ne pas fermer cette fenêtre."
            />
        </div>

        <!-- Conteneur principal -->
        <div class="relative flex flex-col flex-1 min-h-0 h-full">
            
            <!-- Zone de contenu scrollable -->
            <div class="flex-1 min-h-0 bg-bg-dark" :class="contentClass">
                <slot />
            </div>

            <!-- ✅ Footer conditionnel : masqué si hideFooter est true -->
            <div v-if="!hideFooter" class="flex-shrink-0 flex justify-between items-center pt-4 mt-4 border-t border-bg-neutral-genesis">
                <div class="flex items-center gap-2">
                    <GenesisButton
                        variant="secondary"
                        :disabled="currentStep === 1"
                        @click="$emit('previous')"
                    >
                        Précédent
                    </GenesisButton>

                    <GenesisButton
                        v-if="showBackToSummary"
                        variant="tertiary"
                        @click="$emit('back-to-summary')"
                    >
                        Retour au résumé
                    </GenesisButton>
                </div>

                <!-- Indicateur d'étapes -->
                <div class="flex items-center gap-2">
                    <div
                        v-for="(step, index) in totalSteps"
                        :key="step"
                        class="relative flex flex-col items-center group"
                    >
                        <span
                            class="h-2 rounded-full transition-all duration-200"
                            :class="{
                                'w-6 bg-primary cursor-default': step === currentStep,
                                'w-2 bg-primary/40 hover:scale-110 cursor-pointer': visitedSteps?.has(step) && step !== currentStep,
                                'w-2 bg-bg-neutral-genesis opacity-50 cursor-not-allowed': !visitedSteps?.has(step)
                            }"
                            @click="visitedSteps?.has(step) && step !== currentStep ? $emit('go-to-step', step) : null"
                        >
                            <span 
                                v-if="stepLabels && stepLabels[index]"
                                class="absolute bottom-full mb-2 hidden group-hover:block px-2 py-1 text-xs text-text bg-bg-dark border border-secondary rounded shadow-lg whitespace-nowrap z-50 pointer-events-none"
                            >
                                {{ stepLabels[index] }}
                            </span>
                        </span>
                    </div>
                </div>

                <div class="flex items-center gap-2">
                    <GenesisButton
                        v-if="isSkippable"
                        variant="tertiary"
                        @click="$emit('skip')"
                    >
                        Passer
                    </GenesisButton>
                    
                    <GenesisButton 
                        @click="$emit('next')" 
                        :variant="'accent'"
                        :disabled="disableNext"
                    >
                        {{ currentStep === totalSteps ? 'Générer' : 'Suivant' }}
                    </GenesisButton>
                </div>
            </div>
        </div>
    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisLoading from '@genesis-labs/web-core/core/components/ui/feedback/GenesisLoading.vue';
import type { PopupSize, PopupPosition } from './popup.types';

withDefaults(defineProps<{
    title?: string;
    currentStep: number;
    totalSteps: number;
    size?: PopupSize;
    position?: PopupPosition;
    isClosable?: boolean;
    draggable?: boolean;
    contentClass?: string;
    isSkippable?: boolean;
    showBackToSummary?: boolean;
    visitedSteps?: Set<number>;
    stepLabels?: string[];
    isLoading?: boolean;
    disableNext?: boolean;
    hideFooter?: boolean;
}>(), {
    size: 'md',
    isClosable: true,
    draggable: true,
    position: 'center',
    contentClass: 'overflow-hidden',
    isSkippable: false,
    showBackToSummary: false,
    isLoading: false,
    disableNext: false,
    hideFooter: false
});

defineEmits<{
    close: [];
    previous: [];
    next: [];
    skip: [];
    'back-to-summary': [];
    'go-to-step': [step: number];
}>();
</script>