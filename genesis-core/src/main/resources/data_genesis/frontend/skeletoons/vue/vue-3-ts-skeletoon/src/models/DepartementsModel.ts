export interface IDepartements {
  id: number | undefined;
  nomDepartement: string | undefined;
  codeDepartement: string | undefined;
}

export class Departements implements IDepartements {
  id: number | undefined;
  nomDepartement: string | undefined;
  codeDepartement: string | undefined;

  constructor(data?: Partial<IDepartements>) {
    this.id = data?.id;
    this.nomDepartement = data?.nomDepartement;
    this.codeDepartement = data?.codeDepartement;
  }
}
