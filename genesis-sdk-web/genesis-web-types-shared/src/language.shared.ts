/**
 * Représentation minimale d'un langage pour les listes de l'API.
 * Basé sur org.labs.genesis.config.langage.Language
 */
export interface Language {
    id: number;
    name: string;
    extension?: string; // Optionnel, mais utile pour l'affichage UI
}