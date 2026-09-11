<template>
    <div class="flex flex-col justify-center gap-1 w-full h-full">

        <!-- Label header -->
        <div v-if="label || showValue" class="flex justify-between items-center">
            <span v-if="label" class="text-xs text-text-muted">{{ label }}</span>
            <span class="text-xs text-text-muted ml-auto">
                {{ displayValue }}
            </span>
        </div>

        <!-- Barre -->
        <div
            class="w-full rounded-full overflow-hidden"
            :class="trackSizeClass"
            style="background-color: var(--color-secondary);"
        >
            <div
                class="rounded-full transition-all duration-300"
                :class="[fillSizeClass, variantClass]"
                :style="{ width: `${clampedPercent}%` }"
            />
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    value: number;
    max?: number;
    mode?: 'percent' | 'value' | 'both';
    size?: 'sm' | 'md' | 'lg';
    variant?: 'default' | 'success' | 'warning' | 'danger';
    label?: string;
    showValue?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    max: 100,
    mode: 'percent',
    size: 'md',
    variant: 'default',
    showValue: true
});

const clampedPercent = computed(() => {
    const percent = (props.value / props.max) * 100;
    return Math.min(100, Math.max(0, percent));
});

const displayValue = computed(() => {
    if (props.mode === 'percent') return `${Math.round(clampedPercent.value)}%`;
    if (props.mode === 'value')   return `${props.value} / ${props.max}`;
    return `${props.value} / ${props.max} (${Math.round(clampedPercent.value)}%)`;
});

const trackSizeClass = computed(() => ({
    'h-1':   props.size === 'sm',
    'h-2':   props.size === 'md',
    'h-3':   props.size === 'lg',
}));

const fillSizeClass = computed(() => ({
    'h-1':   props.size === 'sm',
    'h-2':   props.size === 'md',
    'h-3':   props.size === 'lg',
}));

const variantClass = computed(() => ({
    'bg-accent':       props.variant === 'default',
    'bg-green-500':    props.variant === 'success',
    'bg-yellow-400':   props.variant === 'warning',
    'bg-red-500':      props.variant === 'danger',
}));
</script>