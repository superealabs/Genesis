<template>
  <GenesisInput
    :model-value="displayValue"
    v-bind="$attrs"
    :label="label"
    :input-id="inputId"
    :row-input="rowInput"
    :violation="violation"
    :mandatory="mandatory"
    type="text"
    inputmode="decimal"
    autocomplete="off"
    class="genesis-currency"
    @input="handleInput"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <template #suffix>
      <div class="currency-code">{{ currency }}</div>
    </template>
  </GenesisInput>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import GenesisInput from './GenesisInput.vue'

defineOptions({ inheritAttrs: false })

const props = withDefaults(
  defineProps<{
    modelValue?: number | string | null
    label?: string
    inputId?: string
    rowInput?: boolean
    violation?: string
    mandatory?: boolean
    currency?: string
    locale?: string
    min?: number
    max?: number
    step?: number
    decimals?: number
  }>(),
  {
    modelValue: null,
    currency: 'MGA',
    locale: 'fr-MG',
    min: 0,
    max: Infinity,
    step: 1,
    decimals: 2,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: number | null): void
}>()

/** L'utilisateur est actuellement en train de saisir. */
const isFocused = ref(false)

/**
 * Valeur brute interne (avec "." comme séparateur décimal).
 * Jamais envoyée telle quelle au parent.
 */
const editingValue = ref('')

/**
 * Saisie valide :
 *   - chiffres
 *   - un seul séparateur décimal optionnel ("," ou ".")
 * Refuse : "abc", "1.2.3", "-5"
 */
const NUMERIC_INPUT_REGEX = /^\d*(?:[.,]\d*)?$/

/** Caractère "significatif" (chiffre ou séparateur décimal). */
const SIGNIFICANT_CHAR_REGEX = /[\d.,]/

/** Découpe une valeur normalisée (".") en parties entière / décimale. */
const splitDecimal = (value: string) => {
  const dotIndex = value.indexOf('.')
  if (dotIndex === -1) {
    return { integer: value, decimal: '', hasDecimal: false }
  }
  return {
    integer: value.slice(0, dotIndex),
    decimal: value.slice(dotIndex + 1),
    hasDecimal: true,
  }
}

/**
 * Ajoute les séparateurs de milliers sur une chaîne de chiffres.
 * Basé sur une chaîne : aucune perte de précision.
 *
 *   "1000"     -> "1 000"
 *   "1000000"  -> "1 000 000"
 *   "0123"     -> "123"
 */
const groupDigits = (digits: string): string => {
  if (digits === '') return ''
  const trimmed = digits.replace(/^0+(?=\d)/, '')
  return trimmed.replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
}

/**
 * Formatage pendant la saisie (décimales non forcées).
 *
 *   "1234.5"  -> "1 234,5"
 *   "1234."   -> "1 234,"
 */
const formatEditingValue = (value: string): string => {
  if (value === '') return ''
  const normalized = value.replace(',', '.')
  const { integer, decimal, hasDecimal } = splitDecimal(normalized)
  const formattedInteger = groupDigits(integer)
  return hasDecimal ? `${formattedInteger},${decimal}` : formattedInteger
}

/**
 * Formatage final hors focus (décimales forcées).
 *
 *   1000  ->  "1 000,00"  (fr-MG)
 */
const formatCurrency = (value: number): string =>
  new Intl.NumberFormat(props.locale, {
    minimumFractionDigits: props.decimals,
    maximumFractionDigits: props.decimals,
    useGrouping: true,
  }).format(value)

/**
 * Valeur affichée dans GenesisInput.
 *
 * Pendant la saisie  : reflet de editingValue.
 * Hors focus          : formatage "monétaire" complet.
 */
const displayValue = computed(() => {
  if (isFocused.value) {
    return formatEditingValue(editingValue.value)
  }

  const raw = props.modelValue
  if (raw === null || raw === undefined || raw === '') return ''

  const number = Number(raw)
  return Number.isFinite(number) ? formatCurrency(number) : ''
})

/**
 * Convertit une saisie brute en Number.
 * Retourne null tant que la saisie est incomplète ("", ".", ",").
 */
const parseNumber = (value: string): number | null => {
  if (value === '' || value === '.' || value === ',') return null
  const number = Number(value.replace(',', '.'))
  return Number.isFinite(number) ? number : null
}

