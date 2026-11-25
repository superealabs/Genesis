// Sert les fichiers envoyés après le build (public/upload/**)
const TYPES: Record<string, string> = {
  png: 'image/png', jpg: 'image/jpeg', jpeg: 'image/jpeg', gif: 'image/gif', svg: 'image/svg+xml',
  webp: 'image/webp', pdf: 'application/pdf', ico: 'image/x-icon', html: 'text/html; charset=utf-8'
}
export default defineEventHandler(async event => {
  const { key, name } = uploadKey('/upload/' + (getRouterParam(event, 'path') || ''))
  const data = await useStorage('uploads').getItemRaw(key)
  if (data == null) throw createError({ statusCode: 404, statusMessage: 'Fichier introuvable' })
  setHeader(event, 'Content-Type', TYPES[name.split('.').pop()!.toLowerCase()] || 'application/octet-stream')
  return data
})
