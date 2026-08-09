import type { PaginationData } from './PageResponseModel'

export interface DataResponse<T> {
  status: number
  data?: T
  error?: string
  pagination?: PaginationData
  success: boolean
  errors: Record<string, object> | null
}

export interface PagedDataResponse<T> {
  status: number
  data: T[]
  error?: string
  pagination: PaginationData
  success: boolean
  errors: Record<string, object> | null
}
