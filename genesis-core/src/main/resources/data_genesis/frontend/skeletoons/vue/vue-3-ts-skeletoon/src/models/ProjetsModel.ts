export interface IProjets {
  id: number;
  nom_projet: string;
  budget: number;
  date_debut: Date;
  date_fin_prevue: Date;
}

export class Projets implements IProjets {
  id: number;
  nom_projet: string;
  budget: number;
  date_debut: Date;
  date_fin_prevue: Date;

  constructor();
  constructor(
    id: number,
    nom_projet: string,
    budget: number,
    date_debut: Date,
    date_fin_prevue: Date
  );
  constructor(
    id?: number,
    nom_projet?: string,
    budget?: number,
    date_debut?: Date,
    date_fin_prevue?: Date
  ) {
    this.id = id ?? 0;
    this.nom_projet = nom_projet ?? "";
    this.budget = budget ?? 0;
    this.date_debut = date_debut ?? new Date();
    this.date_fin_prevue = date_fin_prevue ?? new Date();
  }
}
