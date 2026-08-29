import api from '@/services/api'
import RestResponse from '@/models/api/RestResponseModel'

// On utilise string[] car le backend renvoie une liste de chaînes de caractères
const restApiStringArray = api<string[]>()

// On utilise void car le backend renvoie ResponseEntity<Void>.ok().build() pour le PUT
const restApiVoid = api<void>()

const BASE_URL = '/api/config/columns'

/**
 * Récupère la configuration des champs visibles pour une entité et un type de vue
 *
 * @param entityName Nom de l'entité (ex: 'personne', 'adresse')
 * @param componentType Type de vue ('list' ou 'detail')
 * @returns RestResponse contenant la liste des champs, ou une liste vide si non configuré
 */
export async function getVisibleFields(
  entityName: string,
  componentType: 'list' | 'detail',
): Promise<RestResponse<string[]>> {
  const url = `${BASE_URL}/${entityName}/${componentType}`
  return await restApiStringArray.GET(url)
}

/**
 * Met à jour la configuration des champs visibles pour une entité et un type de vue
 *
 * @param entityName Nom de l'entité (ex: 'personne', 'adresse')
 * @param componentType Type de vue ('list' ou 'detail')
 * @param fields Tableau des clés des champs à afficher
 * @returns RestResponse indiquant le succès ou l'échec de l'opération
 */
export async function updateVisibleFields(
  entityName: string,
  componentType: 'list' | 'detail',
  fields: string[],
): Promise<RestResponse<void>> {
  const url = `${BASE_URL}/${entityName}/${componentType}`
  return await restApiVoid.PUT(url, fields)
}
