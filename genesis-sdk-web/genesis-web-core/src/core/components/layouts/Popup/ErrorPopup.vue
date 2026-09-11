<template>
    <BaseFormPopup :title="title" :size="size" :position="position" @close="$emit('close')">
        <div class="flex flex-col justify-center items-center gap-4 py-4 h-full flex-1">
            <GenesisError
                :message="message"
                :stackTrace="stackTrace"
                :showStackTrace="showStackTrace"
            />
        </div>
    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisError from '@/core/components/ui/feedback/GenesisError.vue';
import type { PopupPosition, PopupSize } from './popup.types';

withDefaults(defineProps<{
    title?: string;
    message: string;
    stackTrace?: string;
    showStackTrace?: boolean;
    size?: PopupSize;
    position?: PopupPosition;
}>(), {
    size: 'md',
    position: 'center'
});

defineEmits<{
    close: [];
}>();
</script>