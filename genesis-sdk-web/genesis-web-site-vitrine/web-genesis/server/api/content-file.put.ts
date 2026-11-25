// PUT /api/content-file { name, html, path? }
// → écrit le HTML dans public/upload/navbar/ (réécrit « path » s'il est déjà dans ce dossier) et renvoie { path }
export default defineEventHandler(async event => {
  const { name, html, path } = await readBody<{ name: string; html: string; path?: string | null }>(event)
  let key: string, file: string
  if (path && path.startsWith('/upload/navbar/')) {
    const k = uploadKey(path); key = k.key; file = k.name
  } else {
    file = `${safeName(name || 'page') || 'page'}.html`
    key = `navbar:${file}`
  }
  await useStorage('uploads').setItemRaw(key, Buffer.from(html ?? '', 'utf-8'))
  return { path: `/upload/navbar/${file}` }
})
