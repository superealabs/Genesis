export interface IPageResponse<T> {
  content: Array<T>;
  page: PaginationData;
}

export class PaginationData {
  size: number | undefined;
  number: number | undefined;
  totalElements: number | undefined;
  totalPages: number | undefined;

  constructor(data?: Partial<PaginationData>) {
    this.size = data?.size ?? 10;
    this.number = data?.number ?? 1;
    this.totalPages = data?.size ?? 1;
  }
}
