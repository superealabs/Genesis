<template>
  <div class="relative w-full">
    <GenesisInput
      v-model="inputValue"
      v-bind="$attrs"
      :label="label"
      :input-id="inputId"
      :row-input="rowInput"
      :violation="violation"
      :mandatory="mandatory"
      type="tel"
      autocomplete="tel"
      inputmode="tel"
      class="genesis-phone"
    >
      <template #prefix>
        <button
          type="button"
          class="phone-prefix flex items-center gap-2 shrink-0 border-r border-base-300 bg-transparent transition-colors hover:bg-base-200 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="disabled"
          @click.prevent="toggleCountries"
        >
          <span class="phone-flag text-base">
            {{ activeCountry.flag }}
          </span>

          <span class="phone-code text-sm font-medium">
            {{ activeCountry.code }}
          </span>

          <span class="phone-chevron text-xs text-base-content/50"> ⌄ </span>
        </button>
      </template>
    </GenesisInput>

    <div
      v-if="showCountries"
      class="phone-countries absolute left-0 z-50 mt-1 w-64 max-h-60 overflow-y-auto rounded-lg border border-base-300 bg-base-100 shadow-lg"
    >
      <button
        v-for="countryItem in countries"
        :key="countryItem.iso"
        type="button"
        class="phone-country flex items-center gap-3 w-full px-3 py-2.5 text-left transition-colors hover:bg-base-200"
        @click.prevent="selectCountry(countryItem)"
      >
        <span>
          {{ countryItem.flag }}
        </span>

        <span class="phone-country-name flex-1 text-sm">
          {{ countryItem.name }}
        </span>

        <span class="phone-country-code text-sm text-base-content/50">
          {{ countryItem.code }}
        </span>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import GenesisInput from './GenesisInput.vue'

defineOptions({
  inheritAttrs: false,
})

interface Country {
  iso: string
  name: string
  code: string
  flag: string
}

const props = withDefaults(
  defineProps<{
    modelValue?: string | null
    label?: string
    inputId?: string
    rowInput?: boolean
    violation?: string
    mandatory?: boolean
    country?: string
    disabled?: boolean
  }>(),
  {
    modelValue: '',
    country: 'MG',
    disabled: false,
  },
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'update:country', value: string): void
}>()

const countries: Country[] = [
  { iso: 'MG', name: 'Madagascar', code: '+261', flag: '🇲🇬' },
  { iso: 'FR', name: 'France', code: '+33', flag: '🇫🇷' },
  { iso: 'US', name: 'United States', code: '+1', flag: '🇺🇸' },
  { iso: 'GB', name: 'United Kingdom', code: '+44', flag: '🇬🇧' },
]

const showCountries = ref(false)

// État interne pour conserver le pays sélectionné
const selectedCountryIso = ref<string>(props.country)

// Met à jour l'état interne si la prop country externe change
watch(
  () => props.country,
  (newIso) => {
    if (newIso) selectedCountryIso.value = newIso
  },
)

// Détecte automatiquement le pays si le modelValue contient un indicatif connu au chargement
watch(
  () => props.modelValue,
  (val) => {
    if (val && val.startsWith('+')) {
      const found = countries.find((c) => val.startsWith(c.code))
      if (found) {
        selectedCountryIso.value = found.iso
      }
    }
  },
  { immediate: true },
)

const activeCountry = computed(() => {
  return countries.find((c) => c.iso === selectedCountryIso.value) ?? countries[0]
})

// Affichage uniquement du numéro local dans le champ texte
const inputValue = computed({
  get: () => {
    if (!props.modelValue) return ''
    return props.modelValue.replace(activeCountry.value.code, '').replace(/[^\d]/g, '')
  },

  set: (value: string) => {
    const cleaned = value.replace(/[^\d]/g, '')
    emit('update:modelValue', cleaned ? `${activeCountry.value.code}${cleaned}` : '')
  },
})

const toggleCountries = () => {
  if (!props.disabled) {
    showCountries.value = !showCountries.value
  }
}

const selectCountry = (newCountry: Country) => {
  // 1. Mettre à jour l'état interne et émettre l'événement
  selectedCountryIso.value = newCountry.iso
  emit('update:country', newCountry.iso)

  // 2. Extraire le numéro local
  const currentLocalNumber = props.modelValue
    ? props.modelValue.replace(/^\+\d+/, '').replace(/[^\d]/g, '')
    : ''

  // 3. Émettre la valeur avec le nouvel indicatif
  emit('update:modelValue', currentLocalNumber ? `${newCountry.code}${currentLocalNumber}` : '')

  showCountries.value = false
}
</script>

<style scoped>
.phone-prefix {
  height: 2.25rem;
  padding-left: 0.75rem;
  padding-right: 0.75rem;
}

.phone-country {
  appearance: none;
  background: transparent;
  border: 0;
}

.phone-country:hover {
  background-color: var(--color-base-200);
}
</style>
