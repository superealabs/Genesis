<template>
  <div :class="{ 'flex-col items-start w-auto': !rowInput, 'flex items-center gap-2': true }">
    <div class="flex gap-2 bg-transparent">
      <!-- Label -->
      <label
        v-if="label"
        :for="inputFormId"
        class="label font-medium"
        :class="{ 'whitespace-nowrap': rowInput }"
      >
        {{ label }}
      </label>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
    <!-- Input -->
    <input
      v-bind="$attrs"
      :id="inputFormId"
      class="w-full min-w-25 focus:border-0 input bg-transparent"
      :class="{ 'border-error': violation }"
      @input="onInput"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

const props = defineProps<{
  label?: string
  inputId?: string
  rowInput?: boolean
  violation?: string
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
  emit('update:model-value', newVal)
}
</script>
