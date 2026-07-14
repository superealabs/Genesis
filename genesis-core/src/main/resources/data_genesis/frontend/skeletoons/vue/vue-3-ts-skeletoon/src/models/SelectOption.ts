import { useObjectUtils } from '@/composables/useObjectUtils'
import { toRaw } from 'vue'
import type { PaginationRequestParameter } from '@/models/api/RequestModel'
import type { PaginationData } from '@/models/api/PageResponseModel'
import type { BaseModel } from '@/models/BaseModel.ts'

export interface SelectOption {
  label: string
  value: string | number
}

/**
 * Generates a standardized selectSearch function for any entity
 * @param ModelClass The class of the entity (e.g., Projet, Employe)
 * @param service The service that provides search() for that entity
 */
export function createSelectSearchFunction<T extends BaseModel>(
  ModelClass: { createLabelSearchFilter(term: string): object },
  service: {
    search: (
      filter: object,
      pagination: PaginationRequestParameter,
    ) => Promise<{ data: T[]; pagination: PaginationData }>
  },
) {
  return async (
    searchTerm: string,
    pagination: PaginationRequestParameter,
  ): Promise<{ options: SelectOption[]; pagination: PaginationData }> => {
    const response = await service.search(
      ModelClass.createLabelSearchFilter(searchTerm),
      pagination,
    )
    return {
      options: extractSelectOptionsFromOjectsData(response.data, (obj: object) => {
        const entity = obj as T
        return {
          value: entity.getKeyValue(),
          label: entity.getKeyValue(),
        }
      }),
      pagination: response.pagination,
    }
  }
}

export function createMulticriteriatSearchFunction<T extends BaseModel>(service: {
  search: (
    filter: object,
    pagination: PaginationRequestParameter,
  ) => Promise<{ data: T[]; pagination: PaginationData }>
}) {
  return async (
    filters: Record<string, unknown>,
    pagination: PaginationRequestParameter,
  ): Promise<{ options: SelectOption[]; pagination: PaginationData }> => {
    const response = await service.search(filters, pagination)
    return {
      options: extractSelectOptionsFromOjectsData(response.data, (obj: object) => {
        const entity = obj as T
        return {
          value: entity.getKeyValue(),
          label: entity.getKeyValue(),
        }
      }),
      pagination: response.pagination,
    }
  }
}

export function extractSelectOptionsFromOjectsData<T>(
  objects: T[],
  parser?: (data: object) => SelectOption,
) {
  return extractSelectOptionsFromRawsData(toRaw(objects), parser)
}
export function extractSelectOptionsFromRawsData<T>(
  rawData: T[],
  parser?: (data: object) => SelectOption,
): SelectOption[] {
  const options: SelectOption[] = []
  if (!parser) {
    const { getNValue } = useObjectUtils()
    parser = (r: object) => {
      return {
        value: getNValue(r, 0),
        label: getNValue(r, 1),
      }
    }
  }
  rawData.forEach((raw) => {
    options.push(parser(raw as object))
  })
  return options
}
