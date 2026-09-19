<template>
  <div>
    <!-- TOP FILTER & ACTIONS -->
    <div id="filter-section" class="mt-3 flex items-center gap-4 mb-1">
      <!-- Search -->
      <GenesisSearch
        :initial-model="entityModel"
        v-show="showFilters"
        :searchFields="entitySearchFields"
        :default-active="defaultActiveFilters"
        @update:filter="updateFilters"
        @search="multiCriteriaSearch"
      />

      <!-- Right section -->
      <div class="ml-auto flex items-center gap-4">
        <div class="flex items-center gap-2">
          <label for="select-itemsPerPage" class="text-sm font-medium">
            {{ $t('pagination.sizeLabel') }}
          </label>
          <input
            v-model="itemsPerPage"
            id="inputItemsPerPage"
            type="number"
            class="input input-bordered w-20 h-8 focus:border-0"
            @change="multiCriteriaSearch()"
            @keydown.enter="multiCriteriaSearch()"
          />

          <!-- Bouton pour ouvrir le popup de configuration -->
          <button
            class="btn btn-outline btn-sm h-8 gap-2"
            @click="isColumnPopupVisible = true"
            title="Configurer les colonnes"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              class="h-4 w-4"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                stroke-linecap="round"
                stroke-linejoin="round"
                stroke-width="2"
                d="M9 17V7m0 10a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2h2a2 2 0 012 2m0 10a2 2 0 002 2h2a2 2 0 002-2M9 7a2 2 0 012-2h2a2 2 0 012 2m0 10V7m0 10a2 2 0 002 2h2a2 2 0 002-2V7a2 2 0 00-2-2h-2a2 2 0 00-2 2"
              />
            </svg>
            <span>Colonnes</span>
          </button>

          <GenesisExportCsvButton @click="onExport" type="button" class="btn h-8">
          </GenesisExportCsvButton>
        </div>
      </div>
    </div>

    <!-- TABLE CONTENT -->
    <div class="flex-1 overflow-auto max-h-[70vh]">
      <component
        :is="listComponent"
        :message="message"
        :data="entities"
        :loading="loading"
        :removeAction="removeAction"
        :editAction="editAction"
        :viewAction="viewAction"
        :visibleFields="visibleFields"
        :layout-mode="layoutMode"
        @request:refresh="doSearch"
        @sortby="sortByAndSearch"
      />
    </div>

    <!-- Pagination -->
    <div class="bg-base-100 border-t border-gray-200 mt-1">
      <PaginationLayout
        :start="1"
        :page="page"
        :end="totalPages"
        @update:page="changePage"
        :quickForm="true"
        :total-elements="getPaginationData().totalElements"
        :start-element="getPaginationData().getStartElement()"
        :end-element="getPaginationData().calcEndElement()"
      />
    </div>

    <ColumnVisibilityPopup
      :visible="isColumnPopupVisible"
      :fields="entitySearchFields"
      :visible-fields="visibleFields"
      @update:visible="isColumnPopupVisible = $event"
      @apply="handleApplyColumns"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'

import GenesisSearch from '@/components/search/GenesisSearch.vue'
import PaginationLayout from '@/components/pagination/PaginationLayout.vue'
import GenesisExportCsvButton from '../button/GenesisExportCsvButton.vue'
import ColumnVisibilityPopup from '@/components/popup/ColumnVisibilityPopup.vue'

import { useEntityTable } from '@/composables/useEntityTable'
import type { EntitySearchField } from '@/models/EntityModel'
import type { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'

const props = withDefaults(
  defineProps<{
    entityModel: Record<string, unknown>
    entitySearchFields: EntitySearchField[]
    searchFn: (
      unpagined: boolean,
      filters: Record<string, unknown>,
      pagination: PaginationRequestParameter,
      sortFields: SortFieldParameter[],
    ) => Promise<void>
    getPaginationData: () => PaginationData
    listComponent: object
    entities: unknown[]
    loading?: boolean
    message?: string | null
    defaultActiveFilters?: string[]
    showFilters?: boolean
    editAction?: boolean
    viewAction?: boolean
    removeAction?: boolean
    visibleFields?: string[]
    layoutMode?: 'list' | 'card'
  }>(),
  {
    showFilters: true,
    editAction: true,
    viewAction: true,
    removeAction: true,
    visibleFields: () => [],
    layoutMode: 'list',
  },
)

const emit = defineEmits<{
  (e: 'export:csv'): void
  (e: 'update:visibleFields', value: string[]): void
}>()

const onExport = () => {
  console.log('Export requested')
  emit('export:csv')
}

const { updateFilters, changePage, page, totalPages, itemsPerPage, doSearch, sortByAndSearch } =
  useEntityTable(props.searchFn, props.getPaginationData)

function multiCriteriaSearch(filters?: Record<string, unknown>) {
  updateFilters(filters)
  changePage(1)
}

// État local pour contrôler l'affichage du popup
const isColumnPopupVisible = ref(false)

// Fonction simplifiée : reçoit les champs appliqués et les transmet au parent
const handleApplyColumns = (newFields: string[]) => {
  emit('update:visibleFields', newFields)
}

onMounted(() => {
  multiCriteriaSearch()
})
</script>

<style scoped>
#filter-section {
  z-index: 999;
}
</style>
