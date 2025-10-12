import { PaginationRequestParameter } from './RequestModel'

export interface IPageResponse<T> {
  content: Array<T>
  page: PaginationData
}

export class PaginationData {
  size: number
  number: number
  totalElements: number
  totalPages: number

  constructor(data?: Partial<PaginationData>) {
    this.size = data?.size ?? 10
    this.number = data?.number ?? 0
    this.totalPages = data?.totalPages ?? 1
    this.totalElements = data?.totalElements ?? 0
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

  getCurrentPage(): number {
    return this.number + 1
  }

  getEndElement(): number {
    return this.getCurrentPage() * this.size
  }

  calcEndElement(): number {
    let end = this.getEndElement()
    if (end > (this.totalElements ?? 0)) {
      end = this.totalElements ?? 0
    }
    return end
  }

  getStartElement(): number {
    const end = this.getEndElement()
    let start = end - this.size + 1
    if (this.totalElements === 0) {
      start = 0
    }
    return start
  }
}
