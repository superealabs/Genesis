<template>
    <BaseFormPopup :title="title" :size="size" :isClosable="isClosable" @close="$emit('close')">
        <div class="flex flex-col justify-center items-center gap-4 py-2 px-8 flex-1">
            <GenesisProgressBar
                :value="value"
                :max="max"
                :mode="mode"
                :variant="variant"
                :label="progressLabel"
                :showValue="true"
            />
            <span v-if="message" class="text-xs text-text-muted text-center">
                {{ message }}
            </span>
        </div>
    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisProgressBar from '@/core/components/ui/feedback/GenesisProgressBar.vue';

withDefaults(defineProps<{
    title?: string;
    message?: string;
    value: number;
    max?: number;
    mode?: 'percent' | 'value' | 'both';
    variant?: 'default' | 'success' | 'warning' | 'danger';
    progressLabel?: string;
    isClosable?: boolean;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
}>(), {
    max: 100,
    mode: 'percent',
    variant: 'default',
    isClosable: false,
    size: 'md'
});

defineEmits<{
    close: [];
}>();
</script>