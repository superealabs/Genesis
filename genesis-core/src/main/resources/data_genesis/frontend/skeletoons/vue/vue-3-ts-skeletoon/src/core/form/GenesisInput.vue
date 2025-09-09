<template>
  <div :class="{ 'flex-col items-start w-auto': !rowInput, 'flex items-center gap-2': true }">
    <!-- Label -->
    <label
      v-if="label"
      :for="inputFormId"
      class="label font-medium text-neutral"
      :class="{ 'whitespace-nowrap': rowInput }"
    >
      {{ label }}
    </label>

    <!-- Input -->
    <input
      v-bind="$attrs"
      :value="modelValue"
      :id="inputFormId"
      class="w-full focus:border-0 input"
      @input="onInput"
    />
  </div>
</template>

<script lang="ts">
import { computed, defineComponent } from 'vue'
import type { PropType } from 'vue'

export default defineComponent({
  name: 'GenesisInput',
  props: {
    label: { type: String, required: false },
    modelValue: {
      type: [String, Number, Date] as PropType<string | number | Date>,
      required: false,
    },
    inputId: {
      type: String,
      required: false,
    },
    rowInput: { type: Boolean, default: false },
  },
  emits: ['update:modelValue'],
  setup(props, { emit }) {
    const inputFormId = computed(() => {
      if (props.inputId) {
        return props.inputId
      }
      return props.label
        ? 'inpt-' + props.label.replace(/\s+/g, '-').toLowerCase()
        : 'inpt-' + Math.random().toString(36).substring(2, 8)
    })

    const onInput = (e: Event) => {
      emit('update:modelValue', (e.target as HTMLInputElement).value)
    }

    return { inputFormId, onInput }
  },
})
</script>
