// genesis-sdk-web/genesis-web-types-shared/src/configStorage/config.service.interface.ts
import { ConfigType } from "./config.types";

/**
 * Structure standardisée d'une configuration Genesis.
 * Le générique T permet de typer strictement le payload en fonction du ConfigType.
 */
export interface GenesisConfig<T = Record<string, unknown>> {
  id: string;            // UUID unique
  name: string;          // Nom lisible par l'utilisateur
  configType: ConfigType; // Catégorie de la configuration
  schemaVersion: string; // Version du schéma (ex: "1.0.0") pour la rétrocompatibilité
  createdAt: string;     // Date de création au format ISO 8601
  updatedAt?: string;    // Date de dernière modification au format ISO 8601
  payload: T;            // Données spécifiques à la configuration
}

/**
 * Contrat d'interface pour la gestion du stockage des configurations.
 * 
 * Ce contrat est agnostique de l'environnement d'exécution.
 * Il sera implémenté différemment pour le Web (LocalStorage/Blob) 
 * et pour VS Code (Extension Host / FileSystem).
 */
export interface IConfigService {
  /**
   * Récupère les configurations stockées localement.
   * @param configType (Optionnel) Si fourni, ne renvoie que les configurations de ce type.
   */
  getAll(configType?: ConfigType): Promise<GenesisConfig[]>;

  /**
   * Sauvegarde ou met à jour une configuration dans le stockage local.
   * @param config La configuration à persister.
   */
  save(config: GenesisConfig): Promise<void>;

  /**
   * Supprime une configuration du stockage local par son identifiant.
   * @param id L'identifiant unique de la configuration à supprimer.
   */
  delete(id: string | number): Promise<void>; // ✅ Corrigé : accepte string | number

  /**
   * Déclenche le processus d'importation (ouverture de dialogue de fichier) 
   * et retourne la ou les configurations parsées et validées.
   * @returns La configuration importée, un tableau de configurations, ou null en cas d'annulation/erreur.
   */
  import(): Promise<GenesisConfig | GenesisConfig[] | null>;

  /**
   * Déclenche le processus d'exportation (téléchargement ou boîte de dialogue "Enregistrer sous") 
   * pour une configuration spécifique.
   * @param config La configuration à exporter.
   */
  export(config: GenesisConfig): Promise<void>;

  /**
   * Déclenche le processus d'exportation pour l'ensemble des configurations fournies.
   * @param configs Le tableau de configurations à exporter dans un seul fichier.
   */
  exportAll(configs: GenesisConfig[]): Promise<void>;
}