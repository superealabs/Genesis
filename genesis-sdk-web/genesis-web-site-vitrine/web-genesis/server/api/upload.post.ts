// POST /api/upload?folder=images|icon|navbar  (multipart, champ « file »)
// → enregistre dans public/upload/<folder>/ et renvoie { path: "/upload/<folder>/<fichier>" }
// Ce chemin est celui à enregistrer dans Siteback (t_equipe.photo, t_charte_graphique.file, t_content.fichier).
export default defineEventHandler(async event => {
  const folder = String(getQuery(event).folder || 'images')
  if (!UPLOAD_FOLDERS.includes(folder)) throw createError({ statusCode: 400, statusMessage: 'Dossier invalide' })
  const parts = await readMultipartFormData(event)
  const file = parts?.find(p => p.name === 'file' && p.filename)
  if (!file) throw createError({ statusCode: 400, statusMessage: 'Aucun fichier reçu (champ « file »)' })
  const name = `${Date.now()}-${safeName(file.filename!)}`
  await useStorage('uploads').setItemRaw(`${folder}:${name}`, file.data)
  return { path: `/upload/${folder}/${name}` }
})
