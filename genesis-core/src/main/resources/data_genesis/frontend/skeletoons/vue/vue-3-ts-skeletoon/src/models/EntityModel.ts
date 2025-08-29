import type { Ref } from 'vue'
import type { SelectOption } from './SelectOption'
import type { PaginationRequestParameter } from './api/RequestModel'
import type { PaginationData } from './api/PageResponseModel'

export class SelectSearchField<T = unknown> {
  optionsRef: Ref<T[]>
  loader: (modelValue: unknown) => Promise<void>

  constructor(optionsRef: Ref<T[]>, loader: (modelValue: unknown) => Promise<void>) {
    this.optionsRef = optionsRef
    this.loader = loader
  }
}

// SearchModel.ts
export interface EntitySearchField {
  key: string
  label: string
  type: string
  sortable: boolean
  searchKey?: string | number
  defaultValue?: string
  selectSearch?: (
    searchTerm: string,
    pagination: PaginationRequestParameter,
  ) => Promise<{ options: SelectOption[]; pagination: PaginationData }>
}
