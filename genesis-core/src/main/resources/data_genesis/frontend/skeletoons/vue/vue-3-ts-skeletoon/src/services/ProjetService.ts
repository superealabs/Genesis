import useRestApi from '@/composables/useRestApi'
import RestResponse from '@/models/api/RestResponseModel'
import { type IPageResponse } from '@/models/api/PageResponseModel'
import {
  PaginationRequestParameter,
  RequestModel,
  SortFieldParameter,
} from '@/models/api/RequestModel'
import { Projet } from '@/models/ProjetModel'
import type { DataResponse, PagedDataResponse } from '@/models/api/DataResponseModel'

const restApiWithPage = useRestApi<IPageResponse<Projet>>()
const restApi = useRestApi<Projet>()
const BASE_URL = '/projets'

/** Helper to wrap response data in Projet instances */
function wrapProjet(response: DataResponse<Projet> | PagedDataResponse<Projet>) {
  if (Array.isArray(response.data)) {
    response.data = response.data.map((r) => new Projet(r))
  } else if (response.data && typeof response.data === 'object') {
    response.data = new Projet(response.data)
  }
  return response
}

/** GET all avec pagination */
export async function getAll(
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
): Promise<PagedDataResponse<Projet>> {
  const request = new RequestModel(BASE_URL, pagination, sortFields)
  const response = await restApiWithPage.GET(request.buildRequestUrl())
  return RestResponse.handlePagedResponse<Projet>(response)
}

/** GET by ID */
export async function getById(id: number | string | undefined): Promise<DataResponse<Projet>> {
  const response = await restApi.GET(BASE_URL + `/` + id)
  return RestResponse.handleDataResponse<Projet>(response)
}

/** POST create */
export async function create(data: Partial<Projet>): Promise<DataResponse<Projet>> {
  const response = await restApi.POST(BASE_URL, data)
  return RestResponse.handleDataResponse<Projet>(response)
}

/** PUT update */
export async function update(
  id: number | string | undefined,
  data: Partial<Projet>,
): Promise<DataResponse<Projet>> {
  const response = await restApi.PUT(BASE_URL + `/` + id, data)
  return RestResponse.handleDataResponse<Projet>(response)
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
  data: Partial<Projet>,
  pagination?: PaginationRequestParameter,
  sortFields?: SortFieldParameter[],
): Promise<PagedDataResponse<Projet>> {
  const request = new RequestModel(BASE_URL + `/search`, pagination, sortFields)
  const response = await restApiWithPage.POST(request.buildRequestUrl(), data)
  return wrapProjet(RestResponse.handlePagedResponse<Projet>(response)) as PagedDataResponse<Projet>
}

/** GET all, unpaginated (fetches every page internally) */
export async function getAllUnpaginated(
  sortFields?: SortFieldParameter[],
  itemsPerRequest: number = 100,
): Promise<Projet[]> {
  let results: Projet[] = []
  let pagination = new PaginationRequestParameter(0, itemsPerRequest) // ou 100 selon ton API
  let hasNext = true

  while (hasNext) {
    const { data, pagination: paged } = await getAll(pagination, sortFields)
    results = results.concat(data)
    hasNext = paged?.hasNext() ?? false
    if (hasNext) pagination = paged.nextPage().toParameter()
  }

  return results
}

/** POST search, unpaginated (fetches all pages internally) */
export async function searchUnpaginated(
  filters: Partial<Projet>,
  sortFields?: SortFieldParameter[],
): Promise<Projet[]> {
  let results: Projet[] = []
  let pagination = new PaginationRequestParameter(0, 50)
  let hasNext = true

  while (hasNext) {
    const { data, pagination: paged } = await search(filters, pagination, sortFields)
    results = results.concat(data)
    hasNext = paged?.hasNext() ?? false
    if (hasNext) pagination = paged.nextPage().toParameter()
  }

  return results
}
