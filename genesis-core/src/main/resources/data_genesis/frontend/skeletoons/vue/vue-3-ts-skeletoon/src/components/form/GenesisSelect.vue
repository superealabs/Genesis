<template>
  <div
    :class="[
      'flex',
      { 'flex-col items-start w-auto': !rowInput },
      { 'items-center gap-2': rowInput },
    ]"
  >
    <div class="flex gap-2 bg-transparent">
      <label
        v-if="label || $slots.label"
        :for="inputId"
        class="label font-medium text-neutral"
        :class="{ 'whitespace-nowrap': rowInput }"
      >
        <slot name="label">{{ label }}</slot>
      </label>
    </div>

    <div class="flex-grow w-full">
      <div class="flex items-center">
        <slot name="prefix"></slot>

        <select
          v-bind="$attrs"
          ref="selectRef"
          :id="inputId"
          class="select w-full overflow-y-auto"
          :class="{
            'border-error': violation,
            'opacity-50': loading, // Dim the select when loading
          }"
          :value="modelValue ?? ''"
          :disabled="loading || $attrs.disabled"
          @change="onChange"
        >
          <option
            value=""
            disabled
            :selected="modelValue === null || modelValue === undefined || modelValue === ''"
          >
            {{ placeholder ?? `-- Select an option --` }}
          </option>

          <option v-if="loading" value="" disabled>Loading options...</option>

          <option v-else v-for="option in options" :key="option.value" :value="option.value">
            {{ option.label }}
          </option>
        </select>

        <slot name="suffix"></slot>
      </div>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
// Assuming SelectOption is defined as:
// interface SelectOption { value: string | number; label: string; }
import type { SelectOption } from '@/models/SelectOption'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

defineOptions({
  name: 'GenesisSelect',
  // Ensures attributes like 'required' go directly to the <select>
  inheritAttrs: false,
})

const props = defineProps<{
  label?: string
  placeholder?: string
  modelValue?: string | number | null
  options: SelectOption[]
  loading?: boolean // Added to handle a loading state
  rowInput?: boolean
  violation?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | null): void
}>()

const selectRef = ref<HTMLSelectElement | null>(null)

const inputId = computed(() => {
  // Use a predictable ID based on label or a fallback
  return props.label
    ? 'select-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-' + Math.random().toString(36).substring(2, 8)
})

const onChange = (e: Event) => {
  const value = (e.target as HTMLSelectElement).value
  // Emit null if the value is the empty string (which is the value of the disabled placeholder option)
  emit('update:modelValue', value === '' ? null : value)
}
</script>
