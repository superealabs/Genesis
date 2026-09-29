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
          :class="inputClasses"
          :value="modelValue ?? ''"
          @input="onInput"
        />

        <slot name="suffix"></slot>
      </div>

      <ErrorMessage v-if="violation" :message="violation" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useAttrs } from 'vue'
import ErrorMessage from '@/components/common/ErrorMessage.vue'

defineOptions({
  inheritAttrs: false,
})

const props = defineProps<{
  modelValue?: string | number | File | null
  label?: string
  inputId?: string
  rowInput?: boolean
  violation?: string
  mandatory?: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | File | null): void
  (e: 'update:model-value', value: string | File | null): void
}>()

const attrs = useAttrs()

const inputType = computed(() => (typeof attrs.type === 'string' ? attrs.type : 'text'))

const inputFormId = computed(() => {
  if (props.inputId) return props.inputId

  if (props.label) {
    return 'inpt-' + props.label.replace(/\s+/g, '-').toLowerCase()
  }

  return 'inpt-' + Math.random().toString(36).substring(2, 8)
})

const inputClasses = computed(() => {
  const classes = ['w-full', 'min-w-25', 'bg-transparent']

  if (inputType.value === 'range') {
    classes.push('cursor-pointer', 'border', 'border-base-100', 'rounded-field')
  } else if (inputType.value === 'color') {
    // Styles spécifiques pour l'input color pour éviter le rendu DaisyUI déformé
    classes.push(
      'h-10',
      'w-16',
      'p-1',
      'cursor-pointer',
      'rounded-lg',
      'border',
      'border-base-300',
      'bg-base-100',
      'square',
    )
  } else {
    classes.push('input')
  }

  if (props.violation && inputType.value !== 'range') {
    classes.push('border-error')
  }

  return classes
})

const onInput = (e: Event) => {
  const input = e.target as HTMLInputElement

  if (input.type === 'file') {
    const val = input.files && input.files.length > 0 ? input.files[0] : null
    emit('update:modelValue', val)
    emit('update:model-value', val)
    return
  }

  emit('update:modelValue', input.value)
  emit('update:model-value', input.value)
}
</script>

<style scoped>
.square {
  max-width: 20px;
}
</style>
