import useRestApi from "@/composables/useRestApi";
import RestResponse from "@/models/api/RestResponseModel";
import { type IPageResponse } from "@/models/api/PageResponseModel";
import {
  PaginationRequestParameter,
  RequestModel,
  SortFieldParameter,
} from "@/models/api/RequestModel";
import type { Tache } from "@/models/TacheModel";

const restApiWithPage = useRestApi<IPageResponse<Tache>>();
const restApi = useRestApi<Tache>();
const BASE_URL = "/taches";

/** GET all avec pagination */
export async function getAll(
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[]
) {
  const request = new RequestModel(BASE_URL, pagination, sortFields);
  const response = await restApiWithPage.GET(request.buildRequestUrl());
  return RestResponse.handleResponse<Tache[]>(response, true);
}

/** GET by ID */
export async function getById(id: number | string | undefined) {
  const response = await restApi.GET(BASE_URL + `/` + id);
  return RestResponse.handleResponse<Tache | null>(response);
}

/** POST create */
export async function create(data: Partial<Tache>) {
  const response = await restApi.POST(BASE_URL, data);
  return RestResponse.handleResponse<Tache | null>(response);
}

/** PUT update */
export async function update(
  id: number | string | undefined,
  data: Partial<Tache>
) {
  const response = await restApi.PUT(BASE_URL + `/` + id, data);
  return RestResponse.handleResponse<Tache | null>(response);
}

/** DELETE remove */
export async function remove(id: number | string | undefined) {
  const response = await restApi.DELETE(BASE_URL + `/` + id);
  return {
    success: response.returnCode === 1,
    error: response.returnCode !== 1 ? response.message : null,
  };
}

/** POST search avec pagination */
export async function search(
  data: Partial<Tache>,
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[]
) {
  const request = new RequestModel(
    BASE_URL + `/search`,
    pagination,
    sortFields
  );
  const response = await restApiWithPage.POST(request.buildRequestUrl(), data);
  return RestResponse.handleResponse<Tache[]>(response, true);
}
