export interface IPageResponse<T> {
  content: Array<T>;
  page: IPageModel;
}

export interface IPageModel {
  size: number | undefined;
  number: number | undefined;
  totalElements: number | undefined;
  totalPages: number | undefined;
}
