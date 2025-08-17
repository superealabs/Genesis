import useRestApi from "@/composables/useRestApi";
import RestResponse from "@/models/api/RestResponseModel";
import { type IPageResponse } from "@/models/api/PageResponseModel";
import {
  PaginationRequestParameter,
  RequestModel,
  SortFieldParameter,
} from "@/models/api/RequestModel";
import type { Projet } from "@/models/ProjetModel";

const restApiWithPage = useRestApi<IPageResponse<Projet>>();
const BASE_URL = "/projets";

/** GET all avec pagination */
export async function getAll(
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[]
) {
  const request = new RequestModel(BASE_URL, pagination, sortFields);
  const response = await restApiWithPage.GET(request.buildRequestUrl());
  return RestResponse.handleResponse<Projet[]>(response, true);
}

/** GET by ID */
export async function getById(id: string | number) {
  const response = await restApiWithPage.GET(BASE_URL + `/` + id);
  return RestResponse.handleResponse<Projet | null>(response);
}

/** POST create */
export async function create(data: Partial<Projet>) {
  const response = await restApiWithPage.POST(BASE_URL, data);
  return RestResponse.handleResponse<Projet | null>(response);
}

/** PUT update */
export async function update(id: string | number, data: Partial<Projet>) {
  const response = await restApiWithPage.PUT(BASE_URL + `/` + id, data);
  return RestResponse.handleResponse<Projet | null>(response);
}

/** DELETE remove */
export async function remove(id: string | number) {
  const response = await restApiWithPage.DELETE(BASE_URL + `/` + id);
  return {
    success: response.returnCode === 1,
    error: response.returnCode !== 1 ? response.message : null,
  };
}

/** POST search avec pagination */
export async function search(
  data: Partial<Projet>,
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[]
) {
  const request = new RequestModel(
    BASE_URL + `/search`,
    pagination,
    sortFields
  );
  const response = await restApiWithPage.POST(request.buildRequestUrl(), data);
  return RestResponse.handleResponse<Projet[]>(response, true);
}