/** Applique min / max et arrondit au nombre de décimales. */
const clampAndRound = (value: number): number => {
  const bounded = Math.max(props.min, Math.min(props.max, value))
  return Number(bounded.toFixed(props.decimals))
}

/**
 * Restaure la position du curseur après reformatage.
 *
 * On compte les caractères significatifs (chiffres + séparateur décimal)
 * avant le curseur dans la saisie brute, puis on replace le curseur
 * après le même nombre de caractères significatifs dans la valeur formatée.
 */
const restoreCursor = (
  input: HTMLInputElement,
  formatted: string,
  significantCount: number,
): void => {
  let position = 0
  let count = 0
  while (position < formatted.length && count < significantCount) {
    if (SIGNIFICANT_CHAR_REGEX.test(formatted[position]!)) count++
    position++
  }
  input.setSelectionRange(position, position)
}

/**
 * Applique le formatage + restaure la position du curseur.
 * Factorisé pour éviter la duplication dans handleInput.
 */
const applyFormat = (
  input: HTMLInputElement,
  rawValue: string,
  significantBeforeCursor: number,
): string => {
  const formatted = formatEditingValue(rawValue)
  input.value = formatted
  restoreCursor(input, formatted, significantBeforeCursor)
  return formatted
}

/** INPUT */
const handleInput = (event: Event): void => {
  const input = event.target as HTMLInputElement
  const selectionStart = input.selectionStart ?? 0

  // Nombre de caractères significatifs avant le curseur
  let significantBeforeCursor = 0
  for (const ch of input.value.slice(0, selectionStart)) {
    if (SIGNIFICANT_CHAR_REGEX.test(ch)) significantBeforeCursor++
  }

  // Saisie brute (sans séparateurs de milliers)
  const rawValue = input.value.replace(/\s/g, '')

  // Refus des caractères invalides
  if (!NUMERIC_INPUT_REGEX.test(rawValue)) {
    applyFormat(input, editingValue.value, significantBeforeCursor)
    return
  }

  // Refus si trop de décimales
  const normalized = rawValue.replace(',', '.')
  const { decimal, hasDecimal } = splitDecimal(normalized)
  if (hasDecimal && decimal.length > props.decimals) {
    applyFormat(input, editingValue.value, significantBeforeCursor)
    return
  }

  // Saisie valide : on met à jour l'état + on notifie le parent
  editingValue.value = rawValue
  emit('update:modelValue', parseNumber(rawValue))

  // Application immédiate (pour l'UX : pas de flash "brut")
  const formatted = applyFormat(input, rawValue, significantBeforeCursor)

  // Ré-application après le cycle de re-render de Vue.
  // Corrige l'écrasement de la valeur formatée par la prop
  // `:model-value="displayValue"` quand Vue re-render.
  nextTick(() => {
    if (input.value !== formatted) {
      input.value = formatted
      restoreCursor(input, formatted, significantBeforeCursor)
    }
  })
}

/** FOCUS */
const handleFocus = (event: FocusEvent): void => {
  isFocused.value = true

  const raw = props.modelValue
  if (raw === null || raw === undefined || raw === '') {
    editingValue.value = ''
  } else {
    const number = Number(raw)
    editingValue.value = Number.isFinite(number) ? String(number) : ''
  }

  const input = event.target as HTMLInputElement
  input.value = formatEditingValue(editingValue.value)

  // Curseur placé à la fin après le reformatage
  requestAnimationFrame(() => {
    input.setSelectionRange(input.value.length, input.value.length)
  })
}

/** BLUR */
const handleBlur = (): void => {
  isFocused.value = false

  const raw = props.modelValue
  if (raw === null || raw === undefined || raw === '') {
    editingValue.value = ''
    return
  }

  const number = Number(raw)
  if (!Number.isFinite(number)) {
    editingValue.value = ''
    emit('update:modelValue', null)
    return
  }

  // Normalisation finale : clamp + arrondi
  const normalized = clampAndRound(number)
  emit('update:modelValue', normalized)
  editingValue.value = String(normalized)
}
</script>

<style scoped>
.currency-code {
  display: flex;
  align-items: center;
  height: 2.25rem;
  padding-left: 0.75rem;
  padding-right: 0.75rem;
  border-left: 1px solid var(--color-base-300, #e5e7eb);
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  color: rgba(0, 0, 0, 0.5);
}
</style>
