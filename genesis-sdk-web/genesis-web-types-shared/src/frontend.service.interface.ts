import type { FrontendFramework, FrontendProgrammingLanguage, InterfaceLanguage } from './frontend.shared';

/**
 * Contrat du service Frontend (Pure Interface).
 * Ne contient aucune logique UI ou mutation de store.
 */
export interface IFrontendService {
    fetchFrontendFrameworks(): Promise<FrontendFramework[]>;

    fetchFrontendProgrammingLanguages(): Promise<FrontendProgrammingLanguage[]>;

    /**
     * Récupère la liste des langues d'interface / localisation disponibles 
     * (ex: Français, English, Español) pour la configuration du layout.
     */
    fetchInterfaceLanguages(): Promise<InterfaceLanguage[]>;

    /**
     * Récupère les types de Navbar disponibles (ex: 'Sidebar', 'Topbar').
     * Retourne un tableau de chaînes de caractères pour correspondre à la logique Java.
     */
    fetchNavbarTypes(): Promise<string[]>;
}