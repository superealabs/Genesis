import { EntitySearchField } from "./EntityModel";

export interface IDepartement {
  id: number | undefined;
  nomDepartement: string | undefined;
  codeDepartement: string | undefined;
}

export class Departement implements IDepartement {
  id: number | undefined;
  nomDepartement: string | undefined;
  codeDepartement: string | undefined;

  constructor(data?: Partial<IDepartement>) {
    this.id = data?.id;
    this.nomDepartement = data?.nomDepartement;
    this.codeDepartement = data?.codeDepartement;
  }

  static getFieldListMetadata(): EntitySearchField[] {
    return [
      { key: "id", label: "Id", type: "number", sortable: true },
      {
        key: "nomDepartement",
        label: "Nom departement",
        type: "text",
        sortable: true,
      },
      {
        key: "codeDepartement",
        label: "Code departement",
        type: "text",
        sortable: true,
      },
    ];
  }
}
