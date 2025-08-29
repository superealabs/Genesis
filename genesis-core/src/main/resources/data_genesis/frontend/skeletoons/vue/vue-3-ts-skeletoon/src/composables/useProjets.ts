import { ref } from 'vue'
import { Projet, ProjetFormDTO } from '../models/ProjetModel'
import * as projetService from '@/services/ProjetService'
import { useLoading } from './useLoading'
import { PaginationRequestParameter, SortFieldParameter } from '@/models/api/RequestModel'
import { PaginationData } from '@/models/api/PageResponseModel'
import { useRouter } from 'vue-router'

/**
 * Composable to manage Projet entities including CRUD operations,
 * search, pagination, foreign key loading, and navigation.
 *
 * @param autoLoad Automatically fetch all projets on mounted (default: true)
 */
export function useProjets() {
  /** List of all projets */
  const projets = ref<Projet[]>([])

  /** Loading state management */
  const { loading, startLoading, stopLoading } = useLoading()

  /** Pagination information returned by API */
  const paginationData = ref(new PaginationData())

  /** Message for errors or notifications */
  const message = ref<string | null>()

  /** Vue router instance for navigation */
  const router = useRouter()

  /**
   * Handles API response by updating the projets and pagination
   * @param data Array of Projet returned by the API
   * @param err Optional error message
   * @param pagination Optional pagination data
   */
  const handleResponse = async (
    data: Projet[],
    err: string | undefined,
    pagination?: PaginationData,
    concatData: boolean = false,
  ) => {
    if (err) {
      message.value = err
    }
    if (concatData) {
      projets.value = projets.value.concat(data)
    } else {
      projets.value = data
    }
    if (pagination !== undefined) {
      paginationData.value = new PaginationData(pagination)
    }
  }

  // Navigation functions
  /** Navigate to projet detail view */
  const viewProjet = (projet: Partial<Projet>) => {
    router.push({ name: 'projetdetailsview', params: { id: projet.id } })
  }

  /** Navigate to projet list view */
  const goToListView = () => {
    router.push({ path: '/projets' })
  }

  /** Navigate to projet create form */
  const goToCreateFormView = () => {
    router.push({ path: '/projets/create' })
  }

  /** Navigate to projet update form */
  const goToUpdateFormView = (projet: Partial<Projet>) => {
    router.push({ name: 'projetupdateview', params: { id: projet.id } })
  }

  // API Requests
  /** Load all projets from the API */
  const loadProjets = async (
    unpagined: boolean = false,
    pagination?: PaginationRequestParameter,
    sortFields?: SortFieldParameter[],
  ) => {
    message.value = null
    startLoading()
    const {
      data,
      error: err,
      pagination: paginationresult,
    } = await projetService.getAll(pagination, sortFields)
    handleResponse(data, err, paginationresult)

    if (unpagined) {
      while (paginationData.value.hasNext()) {
        paginationData.value.nextPage()
        const {
          data: nextData,
          error: nextErr,
          pagination: nextPagination,
        } = await projetService.getAll(paginationData.value.toParameter(), sortFields)
        handleResponse(nextData, nextErr, nextPagination, true)
      }
    }

    stopLoading()
  }

  /**
   * Delete a projet by id
   * @param projet Projet object to delete
   */
  const deleteProjet = async (projet: Partial<Projet>) => {
    return await projetService.remove(projet.id)
  }

  /**
   * Get a single projet by id
   * @param id Projet ID
   */
  const getProjetById = async (id: number | string) => {
    return projetService.getById(id)
  }

  /**
   * Create a new projet
   * @param projetFormDTO Projet object to create
   */
  const createProjet = async (projetFormDTO: Partial<ProjetFormDTO>) => {
    const dto = new ProjetFormDTO(projetFormDTO)
    const projet: Partial<Projet> = await dto.toEntity()
    const { data, error } = await projetService.create(projet)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Update an existing projet
   * @param projetFormDTO Projet object to update
   */
  const updateProjet = async (id: number | string, projetFormDTO: Partial<ProjetFormDTO>) => {
    const dto = new ProjetFormDTO(projetFormDTO)
    const projet: Partial<Projet> = await dto.toEntity()
    const { data, error } = await projetService.update(id, projet)
    if (error) {
      message.value = error
    }
    return data
  }

  /**
   * Search projets with filters, pagination, and sorting
   * @param filters Partial Projet object containing search filters
   * @param pagination Pagination request parameters
   * @param sortFields Array of fields to sort
   */
  const searchProjets = async (
    unpagined: boolean = false,
    filters: Partial<Projet>,
    pagination: PaginationRequestParameter,
    sortFields: SortFieldParameter[],
  ) => {
    message.value = null
    startLoading()
    const {
      data,
      error: err,
      pagination: paginationresult,
    } = await projetService.search(filters, pagination, sortFields)
    handleResponse(data, err, paginationresult)

    if (unpagined) {
      while (paginationData.value.hasNext()) {
        paginationData.value.nextPage()
        const {
          data: nextData,
          error: nextErr,
          pagination: nextPagination,
        } = await projetService.search(filters, paginationData.value.toParameter(), sortFields)
        handleResponse(nextData, nextErr, nextPagination, true)
      }
    }

    stopLoading()
  }

  /** Getter for pagination data */
  const getPaginationData = () => paginationData.value

  return {
    projets,
    loading,
    message,
    loadProjets,
    searchProjets,
    getProjetById,
    deleteProjet,
    createProjet,
    updateProjet,
    paginationData,
    getPaginationData,
    viewProjet,
    goToListView,
    goToCreateFormView,
    goToUpdateFormView,
  }
}
