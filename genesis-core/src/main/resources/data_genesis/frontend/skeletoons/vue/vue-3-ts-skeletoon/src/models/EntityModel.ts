import type { SelectOption } from './SelectOption'
import type { PaginationRequestParameter } from './api/RequestModel'
import type { PaginationData } from './api/PageResponseModel'

export interface MultiCriteriaSelectSearch {
  filters: EntitySearchField[]
  searchFunction: (
    filters: Record<string, unknown>,
    pagination: PaginationRequestParameter,
  ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
}

// SearchModel.ts
export interface EntitySearchField {
  key: string
  label: string
  type: string
  searchKey?: string | number
  defaultValue?: string
  selectSearch?: (
    searchTerm: string,
    pagination: PaginationRequestParameter,
  ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
  multicriteriaSelect?: MultiCriteriaSelectSearch
  showInTable?: boolean
  sortable?: boolean
  showInFilter?: boolean
  identifier?: boolean
}
