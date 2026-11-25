// POST /api/folders { parent, name }  → crée le dossier et l'ouvre
export default defineEventHandler(async event => {
  assertLocalFoldersAllowed()
  const { parent, name } = await readBody<{ parent: string; name: string }>(event)
  return createFolder(parent, name)
})
