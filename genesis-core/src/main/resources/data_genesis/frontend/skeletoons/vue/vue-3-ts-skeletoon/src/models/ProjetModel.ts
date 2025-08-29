import { BaseModel } from './BaseModel'
import type { EntitySearchField } from './EntityModel'

export interface IProjet {
  id?: number

  nomProjet?: string

  budget?: number

  dateDebut?: Date

  dateFinPrevue?: Date
}

export class Projet extends BaseModel implements IProjet {
  id?: number

  nomProjet?: string

  budget?: number

  dateDebut?: Date

  dateFinPrevue?: Date

  constructor(data?: Partial<IProjet>) {
    super()
    Object.assign(this, data)
  }

  static override getReferenceKey(): string {
    return 'nomProjet'
  }

  protected static override searchDaoMetadata: EntitySearchField[] = [
    { key: 'id', label: 'Id', type: 'number', sortable: true },
    { key: 'idMin', label: 'Id Min', type: 'number', sortable: true },
    { key: 'idMax', label: 'Id Max', type: 'number', sortable: true },

    { key: 'nomProjet', label: 'Nom projet', type: 'text', sortable: true },

    { key: 'budget', label: 'Budget', type: 'number', sortable: true },
    { key: 'budgetMin', label: 'Budget Min', type: 'number', sortable: true },
    { key: 'budgetMax', label: 'Budget Max', type: 'number', sortable: true },

    { key: 'dateDebut', label: 'Date debut', type: 'date', sortable: true },
    {
      key: 'dateDebutMin',
      label: 'Date debut Min',
      type: 'date',
      sortable: true,
    },
    {
      key: 'dateDebutMax',
      label: 'Date debut Max',
      type: 'date',
      sortable: true,
    },

    {
      key: 'dateFinPrevue',
      label: 'Date fin prevue',
      type: 'date',
      sortable: true,
    },
    {
      key: 'dateFinPrevueMin',
      label: 'Date fin prevue Min',
      type: 'date',
      sortable: true,
    },
    {
      key: 'dateFinPrevueMax',
      label: 'Date fin prevue Max',
      type: 'date',
      sortable: true,
    },
  ]
}

export class ProjetFormDTO {
  id?: number

  nomProjet?: string

  budget?: number

  dateDebut?: Date

  dateFinPrevue?: Date

  constructor(data?: Partial<ProjetFormDTO>) {
    this.id = data?.id

    this.nomProjet = data?.nomProjet

    this.budget = data?.budget

    this.dateDebut = data?.dateDebut

    this.dateFinPrevue = data?.dateFinPrevue
  }

  static parse(data?: Partial<IProjet> | IProjet | null) {
    const instance = new ProjetFormDTO()
    instance.id = data?.id

    instance.nomProjet = data?.nomProjet

    instance.budget = data?.budget

    instance.dateDebut = data?.dateDebut

    instance.dateFinPrevue = data?.dateFinPrevue

    return instance
  }

  async toEntity(): Promise<Partial<Projet>> {
    const entity: Partial<Projet> = new Projet({
      id: this.id,

      nomProjet: this.nomProjet,

      budget: this.budget,

      dateDebut: this.dateDebut,

      dateFinPrevue: this.dateFinPrevue,
    })

    return entity
  }
}
