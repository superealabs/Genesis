import { ref, onMounted } from 'vue'
import { Departement, DepartementFormDTO } from '../models/DepartementModel'
import * as departementService from '@/services/DepartementService'
import { useLoading } from './useLoading'
import { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { PaginationData } from '@/models/api/PageResponseModel'
import { useRouter } from 'vue-router'

/**
 * Composable to manage Departement entities including CRUD operations,
 * search, pagination, foreign key loading, and navigation.
 *
 * @param autoLoad Automatically fetch all departements on mounted (default: true)
 */
export function useDepartements(autoLoad = true) {
  /** List of all departements */
  const departements = ref<Departement[]>([])

  /** Loading state management */
  const { loading, startLoading, stopLoading } = useLoading()

  /** Pagination information returned by API */
  const paginationData = ref(new PaginationData())

  /** Message for errors or notifications */
  const message = ref<string | null>()

  /** Vue router instance for navigation */
  const router = useRouter()

  /**
   * Load foreign key data for forms (projets and employes)
   * @returns An object containing arrays of external data
   */
  const loadFkMapData = async () => {
    return {}
  }

  /**
   * Handles API response by updating the departements and pagination
   * @param data Array of Departement returned by the API
   * @param err Optional error message
   * @param pagination Optional pagination data
   */
  const handleResponse = async (
    data: Departement[],
    err: string | undefined,
    pagination?: PaginationData,
  ) => {
    if (err) {
      message.value = err
    }
    departements.value = data
    if (pagination !== undefined) {
      paginationData.value = pagination
    }
  }

  // Navigation functions
  /** Navigate to departement detail view */
  const viewDepartement = (departement: Partial<Departement>) => {
    router.push({
      name: 'departementdetailsview',
      params: { id: departement.id },
    })
  }

  /** Navigate to departement list view */
  const goToListView = () => {
    router.push({ path: '/departements' })
  }

  /** Navigate to departement create form */
  const goToCreateFormView = () => {
    router.push({ path: '/departements/create' })
  }

  /** Navigate to departement update form */
  const goToUpdateFormView = (departement: Partial<Departement>) => {
    router.push({
      name: 'departementupdateview',
      params: { id: departement.id },
    })
  }

  // API Requests
  /** Load all departements from the API */
  const loadDepartements = async () => {
    message.value = null
    startLoading()
    const { data, error: err } = await departementService.getAll()
    handleResponse(data, err)
    stopLoading()
  }

  /**
   * Delete a departement by id
   * @param departement Departement object to delete
   */
  const deleteDepartement = async (departement: Partial<Departement>) => {
    return await departementService.remove(departement.id)
  }

  /**
   * Get a single departement by id
   * @param id Departement ID
   */
  const getDepartementById = async (id: number | string) => {
    return departementService.getById(id)
  }

  /**
   * Create a new departement
   * @param departementFormDTO Departement object to create
   */
  const createDepartement = async (departementFormDTO: Partial<DepartementFormDTO>) => {
    const dto = new DepartementFormDTO(departementFormDTO)
    const departement: Partial<Departement> = await dto.toEntity()
    const { data, error } = await departementService.create(departement)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Update an existing departement
   * @param departementFormDTO Departement object to update
   */
  const updateDepartement = async (
    id: number | string,
    departementFormDTO: Partial<DepartementFormDTO>,
  ) => {
    const dto = new DepartementFormDTO(departementFormDTO)
    const departement: Partial<Departement> = await dto.toEntity()
    const { data, error } = await departementService.update(id, departement)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Search departements with filters, pagination, and sorting
   * @param filters Partial Departement object containing search filters
   * @param pagination Pagination request parameters
   * @param sortFields Array of fields to sort
   */
  const searchDepartements = async (
    filters: Partial<Departement>,
    pagination: PaginationRequestParameter,
    sortFields: SortFieldParameter[],
  ) => {
    message.value = null
    startLoading()
    const {
      data,
      error: err,
      pagination: paginationresult,
    } = await departementService.search(filters, pagination, sortFields)
    handleResponse(data, err, paginationresult)
    stopLoading()
  }

  /** Getter for pagination data */
  const getPaginationData = () => paginationData.value

  // Automatically load departements on mount if autoLoad is true
  if (autoLoad) onMounted(loadDepartements)

  return {
    departements,
    loading,
    message,
    loadDepartements,
    searchDepartements,
    getDepartementById,
    deleteDepartement,
    createDepartement,
    updateDepartement,
    loadFkMapData,
    paginationData,
    getPaginationData,
    viewDepartement,
    goToListView,
    goToCreateFormView,
    goToUpdateFormView,
  }
}
