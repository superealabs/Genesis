<template>
  <div>
    <!-- TOP FILTER -->
    <div
      id="filter-section"
      class="mt-3 bg-base-100 border-b border-gray-200 flex items-center gap-4 p-3"
    >
      <!-- Search -->
      <GenesisSearch
        :initial-model="entityModel"
        :searchFields="entitySearchFields"
        @update:filter="updateFilters"
        @search="multiCriteriaSearch"
      />

      <!-- Right section -->
      <div class="ml-auto items-center gap-4">
        <div class="flex items-center gap-2">
          <label for="select-itemsPerPage" class="text font-medium"> Showing: </label>
          <input v-model="itemsPerPage" id="inputItemsPerPage" type="number" class="input w-12 focus:border-0" @focusout="() => changePage(1)" >
        </div>

        <!-- Future export (example DaisyUI style) -->
        <!--
        <div class="flex items-center gap-2">
          <select id="select-export" class="select select-bordered select">
            <option value="csv">CSV</option>
          </select>
        </div>
        -->
      </div>
    </div>

    <div class="flex-1 overflow-auto max-h-[70vh]">
      <component
        :is="listComponent"
        :message="message"
        :data="entities"
        :loading="loading"
        @request:refresh="doSearch"
      />
    </div>

    <!-- Pagination -->
    <div class="bg-base-100 border-t border-gray-200 p-2">
      <PaginationLayout
        :start="1"
        :page="page"
        :end="totalPages"
        @update:page="changePage"
        :quickForm="true"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'

import GenesisSearch from '@/core/search/GenesisSearch.vue'
import PaginationLayout from '@/core/pagination/PaginationLayout.vue'

import { useEntityTable } from '@/composables/useEntityTable'
import type { EntitySearchField } from '@/models/EntityModel'
import type { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'

const props = defineProps<{
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
}>()

const table = useEntityTable(props.searchFn, props.getPaginationData)

function multiCriteriaSearch() {
  table.changePage(1)
}

onMounted(() => {
  multiCriteriaSearch()
})

const { updateFilters, changePage, page, totalPages, itemsPerPage, doSearch } =
  table
</script>

<style scoped>
#filter-section {
  z-index: 999;
}
</style>
