<template>
  <div class="inline-flex items-center gap-3">
    <Switch
      v-model="internalValue"
      :disabled="disabled"
      :class="[
        internalValue ? 'bg-accent' : 'bg-bg-dark border border-secondary',
        disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer',
        sizeClasses.container
      ]"
      class="relative inline-flex shrink-0 rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 focus-visible:ring-offset-bg"
    >
      <span class="sr-only">{{ label || 'Basculer l\'état' }}</span>
      <span
        aria-hidden="true"
        :class="[
          internalValue ? sizeClasses.translateOn : sizeClasses.translateOff,
          sizeClasses.knob
        ]"
        class="pointer-events-none inline-block transform rounded-full bg-white shadow-sm ring-0 transition duration-200 ease-in-out"
      />
    </Switch>

    <!-- Label et Description optionnels pour l'accessibilité et le contexte -->
    <div v-if="label || description" class="flex flex-col select-none">
      <span v-if="label" class="text-sm font-medium text-text" :class="{ 'cursor-pointer': !disabled }" @click="!disabled && (internalValue = !internalValue)">
        {{ label }}
      </span>
      <span v-if="description" class="text-xs text-muted">
        {{ description }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { Switch } from '@headlessui/vue';

interface Props {
  modelValue: boolean;
  disabled?: boolean;
  size?: 'sm' | 'md' | 'lg';
  label?: string;
  description?: string;
}

const props = withDefaults(defineProps<Props>(), {
  disabled: false,
  size: 'md',
  label: '',
  description: ''
});

const emit = defineEmits<{
  'update:modelValue': [value: boolean];
}>();

// Computed pour gérer le v-model proprement
const internalValue = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
});

// Conventions de tailles (Container, Knob, Translation)
const sizeClasses = computed(() => {
  switch (props.size) {
    case 'sm':
      return {
        container: 'w-9 h-5',       // 36px x 20px
        knob: 'w-4 h-4',            // 16px x 16px
        translateOn: 'translate-x-4', // 16px (pour coller au bord droit)
        translateOff: 'translate-x-0'
      };
    case 'md':
      return {
        container: 'w-11 h-6',      // 44px x 24px (Standard)
        knob: 'w-5 h-5',            // 20px x 20px
        translateOn: 'translate-x-5', // 20px
        translateOff: 'translate-x-0'
      };
    case 'lg':
      return {
        container: 'w-14 h-8',      // 56px x 32px
        knob: 'w-7 h-7',            // 28px x 28px
        translateOn: 'translate-x-6', // 24px (14*4 - 7*4 = 56 - 28 = 28px = 7 * 4, donc translate-x-6 laisse 4px de marge, ou translate-x-7 pour coller. Utilisons translate-x-6 pour un look aéré)
        translateOff: 'translate-x-0'
      };
    default:
      return {
        container: 'w-11 h-6',
        knob: 'w-5 h-5',
        translateOn: 'translate-x-5',
        translateOff: 'translate-x-0'
      };
  }
});
</script>