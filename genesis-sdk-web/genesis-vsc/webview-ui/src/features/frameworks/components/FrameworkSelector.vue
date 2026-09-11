<!-- webview-ui/src/features/frameworks/components/FrameworkSelector.vue -->
<template>
    <BaseFormPopup
        v-if="isOpen"
        title="Sélectionner un framework"
        :isClosable="true"
        :draggable="true"
        size="xl"
        @close="close"
    >
        <FrameworksView
            :showBackButton="false"
            :autoSelect="true"
            @select="handleSelect"
        />
    </BaseFormPopup>
</template>

<script setup lang="ts">
import BaseFormPopup from '@/core/components/layouts/Popup/BaseFormPopup.vue';
import FrameworksView from '@/features/frameworks/views/FrameworksView.vue';
import type { Framework } from '@/features/frameworks/types/framework.types';

const props = defineProps<{
    isOpen: boolean;
}>();

const emit = defineEmits<{
    'update:isOpen': [value: boolean];
    'select': [framework: Framework];
    'close': [];
}>();

function handleSelect(framework: Framework) {
    emit('select', framework);
    close();
}

function close() {
    emit('update:isOpen', false);
    emit('close');
}
</script>