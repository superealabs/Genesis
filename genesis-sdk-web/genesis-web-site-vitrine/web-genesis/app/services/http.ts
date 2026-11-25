/** Adresse d'un backend : interne (réseau Docker) pendant le rendu serveur, publique dans le navigateur */
function backendBase(kind: 'siteback' | 'genesisApi'): string {
  const c = useRuntimeConfig()
  if (import.meta.server) {
    const internal = (kind === 'siteback' ? c.sitebackInternal : c.genesisApiInternal) as string
    if (internal) return internal
  }
  return c.public[kind] as string
}

export function siteApi<T>(path: string, opts: Parameters<typeof $fetch>[1] = {}) {
  return $fetch<T>(`/api${path}`, opts as any)
}

/** Réponse standard de Siteback (RestResponse) */
interface RestResponse<T> { status: number; message: string; returnCode: number; data: T; errors?: Record<string, string> | null }
interface SpringPage<T> { content: T[]; page?: { totalElements: number } }

function sitebackError(e: any): Error {
  const base = backendBase('siteback')
  if (!e?.response) return new Error(`Siteback injoignable sur ${base} (le backend est-il lancé ?)`)
  const d = e.data || {}
  const details = d.errors && typeof d.errors === 'object' ? ' : ' + Object.values(d.errors).join(', ') : ''
  return new Error((d.message || `HTTP ${e.response.status}`) + details)
}

/** Backend Siteback (Spring Boot généré par Genesis) : /tnavbars, /tcontents, /tequipes, /tchartegraphiques */
export const siteback = {
  async list<T>(entity: string, sort = 'id,asc'): Promise<T[]> {
    const base = backendBase('siteback')
    try {
      const r = await $fetch<RestResponse<SpringPage<T>>>(`${base}/${entity}`, { query: { page: 0, size: 1000, sortParam: sort } })
      return r.data?.content ?? []
    } catch (e: any) {
      if (e?.response?.status === 404) return []    
      throw sitebackError(e)
    }
  },
  async get<T>(entity: string, id: number): Promise<T> {
    const base = backendBase('siteback')
    try { return (await $fetch<RestResponse<T>>(`${base}/${entity}/${id}`)).data }
    catch (e) { throw sitebackError(e) }
  },
  async save<T extends { id?: number }>(entity: string, body: T): Promise<T> {
    const base = backendBase('siteback')
    try {
      const r = body.id
        ? await $fetch<RestResponse<T>>(`${base}/${entity}/${body.id}`, { method: 'PUT', body })
        : await $fetch<RestResponse<T>>(`${base}/${entity}`, { method: 'POST', body })
      return r.data
    } catch (e) { throw sitebackError(e) }
  },
  async remove(entity: string, id: number) {
    const base = backendBase('siteback')
    try { await $fetch(`${base}/${entity}/${id}`, { method: 'DELETE' }) }
    catch (e) { throw sitebackError(e) }
  }
}
export async function genesisApi<T>(path: string, opts: { method?: string; body?: unknown } = {}): Promise<T> {
  const base = backendBase('genesisApi')
  try {
    return await $fetch<T>(base + path, { method: (opts.method || 'GET') as any, body: opts.body as any })
  } catch (e: any) {
    if (!e?.response) throw new Error(`genesis-api injoignable sur ${base} (le .jar est-il lancé ?)`)
    const d = e.data || {}
    throw new Error(d.message || d.error || `HTTP ${e.response.status}`)
  }
}
