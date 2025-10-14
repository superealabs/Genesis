import type { DataResponse, PagedDataResponse } from './DataResponseModel'
import { PaginationData, type IPageResponse } from './PageResponseModel'

export interface IRestResponse<T> {
  status: number
  timestamp: Date | string
  message: string
  returnCode: number
  data?: T
  errors: Record<string, string> | null
}

export default class RestResponse<T> implements IRestResponse<T> {
  status: number
  timestamp: Date
  message: string
  returnCode: number
  data?: T
  errors: Record<string, string> | null

  constructor(
    errors: Record<string, string> | null,
    status?: number,
    timestamp?: Date,
    message?: string,
    returnCode?: number,
    data?: T,
  ) {
    this.status = status ?? 0
    this.timestamp = timestamp ?? new Date()
    this.message = message ?? ''
    this.returnCode = returnCode ?? 0
    this.data = data
    this.errors = errors
  }

  static fromJson<T>(json: unknown): RestResponse<T> {
    // Type guard minimal pour sécuriser l'accès aux propriétés
    const obj = (json as Partial<IRestResponse<T>>) ?? {}
    return new RestResponse<T>(
      obj.errors ?? null,
      obj.status,
      obj.timestamp ? new Date(String(obj.timestamp)) : new Date(),
      obj.message ?? '',
      obj.returnCode ?? 0,
      obj.data,
    )
  }

  static handlePagedResponse<T>(response: RestResponse<IPageResponse<T>>): PagedDataResponse<T> {
    const success = response.returnCode === 1
    return {
      success: success,
      status: response.status,
      data: response.data?.content ?? [],
      error: success ? undefined : response.message,
      errors: response.errors,
      pagination: success
        ? (new PaginationData(response.data?.page) ?? new PaginationData({ number: 0 }))
        : new PaginationData({ number: 0 }),
    }
  }

  static handleDataResponse<T>(response: RestResponse<T>): DataResponse<T> {
    const success = response.returnCode === 1
    return {
      success: success,
      status: response.status,
      data: response.data,
      error: success ? undefined : response.message,
      errors: response.errors,
    }
  }
}
