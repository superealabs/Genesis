import type { PaginationData } from './PageResponseModel'

export interface DataResponse<T> {
  status: number
  data?: T
  error?: string
  pagination?: PaginationData
  success: boolean
}

export interface PagedDataResponse<T> {
  status: number
  data: T[]
  error?: string
  pagination: PaginationData
  success: boolean
}
