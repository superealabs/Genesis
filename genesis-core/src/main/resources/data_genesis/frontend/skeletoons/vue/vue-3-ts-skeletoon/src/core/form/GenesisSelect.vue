<template>
  <div class="flex items-center gap-2" :class="{ 'flex-col items-start': !rowInput }">
    <!-- Label -->
    <label
      v-if="label"
      :for="inputId"
      class="label font-medium text-neutral"
      :class="{ 'whitespace-nowrap': rowInput }"
    >
      {{ label }}
    </label>

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

<script lang="ts">
import { defineComponent, ref, computed } from 'vue'
import type { SelectOption } from '../../models/SelectOption'
import type { PropType } from 'vue'

export default defineComponent({
  name: 'GenesisSelect',
  props: {
    label: { type: String, required: false },
    placeholder: { type: String, required: false },
    modelValue: {
      type: [String, Number, null] as PropType<string | number | null>,
      required: false,
      default: null,
    },
    options: {
      type: Array as PropType<SelectOption[]>,
      required: true,
    },
    loading: { type: Boolean, default: false },
    rowInput: { type: Boolean, default: false },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
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

    return { inputId, selectRef, onChange }
  },
})
</script>
