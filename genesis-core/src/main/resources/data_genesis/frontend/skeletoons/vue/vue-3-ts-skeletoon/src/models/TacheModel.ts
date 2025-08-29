import { BaseModel } from './BaseModel'
import { type EntitySearchField } from './EntityModel'
import { Projet } from './ProjetModel'
import * as projetService from '@/services/ProjetService'
import { Employe } from './EmployeModel'
import * as employeService from '@/services/EmployeService'

import { createSelectSearchFunction } from './SelectOption'

export interface ITache {
  id?: number

  titre?: string

  description?: string

  priorite?: number

  projetidProjets?: Projet

  assigneaidEmployes?: Employe
}

export class Tache extends BaseModel implements ITache {
  id?: number

  titre?: string

  description?: string

  priorite?: number

  projetidProjets?: Projet

  assigneaidEmployes?: Employe

  constructor(data?: Partial<ITache>) {
    super()
    this.id = data?.id

    this.titre = data?.titre

    this.description = data?.description

    this.priorite = data?.priorite

    this.projetidProjets = new Projet(data?.projetidProjets)

    this.assigneaidEmployes = new Employe(data?.assigneaidEmployes)
  }

  public getRef = (): string => {
    return 'id'
  }

  public getReferenceValue(): string {
    return String(this.titre)
  }

  protected static override searchDaoMetadata: EntitySearchField[] = [
    { key: 'id', label: 'Id', type: 'number', sortable: true },
    { key: 'idMin', label: 'Id Min', type: 'number', sortable: true },
    { key: 'idMax', label: 'Id Max', type: 'number', sortable: true },

    { key: 'titre', label: 'Titre', type: 'text', sortable: true },

    { key: 'description', label: 'Description', type: 'text', sortable: true },

    { key: 'priorite', label: 'Priorite', type: 'number', sortable: true },
    {
      key: 'prioriteMin',
      label: 'Priorite Min',
      type: 'number',
      sortable: true,
    },
    {
      key: 'prioriteMax',
      label: 'Priorite Max',
      type: 'number',
      sortable: true,
    },

    {
      key: 'projetidProjets',
      label: 'Projetid projets',
      type: 'select',
      searchKey: Projet.getKey(),
      sortable: true,
      selectSearch: createSelectSearchFunction(Projet, projetService),
    },

    {
      key: 'assigneaidEmployes',
      label: 'Assigneaid employes',
      type: 'select',
      searchKey: Employe.getKey(),
      sortable: true,
      selectSearch: createSelectSearchFunction(Employe, employeService),
    },
  ]
}

export class TacheFormDTO {
  id?: number

  titre?: string

  description?: string

  priorite?: number

  projetidProjets?: string

  assigneaidEmployes?: string

  constructor(data?: Partial<TacheFormDTO>) {
    this.id = data?.id

    this.titre = data?.titre

    this.description = data?.description

    this.priorite = data?.priorite

    this.projetidProjets = data?.projetidProjets ?? ''

    this.assigneaidEmployes = data?.assigneaidEmployes ?? ''
  }

  static parse(data?: Partial<ITache> | ITache | null) {
    const instance = new TacheFormDTO()
    instance.id = data?.id

    instance.titre = data?.titre

    instance.description = data?.description

    instance.priorite = data?.priorite

    instance.projetidProjets = data?.projetidProjets?.id?.toString()

    instance.assigneaidEmployes = data?.assigneaidEmployes?.id?.toString()

    return instance
  }

  async toEntity(): Promise<Partial<Tache>> {
    const entity: Partial<Tache> = new Tache({
      id: this.id,

      titre: this.titre,

      description: this.description,

      priorite: this.priorite,
    })

    if (this.projetidProjets) {
      const related = await projetService.getById(Number(this.projetidProjets))
      entity.projetidProjets = related.data as Projet
    }
    if (this.assigneaidEmployes) {
      const related = await employeService.getById(Number(this.assigneaidEmployes))
      entity.assigneaidEmployes = related.data as Employe
    }

    return entity
  }
}
