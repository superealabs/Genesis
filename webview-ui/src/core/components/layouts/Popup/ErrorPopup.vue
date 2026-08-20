<template>
    <BasePopup :title="title" :size="size" @close="$emit('close')">
        <div class="flex flex-col justify-center items-center gap-4 py-4 h-full flex-1">
            <GenesisError
                :message="message"
                :stackTrace="stackTrace"
                :showStackTrace="showStackTrace"
            />
        </div>
    </BasePopup>
</template>

<script setup lang="ts">
import BasePopup from '@/core/components/layouts/Popup/BasePopup.vue';
import GenesisError from '@/core/components/ui/feedback/GenesisError.vue';

withDefaults(defineProps<{
    title?: string;
    message: string;
    stackTrace?: string;
    showStackTrace?: boolean;
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
}>(), {
    size: 'md'
});

defineEmits<{
    close: [];
}>();
</script>