<template>
  <div>
    <!-- TOP FILTER -->
    <div id="filter-section" class="mt-3 flex items-center gap-4 mb-1">
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
          <label for="select-itemsPerPage" class="text font-medium">
            {{ $t('pagination.sizeLabel') }}
          </label>
          <input
            v-model="itemsPerPage"
            id="inputItemsPerPage"
            type="number"
            class="input input-bordered w-20 h-8 focus:border-0"
            @change="multiCriteriaSearch"
            @keydown.enter="multiCriteriaSearch"
          />
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

const { updateFilters, changePage, page, totalPages, itemsPerPage, doSearch, sortByAndSearch } =
  useEntityTable(props.searchFn, props.getPaginationData)

function multiCriteriaSearch() {
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
