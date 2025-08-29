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
      <div class="ml-auto flex items-center gap-4">
        <div class="flex items-center gap-2">
          <label for="select-itemsPerPage" class="text font-medium"> Showing: </label>
          <select
            v-model="itemsPerPage"
            id="select-itemsPerPage"
            class="select select-bordered"
            @change="() => changePage(1)"
          >
            <option v-for="option in pageSizeOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </option>
          </select>
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

<script lang="ts">
import GenesisPopup from '../popup/GenesisPopup.vue'
import { defineComponent, onMounted } from 'vue'
import type { PropType } from 'vue'
import GenesisSearch from '@/core/search/GenesisSearch.vue'
import GenesisButton from '@/core/button/GenesisButton.vue'
import PaginationLayout from '@/core/pagination/PaginationLayout.vue'
import { useEntityTable } from '@/composables/useEntityTable'
import type { EntitySearchField } from '@/models/EntityModel'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'

export default defineComponent({
  name: 'EntityTable',
  components: {
    GenesisSearch,
    GenesisButton,
    PaginationLayout,
    GenesisPopup,
  },
  props: {
    entityModel: {
      type: Object,
      required: true,
    },
    entitySearchFields: {
      type: Array as PropType<EntitySearchField[]>,
      required: true,
    },
    searchFn: {
      type: Function as PropType<
        (
          filters: Record<string, unknown>,
          pagination: PaginationRequestParameter,
          sortFields: SortFieldParameter[],
        ) => Promise<void>
      >,
      required: true,
    },
    getPaginationData: {
      type: Function as PropType<() => PaginationData>,
      required: true,
    },
    listComponent: { type: Object, required: true },
    entities: { type: Array, required: true },
    loading: { type: Boolean, default: false },
    message: { type: [String, null], required: false },
  },
  setup(props) {
    const table = useEntityTable(props.searchFn, props.getPaginationData)
    const multiCriteriaSearch = () => {
      table.changePage(1)
    }

    onMounted(() => {
      multiCriteriaSearch()
    })
    return {
      ...table,
      multiCriteriaSearch,
    }
  },
})
</script>

<style scoped>
#filter-section {
  z-index: 999;
}
</style>
