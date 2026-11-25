// GET /api/content-file?path=/upload/navbar/docs-guide.html  → texte HTML du fichier d'une page (t_content.fichier)
export default defineEventHandler(async event => {
  const { key } = uploadKey(String(getQuery(event).path || ''))
  const raw = await useStorage('uploads').getItemRaw(key)
  if (raw == null) throw createError({ statusCode: 404, statusMessage: 'Fichier de contenu introuvable' })
  setHeader(event, 'Content-Type', 'text/html; charset=utf-8')
  return Buffer.isBuffer(raw) ? raw.toString('utf-8') : typeof raw === 'string' ? raw : Buffer.from(raw).toString('utf-8')
})
