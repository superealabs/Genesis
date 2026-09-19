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
}