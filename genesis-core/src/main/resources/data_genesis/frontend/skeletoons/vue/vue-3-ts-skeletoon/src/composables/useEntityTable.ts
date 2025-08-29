// src/composables/useEntityTable.ts
import { ref } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { useSortData } from '@/composables/useSortData'
import { usePaginationOptionsStore } from '@/stores/usePaginationOptionsStore'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { useFreezeScreen } from '@/stores/useFreezeScreen'

export function useEntityTable(
  searchFn: (
    filters: Record<string, unknown>,
    pagination: PaginationRequestParameter,
    sort: SortFieldParameter[],
  ) => Promise<void>,
  getPaginationDataRef: () => PaginationData,
) {
  const defaultValueStore = usePaginationOptionsStore()
  const freezeScreenStore = useFreezeScreen()

  // State
  const {
    goToPage,
    page,
    itemsPerPage,
    totalPages,
    getPaginationRequestParameter,
    setPagination,
    jsonPaginationData,
  } = usePagination()

  const { sortFieldsParameters } = useSortData()

  const currentFilters = ref<Record<string, unknown>>({})

  // Actions
  const doSearch = async () => {
    freezeScreenStore.freeze('Fetching data ...')
    await searchFn(currentFilters.value, getPaginationRequestParameter(), sortFieldsParameters)
    setPagination(getPaginationDataRef())
    freezeScreenStore.unfreeze()
  }

  const updateFilters = (filters: Record<string, unknown>) => {
    currentFilters.value = filters
  }

  const changePage = (newPage: number) => {
    goToPage(newPage)
    doSearch()
  }

  // Options pour select pagination
  const pageSizeOptions = defaultValueStore.pagination.itemsPerPageOptions

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
  }
}
