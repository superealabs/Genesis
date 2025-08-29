import { BaseModel } from './BaseModel'
import type { EntitySearchField } from './EntityModel'
import { Departement } from './DepartementModel'
import * as departementService from '@/services/DepartementService'
import { createSelectSearchFunction } from './SelectOption'

export interface IEmploye {
  id?: number

  prenom?: string

  nom?: string

  email?: string

  dateEmbauche?: Date

  salaire?: number

  departementidDepartements?: Departement
}

export class Employe extends BaseModel implements IEmploye {
  id?: number

  prenom?: string

  nom?: string

  email?: string

  dateEmbauche?: Date

  salaire?: number

  departementidDepartements?: Departement

  constructor(data?: Partial<IEmploye>) {
    super()
    Object.assign(this, data)
  }

  static override getReferenceKey(): string {
    return 'prenom'
  }

  public override getReferenceValue(): string {
    return String(this.prenom) + ' ' + String(this.nom)
  }

  protected static override searchDaoMetadata: EntitySearchField[] = [
    { key: 'id', label: 'Id', type: 'number', sortable: true },
    { key: 'idMin', label: 'Id Min', type: 'number', sortable: true },
    { key: 'idMax', label: 'Id Max', type: 'number', sortable: true },

    { key: 'prenom', label: 'Prenom', type: 'text', sortable: true },

    { key: 'nom', label: 'Nom', type: 'text', sortable: true },

    { key: 'email', label: 'Email', type: 'text', sortable: true },

    {
      key: 'dateEmbauche',
      label: 'Date embauche',
      type: 'date',
      sortable: true,
    },
    {
      key: 'dateEmbaucheMin',
      label: 'Date embauche Min',
      type: 'date',
      sortable: true,
    },
    {
      key: 'dateEmbaucheMax',
      label: 'Date embauche Max',
      type: 'date',
      sortable: true,
    },

    { key: 'salaire', label: 'Salaire', type: 'number', sortable: true },
    { key: 'salaireMin', label: 'Salaire Min', type: 'number', sortable: true },
    { key: 'salaireMax', label: 'Salaire Max', type: 'number', sortable: true },

    {
      key: 'departementidDepartements',
      label: 'Departementid departements',
      type: 'select',
      searchKey: 'id',
      sortable: true,
      selectSearch: createSelectSearchFunction(Departement, departementService),
    },
  ]
}

export class EmployeFormDTO {
  id?: number

  prenom?: string

  nom?: string

  email?: string

  dateEmbauche?: Date

  salaire?: number

  departementidDepartements?: string

  constructor(data?: Partial<EmployeFormDTO>) {
    this.id = data?.id

    this.prenom = data?.prenom

    this.nom = data?.nom

    this.email = data?.email

    this.dateEmbauche = data?.dateEmbauche

    this.salaire = data?.salaire

    this.departementidDepartements = data?.departementidDepartements ?? ''
  }

  static parse(data?: Partial<IEmploye> | IEmploye | null) {
    const instance = new EmployeFormDTO()
    instance.id = data?.id

    instance.prenom = data?.prenom

    instance.nom = data?.nom

    instance.email = data?.email

    instance.dateEmbauche = data?.dateEmbauche

    instance.salaire = data?.salaire

    instance.departementidDepartements = data?.departementidDepartements?.id?.toString()

    return instance
  }

  async toEntity(): Promise<Partial<Employe>> {
    const entity: Partial<Employe> = new Employe({
      id: this.id,

      prenom: this.prenom,

      nom: this.nom,

      email: this.email,

      dateEmbauche: this.dateEmbauche,

      salaire: this.salaire,
    })

    if (this.departementidDepartements) {
      const related = await departementService.getById(Number(this.departementidDepartements))
      entity.departementidDepartements = related.data as Departement
    }

    return entity
  }
}
