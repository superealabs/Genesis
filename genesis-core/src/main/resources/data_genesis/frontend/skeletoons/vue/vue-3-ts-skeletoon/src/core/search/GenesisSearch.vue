<template>
  <div class="flex flex-wrap items-center gap-2 py-2">
    <!-- Filtres actifs -->
    <div class="flex flex-wrap items-center gap-2">
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

        <ul tabindex="0" class="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-52">
          <template v-if="availableFields.length">
            <li v-for="field in availableFields" :key="field.key">
              <button
                class="btn btn-ghost justify-start"
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
        title="Apply filter"
        @click="emitSearch"
        class="btn-secondary dark:btn-neutral border-0 shadow"
      ><FilterIcon class="mr-2" />
        <span>{{ $t('button.applySearch') }}</span>
      </GenesisButton>

      <!-- Reset -->
      <GenesisButton
        class="btn-md shadow border-0"
        title="Reset filter"
        :class="activeFields.length ? 'btn-outline text-error' : 'btn-disabled'"
        @click="clearAllFilters"
      >
        <ReloadIcon />
      </GenesisButton>

      <span class="font-semibold mr-2">{{ $t('search.filters', 1) }} :</span>

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
            @option-selected="(selectedValue) => (searchModel[field.key] = selectedValue)"
          />

          <GenesisInput
            v-else
            class="border-0"
            :placeholder="field.label"
            :label="field.label"
            :type="field.type"
            :rowInput="true"
            v-model="searchModel[field.key] as string | number | Date | undefined"
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
const props = defineProps<{
  initialModel: Record<string, unknown>
  searchFields: EntitySearchField[]
  defaultActive?: string[]
}>()

// ✅ Emits
const emit = defineEmits<{
  (e: 'update:filter', filters: Record<string, unknown>): void
  (e: 'search', event?: Event): void
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
}

const emitSearch = (e?: Event) => {
  updateFilters()
  emit('search', e)
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
