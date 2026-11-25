// GET /api/folders?path=D:\projets  → sous-dossiers (sans path : dossier personnel)
export default defineEventHandler(event => {
  assertLocalFoldersAllowed()
  return listFolders(getQuery(event).path as string | undefined)
})
