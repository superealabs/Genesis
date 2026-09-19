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
        :aria-checked="modelValue"
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
                <!-- État CHECKED (true) -->
                <IconCheckboxChecked 
                    v-if="modelValue" 
                    key="checked" 
                    :size="iconPixelSize" 
                    class="absolute inset-0 text-accent" 
                />

                <!-- État NEUTRE (false) -->
                <IconCheckbox 
                    v-else 
                    key="neutral" 
                    :size="iconPixelSize" 
                    class="absolute inset-0 text-text-muted group-hover:text-text" 
                />
            </transition>
        </div>

        <!-- Label optionnel -->
        <span
            v-if="label || $slots.default"
            class="text-text select-none transition-colors group-hover:text-text"
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

export type CheckboxSize = 'sm' | 'md' | 'lg';

interface Props {
    modelValue: boolean; // ✅ Type booléen standard
    label?: string;
    disabled?: boolean;
    size?: CheckboxSize;
}

const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    label: '',
    disabled: false,
    size: 'lg',
});

const emit = defineEmits<{
    'update:modelValue': [value: boolean];
}>();

function toggleState() {
    if (props.disabled) return;
    emit('update:modelValue', !props.modelValue);
}

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