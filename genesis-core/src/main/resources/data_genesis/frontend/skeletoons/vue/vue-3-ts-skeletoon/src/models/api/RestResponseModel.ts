import { PaginationData } from "./PageResponseModel";

export interface IRestResponse<T> {
  status: number;
  timestamp: Date;
  message: string;
  returnCode: number;
  data?: T;
}

export default class RestResponse<T> implements IRestResponse<T> {
  status: number;
  timestamp: Date;
  message: string;
  returnCode: number;
  data?: T;

  constructor(
    status?: number,
    timestamp?: Date,
    message?: string,
    returnCode?: number,
    data?: T
  ) {
    this.status = status ?? 0;
    this.timestamp = timestamp ?? new Date();
    this.message = message ?? "";
    this.returnCode = returnCode ?? 0;
    this.data = data;
  }

  static fromJson<T>(json: any): RestResponse<T> {
    return new RestResponse<T>(
      json.status,
      new Date(json.timestamp),
      json.message,
      json.returnCode,
      json.data
    );
  }

  static handleResponse<T>(
    response: any,
    withPagination = false
  ): {
    data: T;
    error: string | null;
    pagination?: PaginationData;
  } {
    const success = response.returnCode === 1;
    return {
      data: response.data?.content ?? response.data ?? [],
      error: success ? null : response.message,
      ...(withPagination && {
        pagination: success
          ? response.data?.page ?? new PaginationData({ number: 0 })
          : new PaginationData({ number: 0 }),
      }),
    };
  }
}
