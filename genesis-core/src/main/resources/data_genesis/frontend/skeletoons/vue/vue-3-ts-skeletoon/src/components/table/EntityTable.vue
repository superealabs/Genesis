<template>
  <div>
    <!-- TOP FILTER -->
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
      <div class="ml-auto items-center gap-4">
        <div class="flex items-center gap-2">
          <label for="select-itemsPerPage" class="text font-medium">
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
          <GenesisButton @click="onExport" type="button" class="btn btn-outline w-20 h-8">
            {{ $t('button.export') }}
          </GenesisButton>
        </div>
      </div>
    </div>

    <div class="flex-1 overflow-auto max-h-[70vh]">
      <component
        :is="listComponent"
        :message="message"
        :data="entities"
        :loading="loading"
        :removeAction="removeAction"
        :editAction="editAction"
        :viewAction="viewAction"
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
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

import GenesisSearch from '@/components/search/GenesisSearch.vue'
import PaginationLayout from '@/components/pagination/PaginationLayout.vue'

import { useEntityTable } from '@/composables/useEntityTable'
import type { EntitySearchField } from '@/models/EntityModel'
import type { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import GenesisButton from "@/components/button/GenesisButton.vue";

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
  }>(),
  { showFilters: true, editAction: true, viewAction: true, removeAction: true },
)

const emit = defineEmits<{
  (e: 'export:csv'): void
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

onMounted(() => {
  multiCriteriaSearch()
})
</script>

<style scoped>
#filter-section {
  z-index: 999;
}
</style>
