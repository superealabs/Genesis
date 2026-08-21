<template>
    <BasePopup
        :title="title"
        :isClosable="true"
        :draggable="true"
        :resizableX="true"
        :resizableY="true"
        size="lg"
        @close="$emit('close')"
    >
        <slot />
    </BasePopup>
</template>

<script setup lang="ts">
import BasePopup from '@/core/components/layouts/Popup/BasePopup.vue';

defineProps<{
    title?: string;
}>();

defineEmits<{
    close: [];
}>();
</script>