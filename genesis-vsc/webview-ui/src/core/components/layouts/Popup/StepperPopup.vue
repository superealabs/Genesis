<template>
    <BaseFormPopup
        :title="title"
        :size="size"
        :isClosable="isClosable"
        :draggable="draggable"
        @close="$emit('close')"
    >
        <!-- Contenu de l'étape -->
        <div class="flex-1">
            <slot />
        </div>

        <!-- Footer -->
        <div class="flex justify-between items-center pt-4 mt-4 border-t border-secondary">
            <GenesisButton
                variant="secondary"
                :disabled="currentStep === 1"
                @click="$emit('previous')"
            >
                Previous
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

            <GenesisButton @click="$emit('next')">
                {{ currentStep === totalSteps ? 'Finish' : 'Next' }}
            </GenesisButton>
        </div>

    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';

withDefaults(defineProps<{
    title?: string;
    currentStep: number;
    totalSteps: number;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
    isClosable?: boolean;
    draggable?: boolean;
}>(), {
    size: 'md',
    isClosable: true,
    draggable: true
});

defineEmits<{
    close: [];
    previous: [];
    next: [];
}>();
</script>