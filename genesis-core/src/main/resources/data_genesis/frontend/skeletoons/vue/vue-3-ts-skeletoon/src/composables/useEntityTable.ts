// src/composables/useEntityTable.ts
import { ref } from "vue";
import { usePagination } from "@/composables/usePagination";
import { useSortData } from "@/composables/useSortData";
import { useDefaultDataStore } from "@/store/useDefaultDataStore";
import { PaginationData } from "@/models/api/PageResponseModel";

export function useEntityTable(
  searchFn: (
    filters: Record<string, any>,
    pagination: any,
    sort: any
  ) => Promise<void>,
  getPaginationDataRef: () => PaginationData
) {
  const defaultValueStore = useDefaultDataStore();

  // State
  const {
    goToPage,
    page,
    itemsPerPage,
    totalPages,
    getPaginationRequestParameter,
    setPagination,
    jsonPaginationData,
  } = usePagination();

  const { sortFieldsParameters } = useSortData();

  const currentFilters = ref<Record<string, any>>({});

  // Actions
  const doSearch = async () => {
    await searchFn(
      currentFilters.value,
      getPaginationRequestParameter(),
      sortFieldsParameters
    );
    setPagination(getPaginationDataRef());
  };

  const updateFilters = (filters: Record<string, any>) => {
    currentFilters.value = filters;
  };

  const changePage = (newPage: number) => {
    goToPage(newPage);
    doSearch();
  };

  // Options pour select pagination
  const pageSizeOptions = defaultValueStore.pagination.itemsPerPageOptions;

  return {
    // State exposé
    currentFilters,
    page,
    itemsPerPage,
    totalPages,
    pageSizeOptions,
    jsonPaginationData,

    // Actions exposées
    doSearch,
    updateFilters,
    changePage,
  };
}
