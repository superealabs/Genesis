import { PaginationRequestParameter } from './RequestModel'

export interface IPageResponse<T> {
  content: Array<T>
  page: PaginationData
}

export class PaginationData {
  size: number | undefined
  number: number | undefined
  totalElements: number | undefined
  totalPages: number | undefined

  constructor(data?: Partial<PaginationData>) {
    this.size = data?.size ?? 10
    this.number = data?.number ?? 1
    this.totalPages = data?.totalPages ?? 1
  }

  hasNext(): boolean {
    return !(this.number === Number(this.totalPages) - 1)
  }

  nextPage(): PaginationData {
    if (this.hasNext()) {
      this.number = Number(this.number) + 1
    }
    return this
  }

  toParameter(): PaginationRequestParameter {
    return new PaginationRequestParameter(this.number, this.size)
  }

  reset(size = 10): void {
    this.number = 0
    this.size = size
  }
}
