<template>
  <div class="flex flex-wrap items-center gap-2">
    <!-- Filtres actifs -->
    <div class="flex flex-wrap items-center gap-2">
      <template v-if="activeFields.length">
        <div
          v-for="field in activeFields"
          :key="field.key"
          class="flex items-center shadow rounded-md bg-base pl-3"
        >
          <GenesisSelectSearch
            v-if="field.type === 'select' && field.selectSearch"
            :label="field.label"
            class="border-0"
            :rowInput="true"
            :search-function="field.selectSearch"
            @option-selected="
              (selectedValue) => {
                searchModel[field.key] = selectedValue
                updateFilters()
              }
            "
          />

          <GenesisInput
            v-else
            class="border-0"
            :placeholder="field.label"
            :label="field.label"
            :type="field.type"
            :rowInput="true"
            @update:model-value="
              (newVal) => {
                searchModel[field.key] = newVal
                updateFilters()
              }
            "
          />

          <GenesisButton
            @click="removeFilter(field.key)"
            class="btn btn-ghost btn-md text-error"
            title="Supprimer ce filtre"
          >
            <XIcon />
          </GenesisButton>
        </div>
      </template>

      <span v-else class="text-gray-400 italic">{{ $t('search.filters', 0) }}</span>

      <!-- Dropdown ajout de filtre -->
      <div class="dropdown">
        <GenesisButton
          class="btn btn-outline border-0 shadow"
          type="button"
          tabindex="0"
          title="Add filter criteria"
        >
          <PlusIcon />
        </GenesisButton>

        <ul
          tabindex="0"
          class="dropdown-content p-2 menu shadow bg-base-100 rounded-box w-72 h-72 overflow-y-scroll"
        >
          <template v-if="availableFields.length">
            <li v-for="field in availableFields" :key="field.key">
              <button
                class="btn btn-ghost w-full"
                :disabled="activeFieldKeys.includes(field.key)"
                @click="onFilterSelected(field.key)"
              >
                {{ field.label }}
              </button>
            </li>
          </template>
          <li v-else class="text-center text-gray-400 italic">No available filter</li>
        </ul>
      </div>

      <!-- Bouton Apply -->
      <GenesisButton
        v-if="!auto"
        title="Apply filter"
        @click="emitSearch"
        type="button"
        class="btn-secondary dark:btn-neutral border-0 shadow"
        ><FilterIcon class="mr-2" />
        <span>{{ $t('button.applySearch') }}</span>
      </GenesisButton>

      <!-- Reset -->
      <GenesisButton
        class="btn-md shadow border-0"
        title="Reset filter"
        type="button"
        :class="activeFields.length ? 'btn-outline text-error' : 'btn-disabled'"
        @click="clearAllFilters"
      >
        <ReloadIcon />
      </GenesisButton>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSearch } from '@/composables/useSearch'
import GenesisInput from '@/core/form/GenesisInput.vue'
import GenesisButton from '@/core/button/GenesisButton.vue'
import type { EntitySearchField } from '@/models/EntityModel'
import PlusIcon from '@/core/icons/PlusIcon.vue'
import FilterIcon from '@/core/icons/FilterIcon.vue'
import ReloadIcon from '@/core/icons/ReloadIcon.vue'
import XIcon from '@/core/icons/XIcon.vue'
import GenesisSelectSearch from '@/core/form/GenesisSelectSearch.vue'

// ✅ Props
const props = withDefaults(
  defineProps<{
    initialModel: Record<string, unknown>
    searchFields: EntitySearchField[]
    defaultActive?: string[]
    auto?: boolean
  }>(),
  {
    auto: false,
  },
)

// ✅ Emits
const emit = defineEmits<{
  (e: 'update:filter', filters: Record<string, unknown>): void
  (e: 'search', filters: Record<string, unknown>): void
}>()

// ✅ Use composable
const {
  searchModel,
  activeFieldKeys,
  selectedFieldToAdd,
  activeFields,
  availableFields,
  activateField,
  desactivateField,
  resetFilters,
  getFiltersValues,
} = useSearch(props.initialModel, props.searchFields)

// ✅ Initialize active fields
if (props.defaultActive?.length) {
  activeFieldKeys.value.push(...props.defaultActive)
}

// ✅ Handlers
const onFilterSelected = (key: string) => {
  selectedFieldToAdd.value = key
  activateField()
}

const updateFilters = () => {
  emit('update:filter', getFiltersValues())
  if (props.auto) {
    emitSearch()
  }
}

const emitSearch = () => {
  emit('search', getFiltersValues())
}

const removeFilter = (key: string) => {
  desactivateField(key)
  updateFilters()
  emitSearch()
}

const clearAllFilters = () => {
  resetFilters()
  updateFilters()
  emitSearch()
}
</script>
