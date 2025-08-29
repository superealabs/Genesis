import useRestApi from '@/composables/useRestApi'
import RestResponse from '@/models/api/RestResponseModel'
import { type IPageResponse } from '@/models/api/PageResponseModel'
import {
  PaginationRequestParameter,
  RequestModel,
  SortFieldParameter,
} from '@/models/api/RequestModel'
import { Tache } from '@/models/TacheModel'
import type { DataResponse, PagedDataResponse } from '@/models/api/DataResponseModel'

const restApiWithPage = useRestApi<IPageResponse<Tache>>()
const restApi = useRestApi<Tache>()
const BASE_URL = '/taches'

/** Helper to wrap response data in Tache instances */
function wrapTache(response: DataResponse<Tache> | PagedDataResponse<Tache>) {
  if (Array.isArray(response.data)) {
    response.data = response.data.map((r) => new Tache(r))
  } else if (response.data && typeof response.data === 'object') {
    response.data = new Tache(response.data)
  }
  return response
}

/** GET all with pagination */
export async function getAll(
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
) {
  const request = new RequestModel(BASE_URL, pagination, sortFields)
  const response = await restApiWithPage.GET(request.buildRequestUrl())
  return wrapTache(RestResponse.handlePagedResponse<Tache>(response)) as PagedDataResponse<Tache>
}

/** GET by ID */
export async function getById(id: number | string | undefined) {
  if (!id) throw new Error('getById: id is required')
  const response = await restApi.GET(`${BASE_URL}/${id}`)
  return wrapTache(RestResponse.handleDataResponse<Tache>(response)) as DataResponse<Tache>
}

/** POST create */
export async function create(data: Partial<Tache>) {
  const response = await restApi.POST(BASE_URL, data)
  return wrapTache(RestResponse.handleDataResponse<Tache>(response)) as DataResponse<Tache>
}

/** PUT update */
export async function update(id: number | string | undefined, data: Partial<Tache>) {
  if (!id) throw new Error('update: id is required')
  const response = await restApi.PUT(`${BASE_URL}/${id}`, data)
  return wrapTache(RestResponse.handleDataResponse<Tache>(response)) as DataResponse<Tache>
}

/** DELETE remove */
export async function remove(id: number | string | undefined) {
  if (!id) throw new Error('remove: id is required')
  const response = await restApi.DELETE(`${BASE_URL}/${id}`)
  return {
    success: response.returnCode === 1,
    error: response.returnCode !== 1 ? response.message : null,
  }
}

/** POST search with pagination */
export async function search(
  data: Partial<Tache>,
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
) {
  const request = new RequestModel(`${BASE_URL}/search`, pagination, sortFields)
  const response = await restApiWithPage.POST(request.buildRequestUrl(), data)
  return wrapTache(RestResponse.handlePagedResponse<Tache>(response)) as PagedDataResponse<Tache>
}
