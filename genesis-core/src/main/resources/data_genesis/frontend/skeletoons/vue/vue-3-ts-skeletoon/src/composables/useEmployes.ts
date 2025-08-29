import { ref } from 'vue'
import { Employe, EmployeFormDTO } from '../models/EmployeModel'
import * as employeService from '@/services/EmployeService'
import * as departementService from '@/services/DepartementService'
import { useLoading } from './useLoading'
import { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { PaginationData } from '@/models/api/PageResponseModel'
import { useRouter } from 'vue-router'

/**
 * Composable to manage Employe entities including CRUD operations,
 * search, pagination, foreign key loading, and navigation.
 *
 * @param autoLoad Automatically fetch all employes on mounted (default: true)
 */
export function useEmployes() {
  /** List of all employes */
  const employes = ref<Employe[]>([])

  /** Loading state management */
  const { loading, startLoading, stopLoading } = useLoading()

  /** Pagination information returned by API */
  const paginationData = ref(new PaginationData())

  /** Message for errors or notifications */
  const message = ref<string | null>()

  /** Vue router instance for navigation */
  const router = useRouter()

  /**
   * Load foreign key data for forms (employes and employes)
   * @returns An object containing arrays of external data
   */
  const loadFkMapData = async () => {
    const { data: departements } = await departementService.getAll()

    return {
      departements,
    }
  }

  /**
   * Handles API response by updating the employes and pagination
   * @param data Array of Employe returned by the API
   * @param err Optional error message
   * @param pagination Optional pagination data
   */
  const handleResponse = async (
    data: Employe[],
    err: string | undefined,
    pagination?: PaginationData,
    concatData: boolean = false,
  ) => {
    if (err) {
      message.value = err
    }
    if (concatData) {
      employes.value = employes.value.concat(data)
    } else {
      employes.value = data
    }
    if (pagination !== undefined) {
      paginationData.value = new PaginationData(pagination)
    }
  }

  // Navigation functions
  /** Navigate to employe detail view */
  const viewEmploye = (employe: Partial<Employe>) => {
    router.push({ name: 'employedetailsview', params: { id: employe.id } })
  }

  /** Navigate to employe list view */
  const goToListView = () => {
    router.push({ path: '/employes' })
  }

  /** Navigate to employe create form */
  const goToCreateFormView = () => {
    router.push({ path: '/employes/create' })
  }

  /** Navigate to employe update form */
  const goToUpdateFormView = (employe: Partial<Employe>) => {
    router.push({ name: 'employeupdateview', params: { id: employe.id } })
  }

  // API Requests
  /** Load all employes from the API */
  const loadEmployes = async () => {
    message.value = null
    startLoading()
    const { data, error: err } = await employeService.getAll()
    handleResponse(data, err)
    stopLoading()
  }

  /**
   * Delete a employe by id
   * @param employe Employe object to delete
   */
  const deleteEmploye = async (employe: Partial<Employe>) => {
    return await employeService.remove(employe.id)
  }

  /**
   * Get a single employe by id
   * @param id Employe ID
   */
  const getEmployeById = async (id: number | string) => {
    return employeService.getById(id)
  }

  /**
   * Create a new employe
   * @param employeFormDTO Employe object to create
   */
  const createEmploye = async (employeFormDTO: Partial<EmployeFormDTO>) => {
    const dto = new EmployeFormDTO(employeFormDTO)
    const employe: Partial<Employe> = await dto.toEntity()
    const { data, error } = await employeService.create(employe)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Update an existing employe
   * @param employeFormDTO Employe object to update
   */
  const updateEmploye = async (id: number | string, employeFormDTO: Partial<EmployeFormDTO>) => {
    const dto = new EmployeFormDTO(employeFormDTO)
    const employe: Partial<Employe> = await dto.toEntity()
    const { data, error } = await employeService.update(id, employe)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Search employes with filters, pagination, and sorting
   * @param filters Partial Employe object containing search filters
   * @param pagination Pagination request parameters
   * @param sortFields Array of fields to sort
   */
  const searchEmployes = async (
    unpagined: boolean = false,
    filters: Partial<Employe>,
    pagination: PaginationRequestParameter,
    sortFields: SortFieldParameter[],
  ) => {
    message.value = null
    startLoading()
    const {
      data,
      error: err,
      pagination: paginationresult,
    } = await employeService.search(filters, pagination, sortFields)
    handleResponse(data, err, paginationresult)

    if (unpagined) {
      while (paginationData.value.hasNext()) {
        paginationData.value.nextPage()
        const {
          data: nextData,
          error: nextErr,
          pagination: nextPagination,
        } = await employeService.search(filters, paginationData.value.toParameter(), sortFields)
        handleResponse(nextData, nextErr, nextPagination, true)
      }
    }

    stopLoading()
  }

  /** Getter for pagination data */
  const getPaginationData = () => paginationData.value

  // Automatically load employes on mount if autoLoad is true

  return {
    employes,
    loading,
    message,
    loadEmployes,
    searchEmployes,
    getEmployeById,
    deleteEmploye,
    createEmploye,
    updateEmploye,
    loadFkMapData,
    paginationData,
    getPaginationData,
    viewEmploye,
    goToListView,
    goToCreateFormView,
    goToUpdateFormView,
  }
}
