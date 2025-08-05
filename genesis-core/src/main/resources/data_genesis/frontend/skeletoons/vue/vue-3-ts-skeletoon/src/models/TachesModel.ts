export interface ITaches {
  id: number;
  titre: string;
  description: string;
  priorite: number;
  projet_id: number;
  assigne_a_id: number;
}

export class Taches implements ITaches {
  id: number;
  titre: string;
  description: string;
  priorite: number;
  projet_id: number;
  assigne_a_id: number;

  constructor();
  constructor(
    id: number,
    titre: string,
    description: string,
    priorite: number,
    projet_id: number,
    assigne_a_id: number
  );
  constructor(
    id?: number,
    titre?: string,
    description?: string,
    priorite?: number,
    projet_id?: number,
    assigne_a_id?: number
  ) {
    this.id = id ?? 0;
    this.titre = titre ?? "";
    this.description = description ?? "";
    this.priorite = priorite ?? 0;
    this.projet_id = projet_id ?? 0;
    this.assigne_a_id = assigne_a_id ?? 0;
  }
}
