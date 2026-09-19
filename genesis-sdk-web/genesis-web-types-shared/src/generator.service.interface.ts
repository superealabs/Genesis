import type { TableMetadataDto, RelationParameter } from './generator.shared';

/**
 * Contrat du service générateur (Pure Interface).
 * Ne contient aucune logique UI ou mutation de store.
 */
export interface IGeneratorService {
    fetchTablesMetadata(): Promise<TableMetadataDto[]>;
    fetchTablesMetadataParents(): Promise<TableMetadataDto[]>;
    fetchTablesMetadataChilds(): Promise<TableMetadataDto[]>;
    fetchRelations(): Promise<RelationParameter[]>;

    fetchLoggingLevels(frameworkId: number): Promise<string[]>;
    fetchSecurityTypes(frameworkId: number): Promise<string[]>;
    fetchCacheProviders(frameworkId: number): Promise<string[]>; // Ajouté pour cohérence
    fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]>;
    
    fetchLanguageVersions(languageId: number): Promise<string[]>;
    fetchFrameworkVersions(frameworkId: number): Promise<string[]>;
    fetchBuildTools(frameworkId: number): Promise<string[]>;
}