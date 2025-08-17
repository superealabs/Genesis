import { BaseModel } from "./BaseModel";
import { Employe } from "./EmployeModel";
import { Projet } from "./ProjetModel";
import { EntitySearchField } from "./EntityModel";
import * as EmployeService from "@/services/EmployeService";
import * as ProjetService from "@/services/ProjetService";
import { createOptionsLoader } from "./SelectOption";

export interface ITache {
  id?: number;
  titre?: string;
  description?: string;
  priorite?: number;
  projetidProjets?: Projet;
  assigneaidEmployes?: Employe;
}

export class Tache extends BaseModel implements ITache {
  id?: number;
  titre?: string;
  description?: string;
  priorite?: number;
  projetidProjets?: Projet;
  assigneaidEmployes?: Employe;

  constructor(data?: Partial<ITache>) {
    super();
    Object.assign(this, data);
  }

  protected static override searchDaoMetadata: EntitySearchField[] = [
    { key: "id", label: "Id", type: "number", sortable: true },
    { key: "titre", label: "Titre", type: "text", sortable: true },
    { key: "description", label: "Description", type: "text", sortable: true },
    { key: "priorite", label: "Priorite", type: "number", sortable: true },
    {
      key: "prioriteMin",
      label: "Priorite Min",
      type: "number",
      sortable: false,
    },
    {
      key: "prioriteMax",
      label: "Priorite Max",
      type: "number",
      sortable: false,
    },
    {
      key: "projetidProjets",
      label: "Projet",
      type: "select",
      searchKey: "id",
      sortable: true,
      optionsLoader: createOptionsLoader(ProjetService.getAll),
    },
    {
      key: "assigneaidEmployes",
      label: "Employé assigné",
      type: "select",
      searchKey: "id",
      sortable: true,
      optionsLoader: createOptionsLoader(EmployeService.getAll),
    },
  ];
}

export class TacheFormDTO {
  id?: number;
  titre?: string;
  description?: string;
  priorite?: number;
  projetidProjets?: string;
  assigneaidEmployes?: string;

  constructor(data?: Partial<TacheFormDTO>) {
    this.id = data?.id;
    this.titre = data?.titre;
    this.description = data?.description;
    this.priorite = data?.priorite;
    this.projetidProjets = data?.projetidProjets ?? "";
    this.assigneaidEmployes = data?.assigneaidEmployes ?? "";
  }

  static parseTache(data?: Partial<ITache> | ITache | null) {
    const instance = new TacheFormDTO();
    instance.id = data?.id;
    instance.titre = data?.titre;
    instance.description = data?.description;
    instance.priorite = data?.priorite;
    instance.projetidProjets = data?.projetidProjets?.id?.toString();
    instance.assigneaidEmployes = data?.assigneaidEmployes?.id?.toString();
    return instance;
  }

  async toTache(): Promise<Partial<Tache>> {
    const tache: Partial<Tache> = new Tache({
      id: this.id,
      titre: this.titre,
      description: this.description,
      priorite: this.priorite,
    });

    if (this.projetidProjets) {
      const projet = await ProjetService.getById(Number(this.projetidProjets));
      tache.projetidProjets = projet.data as Projet;
    }

    if (this.assigneaidEmployes) {
      const employe = await EmployeService.getById(
        Number(this.assigneaidEmployes)
      );
      tache.assigneaidEmployes = employe.data as Employe;
    }

    return tache;
  }
}
