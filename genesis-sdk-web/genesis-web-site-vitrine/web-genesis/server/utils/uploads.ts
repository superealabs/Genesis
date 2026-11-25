// Fichiers envoyés : public/upload/<dossier>/<fichier>, exposés sur /upload/<dossier>/<fichier>
export const UPLOAD_FOLDERS = ['images', 'icon', 'navbar']

/** "/upload/navbar/docs-guide.html" → clé de stockage "navbar:docs-guide.html" (refuse les chemins hors upload) */
export function uploadKey(path: string) {
  const m = /^\/upload\/(images|icon|navbar)\/([^/\\]+)$/.exec(path || '')
  if (!m || m[2].includes('..')) {
    throw createError({ statusCode: 400, statusMessage: `Chemin de fichier invalide : ${path}` })
  }
  return { key: `${m[1]}:${m[2]}`, folder: m[1], name: m[2] }
}

export function safeName(name: string) {
  return name.normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^\w.\-]+/g, '-').replace(/^-+|-+$/g, '').toLowerCase()
}
