import { Departement } from "./DepartementModel";
import { EntitySearchField } from "./EntityModel";

export interface IEmploye {
  id: number | undefined;
  prenom: string | undefined;
  nom: string | undefined;
  email: string | undefined;
  dateEmbauche: Date | undefined;
  salaire: number | undefined;
  departementidDepartements: Departement | undefined;
}

export class Employe implements IEmploye {
  id: number | undefined;
  prenom: string | undefined;
  nom: string | undefined;
  email: string | undefined;
  dateEmbauche: Date | undefined;
  salaire: number | undefined;
  departementidDepartements: Departement | undefined;

  constructor(data?: Partial<IEmploye>) {
    this.id = data?.id;
    this.prenom = data?.prenom;
    this.nom = data?.nom;
    this.email = data?.email;
    this.dateEmbauche = data?.dateEmbauche;
    this.salaire = data?.salaire;
    this.departementidDepartements = data?.departementidDepartements;
  }

  static getFieldListMetadata(): EntitySearchField[] {
    return [
      { key: "id", label: "Id", type: "number", sortable: true },
      { key: "prenom", label: "Prenom", type: "text", sortable: true },
      { key: "nom", label: "Nom", type: "text", sortable: true },
      { key: "email", label: "Email", type: "text", sortable: true },
      {
        key: "dateEmbauche",
        label: "Date embauche",
        type: "date",
        sortable: true,
      },
      { key: "salaire", label: "Salaire", type: "number", sortable: true },
      {
        key: "departementidDepartements",
        label: "Departement Id",
        type: "number",
        sortable: true,
      },
    ];
  }
}
