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
        :for="inputFormId"
        class="label font-medium"
        :class="{ 'whitespace-nowrap': rowInput }"
      >
        <slot name="label">{{ label }}</slot>
        <span v-if="mandatory" class="text-error">*</span>
      </label>
    </div>

    <div class="flex-grow w-full">
      <div class="flex items-center relative overflow-hidden">
        <slot name="prefix"></slot>

        <input
          v-bind="$attrs"
          :id="inputFormId"
          class="w-full min-w-25 input bg-transparent"
          :class="{
            'border-error': violation,
            'focus:border-0': false,
          }"
          @input="onInput"
        />

        <slot name="suffix"></slot>
      </div>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<{
  label?: string
  inputId?: string
  rowInput?: boolean
  violation?: string
  mandatory?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:model-value', value: string): void
}>()

const inputFormId = computed(() => {
  if (props.inputId) {
    return props.inputId
  }
  // Generate ID based on label or a random string if label is missing
  return props.label
    ? 'inpt-' + props.label.replace(/\s+/g, '-').toLowerCase()
    : 'inpt-' + Math.random().toString(36).substring(2, 8)
})

const onInput = (e: Event) => {
  const newVal = (e.target as HTMLInputElement).value
  emit('update:model-value', newVal)
}
</script>
