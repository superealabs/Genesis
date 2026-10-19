// genesis-sdk-web/genesis-web-types-shared/src/recent/recent.shared.ts


/**
 * Représente un élément récemment utilisé.
 * L'ID peut être un number (pour les Frameworks) ou un string (pour le moteur de DB, ex: 'mysql').
 */
export interface RecentItem {
    id: string | number;
    name: string;
    lastUsed: number; // Timestamp (Date.now()) pour un tri chronologique si nécessaire
}

/**
 * État global de la récence pour le générateur.
 * Séparé par catégorie pour une gestion ciblée et sécurisée.
 */
export interface RecencyData {
    frameworks: RecentItem[];
    databases: RecentItem[]; // L'ID sera la chaîne du moteur (ex: 'postgre', 'mysql')
    frontends: RecentItem[];
}

/**
 * Type utilitaire pour cibler une catégorie de récence.
 */
export type RecencyCategory = 'frameworks' | 'databases' | 'frontends';