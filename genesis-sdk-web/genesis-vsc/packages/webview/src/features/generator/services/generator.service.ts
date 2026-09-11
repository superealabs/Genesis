import type { 
    IGeneratorService, 
    TableMetadataDto, 
    RelationParameter, 
    LanguageDto, 
    GeneratorData 
} from '@genesis-labs/core/features/generator/manifest';
import { vscodeService } from '../../../core/services/vscode.service';

export class GeneratorServiceVsc implements IGeneratorService {
    constructor(private vscode = vscodeService) {}

    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchTablesMetadataParents(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA_PARENTS');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_PARENTS_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchTablesMetadataChilds(): Promise<TableMetadataDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_TABLES_METADATA_CHILDS');
            const cleanup = this.vscode.onMessage<TableMetadataDto[]>('TABLES_METADATA_CHILDS_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchRelations(): Promise<RelationParameter[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_RELATION_PARAMETERS');
            const cleanup = this.vscode.onMessage<RelationParameter[]>('RELATIONS_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    fetchAvailableLanguages(): Promise<LanguageDto[]> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GET_AVAILABLE_LANGUAGES');
            const cleanup = this.vscode.onMessage<LanguageDto[]>('AVAILABLE_LANGUAGES_LOADED', (data) => {
                cleanup();
                resolve(data);
            });
        });
    }

    generateProject(data: GeneratorData): Promise<{ success: boolean; message?: string }> {
        return new Promise((resolve) => {
            this.vscode.sendMessage('GENERATE_PROJECT', data);
            const cleanup = this.vscode.onMessage<{ success: boolean; message?: string }>('PROJECT_GENERATED', (result) => {
                cleanup();
                resolve(result);
            });
        });
    }
}

export const generatorServiceVsc = new GeneratorServiceVsc();