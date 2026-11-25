// Appels à genesis-api depuis le serveur du site (adresse interne Docker en ligne, 127.0.0.1 en local).
export async function callGenesis<T = any>(path: string, method = 'GET', body?: unknown): Promise<T> {
  const c = useRuntimeConfig()
  const base = (c.genesisApiInternal as string) || (c.public.genesisApi as string)
  try {
    return await $fetch<T>(base + path, { method: method as any, body: body as any })
  } catch (e: any) {
    if (!e?.response) {
      throw createError({ statusCode: 502, statusMessage: `genesis-api injoignable sur ${base} (le .jar est-il lancé ?)` })
    }
    const d = e.data || {}
    throw createError({ statusCode: 400, statusMessage: `${path} : ${d.message || d.error || 'HTTP ' + e.response.status}` })
  }
}
