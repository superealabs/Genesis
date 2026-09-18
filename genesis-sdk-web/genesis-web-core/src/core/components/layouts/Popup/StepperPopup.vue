<template>
    <BaseFormPopup
        :title="title"
        :size="size"
        :isClosable="isClosable"
        :draggable="draggable"
        @close="$emit('close')"
        :position="position"
    >
        <!--  CORRECTION : Conteneur flex qui prend toute la hauteur disponible sans cacher le débordement -->
        <div class="flex flex-col flex-1 min-h-0 h-full">
            
            <!-- Zone de contenu scrollable -->
            <div class="flex-1 min-h-0" :class="contentClass">
                <slot />
            </div>

            <!--  CORRECTION : Footer fixé en bas, il ne doit JAMAIS rétrécir (flex-shrink-0) -->
            <!-- Footer fixé en bas -->
            <div class="flex-shrink-0 flex justify-between items-center pt-4 mt-4 border-t border-secondary">
                <GenesisButton
                    variant="secondary"
                    :disabled="currentStep === 1"
                    @click="$emit('previous')"
                >
                    Précédent
                </GenesisButton>

                <!-- Indicateur d'étapes -->
                <div class="flex items-center gap-2">
                    <span
                        v-for="step in totalSteps"
                        :key="step"
                        class="h-2 rounded-full transition-all duration-200"
                        :class="{
                            'w-6 bg-accent': step === currentStep,
                            'w-2 bg-accent/40': step < currentStep,
                            'w-2 bg-secondary': step > currentStep
                        }"
                    />
                </div>

                <!-- ✅ Groupe d'actions à droite -->
                <div class="flex items-center gap-2">
                    <GenesisButton
                        v-if="isSkippable"
                        variant="tertiary"
                        @click="$emit('skip')"
                    >
                        Passer
                    </GenesisButton>
                    
                    <GenesisButton @click="$emit('next')">
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
}>(), {
    size: 'md',
    isClosable: true,
    draggable: true,
    position: 'center',
    contentClass: 'overflow-y-auto',
    isSkippable: false
});

defineEmits<{
    close: [];
    previous: [];
    next: [];
    skip: [];
}>();
</script>