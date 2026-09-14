<template>
    <button
        type="button"
        :disabled="disabled"
        class="inline-flex items-center gap-2 group"
        :class="[
            disabled ? 'cursor-not-allowed opacity-50' : 'cursor-pointer',
            sizeClasses
        ]"
        @click="toggleState"
        @keydown.space.prevent="toggleState"
        :aria-checked="ariaChecked"
        role="checkbox"
    >
        <!-- ═══ Wrapper qui réserve l'espace exact de l'icône ═══ -->
        <div 
            class="relative flex-shrink-0" 
            :style="{ width: `${iconPixelSize}px`, height: `${iconPixelSize}px` }"
        >
            <transition
                enter-active-class="transition-opacity duration-150 ease-out"
                enter-from-class="opacity-0"
                enter-to-class="opacity-100"
                leave-active-class="transition-opacity duration-100 ease-in"
                leave-from-class="opacity-100"
                leave-to-class="opacity-0"
            >
                <!-- État CHECKED (Inclus) -->
                <IconCheckboxChecked 
                    v-if="modelValue === 'checked'" 
                    key="checked" 
                    :size="iconPixelSize" 
                    class="absolute inset-0 text-accent" 
                />

                <!-- État REFUSED (Non inclus) -->
                <IconCheckboxMinus 
                    v-else-if="modelValue === 'refused'" 
                    key="refused" 
                    :size="iconPixelSize" 
                    class="absolute inset-0 text-red-500" 
                />

                <!-- État NEUTRE (Vide) -->
                <IconCheckbox 
                    v-else 
                    key="neutral" 
                    :size="iconPixelSize" 
                    class="absolute inset-0 text-text-muted" 
                />
            </transition>
        </div>

        <!-- Label optionnel -->
        <span
            v-if="label || $slots.default"
            class="text-text select-none"
            :class="labelSizeClasses"
        >
            <slot>{{ label }}</slot>
        </span>
    </button>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import IconCheckbox from '../icons/IconCheckbox.vue';
import IconCheckboxChecked from '../icons/IconCheckboxChecked.vue';
import IconCheckboxMinus from '../icons/IconCheckboxMinus.vue';

export type CheckboxState = 'neutral' | 'checked' | 'refused';
export type CheckboxSize = 'sm' | 'md' | 'lg';

interface Props {
    modelValue: CheckboxState;
    label?: string;
    disabled?: boolean;
    size?: CheckboxSize;
    cycle?: CheckboxState[];
}

const props = withDefaults(defineProps<Props>(), {
    modelValue: 'neutral',
    label: '',
    disabled: false,
    size: 'lg',
    cycle: () => ['neutral', 'checked', 'refused']
});

const emit = defineEmits<{
    'update:modelValue': [value: CheckboxState];
}>();

function toggleState() {
    if (props.disabled) return;
    const currentIndex = props.cycle.indexOf(props.modelValue);
    const nextIndex = (currentIndex + 1) % props.cycle.length;
    emit('update:modelValue', props.cycle[nextIndex]);
}

const ariaChecked = computed(() => {
    if (props.modelValue === 'checked') return 'true';
    if (props.modelValue === 'refused') return 'mixed';
    return 'false';
});

const sizeClasses = computed(() => ({
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base',
}[props.size]));

const labelSizeClasses = computed(() => ({
    sm: 'text-xs',
    md: 'text-sm',
    lg: 'text-base',
}[props.size]));

const iconPixelSize = computed(() => {
    switch (props.size) {
        case 'sm': return 16;
        case 'lg': return 24;
        default: return 20; // md
    }
});
</script>