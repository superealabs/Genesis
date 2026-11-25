// Table t_charte_graphique (Siteback : /tchartegraphiques) — « file » = chemin du PDF envoyé
import { siteback } from './http'
export interface CharteGraphique { id?: number; file: string; version?: string; dateAjout?: string }

export const CharteGraphiqueService = {
  Getall: () => siteback.list<CharteGraphique>('tchartegraphiques', 'id,asc'),
  Set: (c: CharteGraphique) => siteback.save<CharteGraphique>('tchartegraphiques',
    { ...c, dateAjout: c.dateAjout || new Date().toISOString().slice(0, 10) }),
  Delete: (id: number) => siteback.remove('tchartegraphiques', id)
}
