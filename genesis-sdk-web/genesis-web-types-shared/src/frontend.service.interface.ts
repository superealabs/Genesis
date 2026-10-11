import type { FrontendFramework, FrontendProgrammingLanguage, InterfaceLanguage } from './frontend.shared';

/**
 * Contrat du service Frontend (Pure Interface).
 * Ne contient aucune logique UI ou mutation de store.
 */
export interface IFrontendService {
    /**
     * Récupère la liste des frameworks frontend disponibles.
     * (Le backend peut les filtrer si un framework backend est déjà choisi, 
     * ou renvoyer la liste complète).
     */
    fetchFrontendFrameworks(): Promise<FrontendFramework[]>;

    /**
     * Récupère la liste de TOUS les langages de programmation frontend disponibles 
     * (ex: JavaScript, TypeScript). Aucun paramètre n'est nécessaire.
     */
    fetchFrontendProgrammingLanguages(): Promise<FrontendProgrammingLanguage[]>;

    /**
     * Récupère la liste des langues d'interface / localisation disponibles 
     * (ex: Français, English, Español).
     */
    fetchInterfaceLanguages(): Promise<InterfaceLanguage[]>;

    /**
     * Récupère les types de Navbar disponibles (ex: 'Sidebar', 'Topbar').
     */
    fetchNavbarTypes(): Promise<string[]>;
}