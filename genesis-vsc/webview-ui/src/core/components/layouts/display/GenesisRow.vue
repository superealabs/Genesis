<template>
    <div
        class="flex items-center gap-3 border border-secondary rounded-lg px-3 py-2 cursor-pointer transition-all duration-200"
        :class="{
            'bg-accent/10 border-accent': selected,
            'bg-bg-light hover:border-accent/50': !selected
        }"
        @click="$emit('click')"
    >
        <!-- Logo -->
        <div class="flex items-center justify-center w-8 h-8 rounded bg-secondary text-text-muted text-xs font-mono shrink-0">
            <slot name="logo">{{ initials }}</slot>
        </div>

        <!-- Infos -->
        <div class="flex flex-col gap-0.5 min-w-0">
            <span class="text-xs font-semibold text-text truncate">{{ label }}</span>
            <span v-if="sublabel" class="text-text-muted truncate" style="font-size: 10px;">{{ sublabel }}</span>
        </div>

        <!-- Slot droite (badge, action, etc.) -->
        <div class="ml-auto shrink-0">
            <slot name="right" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
    label: string;
    sublabel?: string;
    selected?: boolean;
}>();

defineEmits<{
    click: [];
}>();

const initials = computed(() => {
    return props.label
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase()
        .slice(0, 3);
});
</script>