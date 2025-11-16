<template>
  <div class="flex flex-1 flex-wrap items-center gap-2">
    <!-- Bouton Apply -->
    <GenesisButton
      v-if="!auto"
      title="Apply filter"
      @click="emitSearch"
      type="button"
      class="btn-primary border-0 shadow"
    ><FilterIcon class="mr-2" />
      <span>{{ $t('button.applySearch') }}</span>
    </GenesisButton>

    <!-- Reset -->
    <GenesisButton
      class="btn btn-ghost border-0"
      title="Reset filter"
      type="button"
      :class="activeFields.length ? 'text-error' : 'btn-disabled'"
      @click="clearAllFilters"
    >
      <ReloadIcon />
    </GenesisButton>

    <!-- Filtres actifs -->
    <template v-if="activeFields.length">
      <div
        v-for="field in activeFields"
        :key="field.key"
        class="flex items-center shadow border border-base-300 rounded-md pl-3"
      >
        <GenesisSelectSearch
          v-if="field.type === 'select' && field.selectSearch"
          :label="field.label"
          class="border-0"
          :defaultValue="searchModel[field.key] ?? ''"
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
      <GenesisButton class="btn btn-ghost" type="button" tabindex="0" title="Add filter criteria">
        <PlusIcon />
      </GenesisButton>

      <ul
        tabindex="0"
        class="dropdown-content p-2 menu flex-nowrap shadow bg-base-200 rounded-box h-72 w-80 overflow-y-scroll"
      >
        <template v-if="availableFields.length">
          <li v-for="field in availableFields" :key="field.key">
            <div
              class=""
              :disabled="activeFieldKeys.includes(field.key)"
              @click="onFilterSelected(field.key)"
            >
              {{ field.label }}
            </div>
          </li>
        </template>
        <li v-else class="text-center text-gray-400 italic">No available filter</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useSearch } from '@/composables/useSearch'
import GenesisInput from '@/components/form/GenesisInput.vue'
import GenesisButton from '@/components/button/GenesisButton.vue'
import type { EntitySearchField } from '@/models/EntityModel'
import PlusIcon from '@/components/icons/PlusIcon.vue'
import FilterIcon from '@/components/icons/FilterIcon.vue'
import ReloadIcon from '@/components/icons/ReloadIcon.vue'
import XIcon from '@/components/icons/XIcon.vue'
import GenesisSelectSearch from '@/components/form/GenesisSelectSearch.vue'
import { nextTick, onMounted } from 'vue'

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

const emit = defineEmits<{
  (e: 'update:filter', filters: Record<string, unknown>): void
  (e: 'search', filters: Record<string, unknown>): void
}>()

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
} = useSearch(props.initialModel, props.searchFields, props.defaultActive)

if (props.defaultActive?.length) {
  activeFieldKeys.value.push(...props.defaultActive)
}

const onFilterSelected = (key: string) => {
  selectedFieldToAdd.value = key
  activateField()
  updateFilters()
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

onMounted(async () => {
  // ✅ Attendre le prochain tick pour que Vue mette à jour le DOM
  await nextTick()
  emitSearch()
})
</script>

<style scoped>
.menu {
  flex-direction: column;
}
</style>
