import { SortFieldParameter } from '@/models/api/RequestModel'
import { ref } from 'vue'

export function useSortData() {
  const sortFieldsParameters = ref<SortFieldParameter[]>([])

  const addSort = (field: string, ascending: boolean) => {
    const existingIndex = sortFieldsParameters.value.findIndex((s) => s.fieldName === field)
    if (existingIndex !== -1) {
      // Update existing sort field
      sortFieldsParameters.value[existingIndex].setAscending(ascending)
    } else {
      // Add new sort field
      sortFieldsParameters.value.push(new SortFieldParameter(field, undefined, ascending))
    }
  }

  const sortBy = (sortData: SortFieldParameter) => {
    sortFieldsParameters.value = [sortData]
  }

  const clearSorts = () => {
    sortFieldsParameters.value = []
  }

  return {
    sortFieldsParameters,
    addSort,
    sortBy,
    clearSorts,
  }
}
