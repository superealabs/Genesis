import useRestApi from '@/composables/useRestApi'
import RestResponse from '@/models/api/RestResponseModel'
import { type IPageResponse } from '@/models/api/PageResponseModel'
import {
  PaginationRequestParameter,
  RequestModel,
  SortFieldParameter,
} from '@/models/api/RequestModel'
import { Employe } from '@/models/EmployeModel'
import type { DataResponse, PagedDataResponse } from '@/models/api/DataResponseModel'

const restApiWithPage = useRestApi<IPageResponse<Employe>>()
const restApi = useRestApi<Employe>()
const BASE_URL = '/employes'

/** Helper to wrap response data in Employe instances */
function wrapEmploye(response: DataResponse<Employe> | PagedDataResponse<Employe>) {
  if (Array.isArray(response.data)) {
    response.data = response.data.map((r) => new Employe(r))
  } else if (response.data && typeof response.data === 'object') {
    response.data = new Employe(response.data)
  }
  return response
}

/** GET all avec pagination */
export async function getAll(
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
) {
  const request = new RequestModel(BASE_URL, pagination, sortFields)
  const response = await restApiWithPage.GET(request.buildRequestUrl())
  return RestResponse.handlePagedResponse<Employe>(response)
}

/** GET by ID */
export async function getById(id: number | string | undefined) {
  const response = await restApi.GET(BASE_URL + `/` + id)
  return RestResponse.handleDataResponse<Employe>(response)
}

/** POST create */
export async function create(data: Partial<Employe>) {
  const response = await restApi.POST(BASE_URL, data)
  return RestResponse.handleDataResponse<Employe>(response)
}

/** PUT update */
export async function update(id: number | string | undefined, data: Partial<Employe>) {
  const response = await restApi.PUT(BASE_URL + `/` + id, data)
  return RestResponse.handleDataResponse<Employe>(response)
}

/** DELETE remove */
export async function remove(id: number | string | undefined) {
  const response = await restApi.DELETE(BASE_URL + `/` + id)
  return {
    success: response.returnCode === 1,
    error: response.returnCode !== 1 ? response.message : null,
  }
}

/** POST search avec pagination */
export async function search(
  data: Partial<Employe>,
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
) {
  const request = new RequestModel(BASE_URL + `/search`, pagination, sortFields)
  const response = await restApiWithPage.POST(request.buildRequestUrl(), data)
  return wrapEmploye(
    RestResponse.handlePagedResponse<Employe>(response),
  ) as PagedDataResponse<Employe>
}
