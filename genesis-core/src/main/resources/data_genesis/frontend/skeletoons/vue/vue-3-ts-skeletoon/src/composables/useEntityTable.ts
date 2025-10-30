// src/composables/useEntityTable.ts
import { ref } from 'vue'
import { usePagination } from '@/composables/usePagination'
import { useSortData } from '@/composables/useSortData'
import { PaginationData } from '@/models/api/PageResponseModel'
import type { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { useFreezeScreenStore } from '@/stores/useFreezeScreenStore.ts'

export function useEntityTable(
  searchFn: (
    unpagined: boolean,
    filters: Record<string, unknown>,
    pagination: PaginationRequestParameter,
    sort: SortFieldParameter[],
  ) => Promise<void>,
  getPaginationDataRef: () => PaginationData,
) {
  const freezeScreenStore = useFreezeScreenStore()

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

  const { sortFieldsParameters, sortBy } = useSortData()

  const currentFilters = ref<Record<string, unknown>>({})

  // Actions
  const doSearch = async () => {
    freezeScreenStore.freeze('Fetching data ...')
    console.log(
      `Searching with filters: ${JSON.stringify(currentFilters.value)}, pagination: ${jsonPaginationData()}, sorts: ${JSON.stringify(sortFieldsParameters.value)}`,
    )
    await searchFn(
      false,
      currentFilters.value,
      getPaginationRequestParameter(),
      sortFieldsParameters.value,
    )
    setPagination(getPaginationDataRef())
    freezeScreenStore.unfreeze()
  }

  const updateFilters = (filters: Record<string, unknown>) => {
    currentFilters.value = filters
  }

  const changePage = async (newPage: number) => {
    goToPage(newPage)
    await doSearch()
  }

  const sortByAndSearch = async (sortData: SortFieldParameter) => {
    sortBy(sortData)
    await doSearch()
  }

  return {
    // State exposé
    currentFilters,
    page,
    itemsPerPage,
    totalPages,
    jsonPaginationData,

    // Actions exposées
    doSearch,
    updateFilters,
    changePage,
    sortByAndSearch,
  }
}
