<template>
  <div class="flex items-center gap-2" :class="{ 'flex-col items-start': !rowInput }">
    <!-- Label -->
    <div class="flex gap-2 bg-transparent">
      <slot name="label">
        <label
          v-if="label"
          :for="inputId"
          class="label font-medium text-neutral"
          :class="{ 'whitespace-nowrap': rowInput }"
        >
          {{ label }}
        </label>
      </slot>
      <ErrorMessage v-if="violation" :message="violation" />
    </div>
    <!-- Select -->
    <select
      v-bind="$attrs"
      ref="selectRef"
      :id="inputId"
      class="select w-full overflow-y-auto"
      :value="modelValue ?? ''"
      :disabled="loading"
      @change="onChange"
    >
      <option value="" disabled selected>
        {{ placeholder ?? `-- Select an option --` }}
      </option>
      <option v-for="option in options" :key="option.value" :value="option.value">
        {{ option.label }}
      </option>
    </select>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import type { SelectOption } from '@/models/SelectOption'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

const props = defineProps<{
  label?: string
  placeholder?: string
  modelValue?: string | number | null
  options: SelectOption[]
  loading?: boolean
  rowInput?: boolean
  violation?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | number | null): void
}>()

const selectRef = ref<HTMLSelectElement | null>(null)

const inputId = computed(() => {
  return props.label
    ? 'select-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'select-' + Math.random().toString(36).substring(2, 8)
})

const onChange = (e: Event) => {
  const value = (e.target as HTMLSelectElement).value
  emit('update:modelValue', value === '' ? null : value)
}

defineOptions({
  name: 'GenesisSelect',
})
</script>
