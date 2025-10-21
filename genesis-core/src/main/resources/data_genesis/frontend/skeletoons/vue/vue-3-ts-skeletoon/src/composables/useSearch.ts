import { ref, computed } from 'vue'
import type { EntitySearchField } from '@/models/EntityModel'

export function useSearch(
  initialModel: Record<string, unknown>,
  availableFilters: EntitySearchField[],
) {
  const searchModel = ref({ ...initialModel })
  const activeFieldKeys = ref<string[]>([])
  const selectedFieldToAdd = ref<string>('')

  // Holds the actual field metadata (mutated with options if loaded)
  // const fields = ref<EntitySearchField[]>([...availableFilters])
  const fields = computed(() => {
    return availableFilters.filter(f => f.showInFilter)
  })

  const availableFields = computed(() =>
    fields.value.filter((f) => !activeFieldKeys.value.includes(f.key)),
  )

  const activeFields = computed(() =>
    fields.value.filter((f) => activeFieldKeys.value.includes(f.key)),
  )

  function activateField() {
    if (selectedFieldToAdd.value && !activeFieldKeys.value.includes(selectedFieldToAdd.value)) {
      activeFieldKeys.value.push(selectedFieldToAdd.value)
      selectedFieldToAdd.value = ''
    }
  }

  function desactivateField(key: string) {
    activeFieldKeys.value = activeFieldKeys.value.filter((k) => k !== key)
    searchModel.value[key] = undefined
  }

  function resetFilters() {
    for (const key in searchModel.value) {
      searchModel.value[key] = undefined
    }
    activeFieldKeys.value = []
  }

  function getFiltersValues() {
    const filtered: Record<string, unknown> = {}
    for (const key of activeFieldKeys.value) {
      const value = searchModel.value[key]
      if (value === undefined || value === null || value === '') continue

      const fieldMeta = fields.value.find((f) => f.key === key)

      if (fieldMeta?.type === 'select' && fieldMeta.searchKey) {
        // Wrap select values into nested object for API
        filtered[key] = { [fieldMeta.searchKey]: value }
      } else {
        filtered[key] = value
      }
    }
    return filtered
  }

  return {
    searchModel,
    activeFieldKeys,
    selectedFieldToAdd,
    activeFields,
    availableFields,
    activateField,
    desactivateField,
    resetFilters,
    getFiltersValues,
    fields,
  }
}
