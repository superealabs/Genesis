<template>
    <div
        class="inline-flex border border-secondary rounded-lg p-1 gap-1 bg-bg-light"
        role="group"
    >
        <button
            v-for="option in options"
            :key="String(option.value)"
            type="button"
            :class="buttonClasses(option)"
            :disabled="disabled || option.disabled"
            @click="updateValue(option.value)"
        >
            <component 
                v-if="option.icon" 
                :is="option.icon" 
                :class="iconSizeClasses"
            />
            <span v-if="option.label">{{ option.label }}</span>
        </button>
    </div>
</template>

<script setup lang="ts">
import { computed, type Component } from 'vue';

interface SegmentedOption {
    label?: string;
    value: string | number;
    icon?: Component;
    disabled?: boolean;
}

interface Props {
    modelValue: string | number;
    options: SegmentedOption[];
    size?: 'sm' | 'md' | 'lg';
    disabled?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    size: 'md',
    disabled: false
});

const emit = defineEmits<{
    'update:modelValue': [value: string | number];
}>();

function updateValue(value: string | number) {
    if (props.disabled) return;
    emit('update:modelValue', value);
}

function isActive(value: string | number): boolean {
    return props.modelValue === value;
}

// ═══ Classes des boutons ═══
const buttonClasses = (option: SegmentedOption) => {
    const active = isActive(option.value);
    
    const base = [
        'inline-flex items-center justify-center gap-1.5',
        'font-medium transition-all duration-200 rounded-md',
        'focus:outline-none focus:ring-2 focus:ring-accent/50',
        'disabled:opacity-50 disabled:cursor-not-allowed'
    ];

    const sizeMap = {
        sm: 'px-2.5 py-1 text-xs',
        md: 'px-3 py-1.5 text-sm',
        lg: 'px-4 py-2 text-base'
    };

    const stateClasses = active
        ? 'bg-accent text-bg shadow-sm'
        : 'text-text-muted hover:text-text hover:bg-white/5 dark:hover:bg-white/5';

    return [
        ...base,
        sizeMap[props.size],
        stateClasses
    ];
};

const iconSizeClasses = computed(() => ({
    'w-3.5 h-3.5': props.size === 'sm',
    'w-4 h-4': props.size === 'md',
    'w-5 h-5': props.size === 'lg'
}));
</script>