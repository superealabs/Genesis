import { EntitySearchField } from "./EntityModel";

export interface IProjet {
  id: number | undefined;
  nomProjet: string | undefined;
  budget: number | undefined;
  dateDebut: Date | undefined;
  dateFinPrevue: Date | undefined;
}

export class Projet implements IProjet {
  id: number | undefined;
  nomProjet: string | undefined;
  budget: number | undefined;
  dateDebut: Date | undefined;
  dateFinPrevue: Date | undefined;

  constructor(data?: Partial<IProjet>) {
    this.id = data?.id;
    this.nomProjet = data?.nomProjet;
    this.budget = data?.budget;
    this.dateDebut = data?.dateDebut;
    this.dateFinPrevue = data?.dateFinPrevue;
  }

  static getFieldListMetadata(): EntitySearchField[] {
    return [
      { key: "id", label: "Id", type: "number", sortable: true },
      { key: "nomProjet", label: "Nom projet", type: "text", sortable: true },
      { key: "budget", label: "Budget", type: "number", sortable: true },
      { key: "dateDebut", label: "Date debut", type: "date", sortable: true },
      {
        key: "dateFinPrevue",
        label: "Date fin prevue",
        type: "date",
        sortable: true,
      },
    ];
  }
}
