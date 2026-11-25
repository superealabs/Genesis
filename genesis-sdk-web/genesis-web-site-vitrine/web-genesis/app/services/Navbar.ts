// Table t_navbar (Siteback : /tnavbars)
import { siteback } from './http'
import type { Content } from './Content'

export interface Navbar { id?: number; nom: string; ordre: number; contents?: Content[] }

export const NavbarService = {
  Getall: () => siteback.list<Navbar>('tnavbars', 'ordre,asc'),
  /** Navbars avec leurs contenus (sans argument : toutes) */
  GetNavbarWithAllContent: async (navbar?: Navbar | string) => {
    const [navbars, contents] = await Promise.all([
      siteback.list<Navbar>('tnavbars', 'ordre,asc'),
      siteback.list<Content>('tcontents', 'ordre,asc')
    ])
    return navbars
      .filter(n => !navbar || (typeof navbar === 'string' ? n.nom.toLowerCase() === navbar.toLowerCase() : n.id === navbar.id))
      .map(n => ({ ...n, contents: contents.filter(c => c.navbaridTNavbar?.id === n.id) }))
  },
  Set: (n: Navbar) => siteback.save<Navbar>('tnavbars', { id: n.id, nom: n.nom, ordre: Number(n.ordre) }),
  Delete: (id: number) => siteback.remove('tnavbars', id)
}
