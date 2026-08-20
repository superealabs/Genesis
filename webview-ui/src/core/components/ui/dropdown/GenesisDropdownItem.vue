<template>
    <button
        class="w-full flex items-center gap-2 px-3 py-1.5 text-xs text-text
               hover:bg-secondary cursor-pointer transition-colors
               disabled:opacity-50 disabled:cursor-not-allowed"
        :class="{ 'bg-accent/10 text-accent': active }"
        :disabled="disabled"
        @click="handleClick"
    >
        <span v-if="$slots.icon" class="flex items-center shrink-0">
            <slot name="icon" />
        </span>
        <span v-if="$slots.default" class="flex-1 text-left">
            <slot />
        </span>
    </button>
</template>

<script setup lang="ts">
import { inject } from 'vue';

defineProps<{
    active?: boolean;
    disabled?: boolean;
}>();

const emit = defineEmits<{
    click: [];
}>();

const closeDropdown = inject<() => void>('closeDropdown');

function handleClick() {
    emit('click');
    closeDropdown?.();
}
</script>