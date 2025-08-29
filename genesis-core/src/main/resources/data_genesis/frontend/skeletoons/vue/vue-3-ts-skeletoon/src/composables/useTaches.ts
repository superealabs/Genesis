import { ref, onMounted } from 'vue'
import { Tache, TacheFormDTO } from '../models/TacheModel'
import * as tacheService from '@/services/TacheService'
import { useLoading } from './useLoading'
import { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { PaginationData } from '@/models/api/PageResponseModel'
import { useRouter } from 'vue-router'

/**
 * Composable to manage Tache entities including CRUD operations,
 * search, pagination, foreign key loading, and navigation.
 *
 * @param autoLoad Automatically fetch all taches on mounted (default: true)
 */
export function useTaches(autoLoad = true) {
  /** List of all taches */
  const taches = ref<Tache[]>([])

  /** Loading state management */
  const { loading, startLoading, stopLoading } = useLoading()

  /** Pagination information returned by API */
  const paginationData = ref(new PaginationData())

  /** Message for errors or notifications */
  const message = ref<string | null>()

  /** Vue router instance for navigation */
  const router = useRouter()

  /**
   * Handles API response by updating the taches and pagination
   * @param data Array of Tache returned by the API
   * @param err Optional error message
   * @param pagination Optional pagination data
   */
  const handleResponse = async (
    data: Tache[],
    err: string | undefined,
    pagination?: PaginationData,
  ) => {
    if (err) {
      message.value = err
    }
    taches.value = data
    if (pagination !== undefined) {
      paginationData.value = pagination
    }
  }

  // Navigation functions
  /** Navigate to tache detail view */
  const viewTache = (tache: Partial<Tache>) => {
    router.push({ name: 'tachedetailsview', params: { id: tache.id } })
  }

  /** Navigate to tache list view */
  const goToListView = () => {
    router.push({ path: '/taches' })
  }

  /** Navigate to tache create form */
  const goToCreateFormView = () => {
    router.push({ path: '/taches/create' })
  }

  /** Navigate to tache update form */
  const goToUpdateFormView = (tache: Partial<Tache>) => {
    router.push({ name: 'tacheupdateview', params: { id: tache.id } })
  }

  // API Requests
  /** Load all taches from the API */
  const loadTaches = async () => {
    message.value = null
    startLoading()
    const { data, error: err } = await tacheService.getAll()
    handleResponse(data, err)
    stopLoading()
  }

  /**
   * Delete a tache by id
   * @param tache Tache object to delete
   */
  const deleteTache = async (tache: Partial<Tache>) => {
    return await tacheService.remove(tache.id)
  }

  /**
   * Get a single tache by id
   * @param id Tache ID
   */
  const getTacheById = async (id: number | string) => {
    return tacheService.getById(id)
  }

  /**
   * Create a new tache
   * @param tacheFormDTO Tache object to create
   */
  const createTache = async (tacheFormDTO: Partial<TacheFormDTO>) => {
    const dto = new TacheFormDTO(tacheFormDTO)
    const tache: Partial<Tache> = await dto.toEntity()
    const { data, error } = await tacheService.create(tache)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Update an existing tache
   * @param tacheFormDTO Tache object to update
   */
  const updateTache = async (id: number | string, tacheFormDTO: Partial<TacheFormDTO>) => {
    const dto = new TacheFormDTO(tacheFormDTO)
    const tache: Partial<Tache> = await dto.toEntity()
    const { data, error } = await tacheService.update(id, tache)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Search taches with filters, pagination, and sorting
   * @param filters Partial Tache object containing search filters
   * @param pagination Pagination request parameters
   * @param sortFields Array of fields to sort
   */
  const searchTaches = async (
    filters: Partial<Tache>,
    pagination: PaginationRequestParameter,
    sortFields: SortFieldParameter[],
  ) => {
    message.value = null
    startLoading()
    const {
      data,
      error: err,
      pagination: paginationresult,
    } = await tacheService.search(filters, pagination, sortFields)
    handleResponse(data, err, paginationresult)
    stopLoading()
  }

  /** Getter for pagination data */
  const getPaginationData = () => paginationData.value

  // Automatically load taches on mount if autoLoad is true
  if (autoLoad) onMounted(loadTaches)

  return {
    taches,
    loading,
    message,
    loadTaches,
    searchTaches,
    getTacheById,
    deleteTache,
    createTache,
    updateTache,
    paginationData,
    getPaginationData,
    viewTache,
    goToListView,
    goToCreateFormView,
    goToUpdateFormView,
  }
}
