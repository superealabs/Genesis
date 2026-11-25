import { siteApi } from './http'
export interface FolderEntry { name: string; path: string }
export interface FolderList { path: string; parent: string | null; home: string; roots: FolderEntry[]; folders: FolderEntry[] }

/** Choix d'un dossier sur la machine (serveur du site, en local) */
export const FolderService = {
  List: (path?: string) => siteApi<FolderList>('/folders', { query: path ? { path } : {} }),
  Create: (parent: string, name: string) => siteApi<FolderList>('/folders', { method: 'POST', body: { parent, name } })
}
