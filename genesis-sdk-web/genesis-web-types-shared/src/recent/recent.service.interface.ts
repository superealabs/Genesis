// genesis-sdk-web/genesis-web-types-shared/src/recent/recent.service.interface.ts

import { RecencyCategory, RecencyData, RecentItem } from './recent.types';

/**
 * Contrat d'interface pour la gestion de la récence des éléments utilisés.
 * 
 * Ce contrat est strictement agnostique de l'environnement d'exécution.
 * Il sera implémenté différemment selon le contexte :
 * - Web : via LocalStorage
 * - VS Code : via ExtensionContext.globalState
 * - IntelliJ : via PropertiesComponent (Global)
 */
export interface IRecentService {
  /**
   * Récupère l'ensemble des données de récence (frameworks, databases, frontends).
   * @returns Un objet contenant les trois tableaux de récence.
   */
  getRecencyData(): Promise<RecencyData>;

  /**
   * Récupère uniquement les éléments récents d'une catégorie spécifique.
   * @param category La catégorie ciblée ('frameworks' | 'databases' | 'frontends').
   * @returns Le tableau des éléments récents pour cette catégorie.
   */
  getRecentItems(category: RecencyCategory): Promise<RecentItem[]>;

  /**
   * Ajoute ou met à jour un élément dans la liste de récence d'une catégorie.
   * L'implémentation DOIT gérer automatiquement :
   * 1. La suppression de l'élément s'il existe déjà (pour éviter les doublons).
   * 2. L'ajout de l'élément en tête de liste (index 0).
   * 3. La troncation de la liste à MAX_RECENT_ITEMS éléments.
   * 
   * @param category La catégorie ciblée.
   * @param item L'élément à ajouter ou mettre à jour.
   */
  addRecentItem(category: RecencyCategory, item: RecentItem): Promise<void>;

  /**
   * Vide la liste de récence pour une catégorie spécifique, ou pour toutes si aucune n'est fournie.
   * @param category (Optionnel) La catégorie à vider. Si omis, réinitialise l'ensemble des données.
   */
  clearRecentItems(category?: RecencyCategory): Promise<void>;
}