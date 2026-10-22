// genesis-sdk-web/genesis-web-types-shared/src/repository/repository.service.interface.ts

import type { RepositoryConfig, RepositoryProviderMetadata } from './repository.shared';

/**
 * Contrat d'interface pour la gestion des fournisseurs de dépôt Git.
 * 
 * Ce contrat est strictement agnostique de l'environnement d'exécution.
 */
export interface IRepositoryService {
  /**
   * Récupère la liste des fournisseurs de dépôt supportés et leurs métadonnées.
   * Permet à l'UI de générer dynamiquement la liste de sélection.
   */
  getSupportedProviders(): Promise<RepositoryProviderMetadata[]>;

  /**
   * Valide les identifiants et la connexion au fournisseur distant.
   * Utile pour vérifier la validité du token ou de la clé SSH avant la génération.
   * 
   * @param config La configuration complète à tester.
   * @returns Un objet indiquant le succès et un message explicite en cas d'échec.
   */
  testConnection(config: RepositoryConfig): Promise<{ success: boolean; message: string }>;

  /**
   * (Optionnel / Futur) Liste les dépôts existants dans un namespace donné.
   * Utile si l'utilisateur choisit "Utiliser un dépôt existant" au lieu d'en créer un nouveau.
   */
  // listExistingRepositories(config: RepositoryConfig): Promise<{ name: string; url: string }[]>;
}