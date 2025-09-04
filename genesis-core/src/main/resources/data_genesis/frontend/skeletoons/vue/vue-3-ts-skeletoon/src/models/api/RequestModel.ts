export class PaginationRequestParameter {
  page: number
  size: number

  constructor(page?: number, size?: number) {
    this.page = page ?? 0
    this.size = size ?? 10
  }

  setPage(pageNum: number | undefined) {
    if (!pageNum) {
      return
    }
    this.page = pageNum - 1
  }
}

export class SortFieldParameter {
  fieldName: string
  direction: string

  constructor(fieldName: string, direction: string) {
    this.fieldName = fieldName
    this.direction = direction
  }

  changeDirection() {
    if (this.direction === 'asc') {
      this.direction = 'desc'
    } else if (this.direction === 'desc') {
      this.direction = 'asc'
    } else {
      this.direction = 'asc'
    }
  }
}

export class RequestModel {
  url: string
  pagination: PaginationRequestParameter
  sortFields: SortFieldParameter[]
  data?: Record<string, unknown>

  constructor(
    url: string,
    pagination?: PaginationRequestParameter,
    sortFields?: SortFieldParameter[],
    data?: Record<string, unknown>,
  ) {
    this.url = url
    this.pagination = pagination ?? new PaginationRequestParameter(0, 10)
    this.sortFields = sortFields ?? [new SortFieldParameter('id', 'asc')]
    this.data = data
  }

  buildRequestUrl(): string {
    let urlParameter: string = this.url
    const fields: string[] = []
    const parameters: string[] = []

    parameters.push(`page=${this.pagination.page}`)
    parameters.push(`size=${this.pagination.size}`)

    this.sortFields.forEach((f) => {
      fields.push(`${f.fieldName},${f.direction}`)
    })
    const sortString = fields.join(';')
    parameters.push(`sortParam=${sortString}`)

    const parameterString = parameters.join('&')

    return (urlParameter += '?' + parameterString)
  }
}
