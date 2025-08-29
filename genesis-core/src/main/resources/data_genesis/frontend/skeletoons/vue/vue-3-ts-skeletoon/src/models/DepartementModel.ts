import { BaseModel } from './BaseModel'
import type { EntitySearchField } from './EntityModel'

export interface IDepartement {
  id?: number

  nomDepartement?: string

  codeDepartement?: string
}

export class Departement extends BaseModel implements IDepartement {
  id?: number

  nomDepartement?: string

  codeDepartement?: string

  constructor(data?: Partial<IDepartement>) {
    super()
    Object.assign(this, data)
  }

  protected static override searchDaoMetadata: EntitySearchField[] = [
    { key: 'id', label: 'Id', type: 'number', sortable: true },
    { key: 'idMin', label: 'Id Min', type: 'number', sortable: true },
    { key: 'idMax', label: 'Id Max', type: 'number', sortable: true },

    {
      key: 'nomDepartement',
      label: 'Nom departement',
      type: 'text',
      sortable: true,
    },

    {
      key: 'codeDepartement',
      label: 'Code departement',
      type: 'text',
      sortable: true,
    },
  ]
}

export class DepartementFormDTO {
  id?: number

  nomDepartement?: string

  codeDepartement?: string

  constructor(data?: Partial<DepartementFormDTO>) {
    this.id = data?.id

    this.nomDepartement = data?.nomDepartement

    this.codeDepartement = data?.codeDepartement
  }

  static parse(data?: Partial<IDepartement> | IDepartement | null) {
    const instance = new DepartementFormDTO()
    instance.id = data?.id

    instance.nomDepartement = data?.nomDepartement

    instance.codeDepartement = data?.codeDepartement

    return instance
  }

  async toEntity(): Promise<Partial<Departement>> {
    const entity: Partial<Departement> = new Departement({
      id: this.id,

      nomDepartement: this.nomDepartement,

      codeDepartement: this.codeDepartement,
    })

    return entity
  }
}
