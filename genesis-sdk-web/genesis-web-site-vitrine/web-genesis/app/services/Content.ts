// Table t_content (Siteback : /tcontents).
// « fichier » = chemin du fichier HTML envoyé (public/upload/navbar/...) qui contient le texte de la page.
import { siteback, siteApi } from './http'
import type { Navbar } from './Navbar'

/** Fichier de l'accueil (pas de ligne dans t_content) */
export const HOME_FILE = '/upload/navbar/accueil.html'

export interface Content {
  id?: number; nom: string; route: string; fichier?: string | null; ordre: number
  navbaridTNavbar: Navbar | { id: number }
}

export const ContentService = {
  Getall: () => siteback.list<Content>('tcontents', 'ordre,asc'),
  GetContentId: (id: number) => siteback.get<Content>('tcontents', id),
  GetContentWithNavbar: async (navbar: Navbar) =>
    (await siteback.list<Content>('tcontents', 'ordre,asc')).filter(c => c.navbaridTNavbar?.id === navbar.id),
  GetByRoute: async (route: string) =>
    (await siteback.list<Content>('tcontents', 'ordre,asc')).find(c => c.route === route) ?? null,
  Set: (c: Content) => siteback.save<Content>('tcontents', {
    id: c.id, nom: c.nom, route: c.route, fichier: c.fichier || null, ordre: Number(c.ordre),
    navbaridTNavbar: { id: Number(c.navbaridTNavbar?.id) }
  }),
  Delete: (id: number) => siteback.remove('tcontents', id),

  /** Lit le HTML du fichier de la page */
  ReadFile: (path: string) => siteApi<string>('/content-file', { query: { path }, responseType: 'text' }),
  /** Écrit le HTML dans public/upload/navbar/<nom>.html et renvoie son chemin */
  WriteFile: (name: string, html: string, path?: string | null) =>
    siteApi<{ path: string }>('/content-file', { method: 'PUT', body: { name, html, path } }).then(r => r.path)
}
