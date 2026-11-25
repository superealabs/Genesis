// Table t_equipe (Siteback : /tequipes)
import { siteback } from './http'
export interface Equipe {
  id?: number; nom: string; prenom?: string; poste?: string; email?: string | null
  photo?: string; github?: string; linkedin?: string
}

export const EquipeService = {
  Getall: () => siteback.list<Equipe>('tequipes'),
  GetEquipeById: (id: number) => siteback.get<Equipe>('tequipes', id),
  Set: (e: Equipe) => siteback.save<Equipe>('tequipes', { ...e, email: e.email || null }),
  Delete: (id: number) => siteback.remove('tequipes', id)
}
