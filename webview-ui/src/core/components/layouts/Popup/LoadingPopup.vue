<template>
    <BasePopup :title="title" :size="size" :isClosable="isClosable" @close="$emit('close')">
        <div class="flex flex-col justify-center items-center gap-4 py-4 h-full flex-1">
            <GenesisLoader size="lg" />
            <span v-if="message" class="text-xs text-text-muted text-center">
                {{ message }}
            </span>
        </div>
    </BasePopup>
</template>

<script setup lang="ts">
import BasePopup from '@/core/components/layouts/Popup/BasePopup.vue';
import GenesisLoader from '@/core/components/ui/feedback/GenesisLoader.vue';

withDefaults(defineProps<{
    title?: string;
    message?: string;
    isClosable?: boolean;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
}>(), {
    isClosable: false,
    size: 'md'
});

defineEmits<{
    close: [];
}>();
</script>