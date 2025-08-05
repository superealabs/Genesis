export interface IEmployes {
  id: number;
  prenom: string;
  nom: string;
  email: string;
  date_embauche: Date;
  salaire: number;
  departement_id: number;
}

export class Employes implements IEmployes {
  id: number;
  prenom: string;
  nom: string;
  email: string;
  date_embauche: Date;
  salaire: number;
  departement_id: number;

  constructor();
  constructor(
    id: number,
    prenom: string,
    nom: string,
    email: string,
    date_embauche: Date,
    salaire: number,
    departement_id: number
  );
  constructor(
    id?: number,
    prenom?: string,
    nom?: string,
    email?: string,
    date_embauche?: Date,
    salaire?: number,
    departement_id?: number
  ) {
    this.id = id ?? 0;
    this.prenom = prenom ?? "";
    this.nom = nom ?? "";
    this.email = email ?? "";
    this.date_embauche = date_embauche ?? new Date();
    this.salaire = salaire ?? 0;
    this.departement_id = departement_id ?? 0;
  }
}
