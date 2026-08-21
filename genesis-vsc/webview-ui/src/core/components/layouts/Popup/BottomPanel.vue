<template>
    <BaseFormPopup
        :title="title"
        :isClosable="true"
        :draggable="true"
        :resizableX="true"
        :resizableY="true"
        size="lg"
        @close="$emit('close')"
    >
        <slot />
    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';

defineProps<{
    title?: string;
}>();

defineEmits<{
    close: [];
}>();
</script>