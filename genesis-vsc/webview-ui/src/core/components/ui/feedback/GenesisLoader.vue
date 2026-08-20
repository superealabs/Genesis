<template>
    <div class="flex flex-col items-center justify-center gap-3 w-full"
        :class="fullPage ? 'min-h-screen' : 'h-full'"
    >
        <svg
            :width="svgSize"
            :height="svgSize"
            viewBox="0 0 24 24"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
            class="animate-spin"
        >
            <circle
                cx="12"
                cy="12"
                :r="radius"
                stroke="currentColor"
                :stroke-width="strokeWidth"
                class="text-secondary opacity-30"
            />
            <path
                :d="arcPath"
                stroke="currentColor"
                :stroke-width="strokeWidth"
                stroke-linecap="round"
                class="text-accent"
            />
        </svg>
        <div class="flex flex-col items-center gap-1" v-if="title || message">
            <span v-if="title" class="text-sm font-medium text-text">{{ title }}</span>
            <span v-if="message" class="text-xs text-text-muted text-center">{{ message }}</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

interface Props {
    size?: 'sm' | 'md' | 'lg';
    title?: string;
    message?: string;
    fullPage?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    size: 'md',
    fullPage: false
});

const config = {
    sm: { svgSize: 16, radius: 6, strokeWidth: 2 },
    md: { svgSize: 24, radius: 9, strokeWidth: 2.5 },
    lg: { svgSize: 40, radius: 15, strokeWidth: 3 },
};

const svgSize    = computed(() => config[props.size].svgSize);
const radius     = computed(() => config[props.size].radius);
const strokeWidth = computed(() => config[props.size].strokeWidth);

const arcPath = computed(() => {
    const r = radius.value;
    return `M 12 ${12 - r} A ${r} ${r} 0 0 1 ${12 + r} 12`;
});
</script>