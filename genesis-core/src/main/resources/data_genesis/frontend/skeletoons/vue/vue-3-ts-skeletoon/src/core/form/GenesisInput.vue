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
    <input v-bind="$attrs" :id="inputFormId" class="w-full min-w-25 focus:border-0 input" @input="onInput" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  label?: string
  inputId?: string
  rowInput?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:model-value', value: string): void
}>()

const inputFormId = computed(() => {
  if (props.inputId) {
    return props.inputId
  }
  return props.label
    ? 'inpt-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'inpt-' + Math.random().toString(36).substring(2, 8)
})

const onInput = (e: Event) => {
  const newVal = (e.target as HTMLInputElement).value
  console.log('Input Val: ' + newVal)
  emit('update:model-value', newVal)
}
</script>
