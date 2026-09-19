import type { FrontendFramework, LanguageDto } from './frontend.shared';

/**
 * Contrat du service Frontend (Pure Interface).
 * Ne contient aucune logique UI ou mutation de store.
 */
export interface IFrontendService {
    fetchFrontendFrameworks(): Promise<FrontendFramework[]>;
    selectFrontendFramework(framework: FrontendFramework): Promise<void>;
    fetchAvailableLanguages(): Promise<LanguageDto[]>;
}