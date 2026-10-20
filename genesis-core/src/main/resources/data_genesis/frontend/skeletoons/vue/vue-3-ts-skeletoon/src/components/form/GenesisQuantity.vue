<template>
  <GenesisInput
    v-model="inputValue"
    v-bind="$attrs"
    :label="label"
    :input-id="inputId"
    :row-input="rowInput"
    :violation="violation"
    :mandatory="mandatory"
    type="number"
    :min="min"
    :max="max"
    :step="step"
    class="genesis-quantity"
  >
    <template #prefix>
      <button
        type="button"
        class="
          flex h-9 w-9 shrink-0
          items-center justify-center
          select-none
          transition-all duration-150
          border-r border-base-300
          hover:bg-base-200
          active:scale-95
          disabled:cursor-not-allowed
          disabled:opacity-30
        "
        :disabled="disabled || isMin"
        @mousedown.prevent
        @click.prevent="decrement"
      >
        <span class="text-lg font-medium leading-none">−</span>
      </button>
    </template>

    <template #suffix>
      <div class="flex items-center">
        <span
          v-if="unit"
          class="
            border-l border-base-300
            px-3
            text-sm
            text-base-content/50
            whitespace-nowrap
          "
        >
          {{ unit }}
        </span>

        <button
          type="button"
          class="
            flex h-9 w-9 shrink-0
            items-center justify-center
            select-none
            transition-all duration-150
            border-l border-base-300
            hover:bg-base-200
            active:scale-95
            disabled:cursor-not-allowed
            disabled:opacity-30
          "
          :disabled="disabled || isMax"
          @mousedown.prevent
          @click.prevent="increment"
        >
          <span class="text-lg font-medium leading-none">+</span>
        </button>
      </div>
    </template>
  </GenesisInput>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import GenesisInput from './GenesisInput.vue'

defineOptions({
  inheritAttrs: false,
})

const props = withDefaults(
  defineProps<{
    modelValue?: number | string | null
    label?: string
    inputId?: string
    rowInput?: boolean
    violation?: string
    mandatory?: boolean

    min?: number
    max?: number
    step?: number
    unit?: string
    disabled?: boolean
  }>(),
  {
    modelValue: 0,
    min: 0,
    max: Infinity,
    step: 1,
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
}>()

/**
 * Valeur numérique courante.
 * - null / undefined / "" / NaN → 0 (par défaut)
 * - clamps pas appliqués ici : on veut pouvoir taper 5 même si min=100
 */
const numericValue = computed<number>(() => {
  const value = Number(props.modelValue)
  return Number.isFinite(value) ? value : 0
})

/** Bornes atteintes (pour désactiver les boutons). */
const isMin = computed(() => numericValue.value <= props.min)
const isMax = computed(() => numericValue.value >= props.max)

/**
 * v-model local : lit numericValue, écrit vers le parent via emit.
 * Le clamp min/max est appliqué à l'écriture, quelle que soit la source
 * (frappe clavier OU boutons +/−).
 */
const inputValue = computed<number>({
  get: () => numericValue.value,
  set: (value: number | string) => {
    let number = Number(value)

    if (!Number.isFinite(number)) {
      number = props.min
    }

    number = Math.max(props.min, Math.min(props.max, number))

    emit('update:modelValue', number)
  },
})

const decrement = (): void => {
  if (props.disabled || isMin.value) return
  const value = Math.max(props.min, numericValue.value - props.step)
  emit('update:modelValue', value)
}

const increment = (): void => {
  if (props.disabled || isMax.value) return
  const value = Math.min(props.max, numericValue.value + props.step)
  emit('update:modelValue', value)
}
</script>
