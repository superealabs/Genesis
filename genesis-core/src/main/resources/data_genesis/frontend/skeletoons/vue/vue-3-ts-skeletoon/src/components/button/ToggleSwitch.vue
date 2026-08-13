<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    label?: string
    color?: 'primary' | 'secondary' | 'accent' | 'success' | 'warning' | 'error' | 'info'
    size?: 'xs' | 'sm' | 'md' | 'lg'
    disabled?: boolean
  }>(),
  {
    color: 'primary',
    size: 'md',
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// Construction dynamique des classes DaisyUI
const toggleClass = computed(() => {
  return `toggle toggle-${props.color} toggle-${props.size}`
})

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.checked)
}
</script>

<template>
  <label class="inline-flex items-center gap-3 cursor-pointer select-none">
    <input
      type="checkbox"
      :class="toggleClass"
      :checked="modelValue"
      :disabled="disabled"
      @change="handleChange"
    />
    <span v-if="label" class="label-text font-medium text-base-content">
      {{ label }}
    </span>
  </label>
</template>
