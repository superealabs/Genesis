import type { InjectionKey } from 'vue';
import type { TableMetadataDto, RelationParameter, LanguageDto, /*GeneratorData,*/ DatabaseConfig } from '@genesis-labs/shared-types';

export interface IGeneratorService {    
    // Fetching de données avec Promises (plus de onMessage global qui mute le store)
    fetchTablesMetadataParents(): Promise<TableMetadataDto[]>;
    fetchTablesMetadataChilds(): Promise<TableMetadataDto[]>;
    fetchRelations(): Promise<RelationParameter[]>;
    fetchAvailableLanguages(): Promise<LanguageDto[]>;
    fetchTablesMetadata(): Promise<TableMetadataDto[]>;

    testDatabaseConnection(config: DatabaseConfig): Promise<{ success: boolean; message: string }>;

    // Action finale de génération
    // generateProject(data: GeneratorData): Promise<{ success: boolean; message?: string }>;
}

export const GENERATOR_SERVICE_KEY: InjectionKey<IGeneratorService> = Symbol('GeneratorService');