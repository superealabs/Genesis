<template>
  <div>
    <!-- TOP FILTER -->
    <div id="filter-section" class="mt-3 d-flex position-sticky top-0 bg-light">
      <GenesisSearch
        :initial-model="entityModel"
        :searchFields="entitySearchFields"
        @update:filter="updateFilters"
        @search="multiCriteriaSearch"
      />
      <div class="ms-auto d-flex align-items-center gap-2">
        <div class="row">
          <div class="d-flex align-items-center gap-2">
            <label for="select-itemsPerPage">Showing:</label>
            <select
              v-model="itemsPerPage"
              id="select-itemsPerPage"
              class="form-select border-0 shadow-sm"
              @change="() => changePage(1)"
            >
              <option
                v-for="option in pageSizeOptions"
                :key="option.value"
                :value="option.value"
              >
                {{ option.label }}
              </option>
            </select>
          </div>
          <!-- <div class="d-flex align-items-center gap-2">
            <select id="select-export" class="form-select border-0 shadow-sm">
              <option value="csv">CSV</option>
            </select>
          </div> -->
        </div>
      </div>
    </div>

    <component
      :is="listComponent"
      :message="message"
      :data="entities"
      :loading="loading"
      @request:refresh="doSearch"
    />
    <div class="position-sticky bottom-0 bg-light">
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
import GenesisPopup from "../popup/GenesisPopup.vue";
import { defineComponent, onMounted, PropType } from "vue";
import GenesisSearch from "@/core/search/GenesisSearch.vue";
import GenesisButton from "@/core/button/GenesisButton.vue";
import PaginationLayout from "@/core/pagination/PaginationLayout.vue";
import { useEntityTable } from "@/composables/useEntityTable";
import { EntitySearchField } from "@/models/EntityModel";
import { PaginationData } from "@/models/api/PageResponseModel";

export default defineComponent({
  name: "EntityTable",
  components: {
    GenesisSearch,
    GenesisButton,
    PaginationLayout,
    GenesisPopup,
  },
  props: {
    entityModel: {
      type: Object as PropType<Record<string, any>>,
      required: true,
    },
    entitySearchFields: {
      type: Array as PropType<EntitySearchField[]>,
      required: true,
    },
    searchFn: {
      type: Function as PropType<
        (
          filters: Record<string, any>,
          pagination: any,
          sort: any
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
    const table = useEntityTable(props.searchFn, props.getPaginationData);
    const multiCriteriaSearch = () => {
      table.changePage(1);
    };

    onMounted(() => {
      multiCriteriaSearch();
    });
    return {
      ...table,
      multiCriteriaSearch,
    };
  },
});
</script>

<style scoped>
#filter-section {
  z-index: 999;
}
</style>
