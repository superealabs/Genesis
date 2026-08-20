<template>
    <div
        class="absolute z-200 bg-bg border border-secondary rounded-lg shadow-lg p-1 max-h-[40vh] overflow-y-auto"
        :class="[positionClasses, sizeClasses]"
    >
        <slot />
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    position?: 'top-left' | 'top-right' | 'bottom-left' | 'bottom-right';
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl';
}

const props = withDefaults(defineProps<Props>(), {
    size: 'md'
});

// ═══ Positionnement relatif au trigger ═══
const positionClasses = computed(() => ({
    'bottom-full mb-1 left-0':  props.position === 'top-left',
    'bottom-full mb-1 right-0': props.position === 'top-right',
    'top-full mt-1 left-0':     props.position === 'bottom-left',
    'top-full mt-1 right-0':    props.position === 'bottom-right',
}));

// ═══ Largeur minimale selon la taille ═══
const sizeClasses = computed(() => ({
    'min-w-[100px]': props.size === 'sm',
    'min-w-[150px]': props.size === 'md',
    'min-w-[200px]': props.size === 'lg',
    'min-w-[250px]': props.size === 'xl',
    'min-w-[300px]': props.size === '2xl',
    'min-w-[400px]': props.size === '3xl',
}));
</script>