export interface IPageResponse<T> {
  content: Array<T>;
  page: PageData;
}

export class PageData {
  size: number;
  number: number;
  totalElements: number;
  totalPages: number;

  constructor();
  constructor(
    size: number,
    number: number,
    totalElements: number,
    totalPages: number
  );
  constructor(
    size?: number,
    number?: number,
    totalElements?: number,
    totalPages?: number
  ) {
    this.size = size ?? 10;
    this.number = number ?? 0;
    this.totalElements = totalElements ?? 1;
    this.totalPages = totalPages ?? 1;
  }
}
