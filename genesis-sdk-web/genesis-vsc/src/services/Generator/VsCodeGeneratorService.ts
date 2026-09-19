import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IGeneratorService, 
    TableMetadataDto, 
    RelationParameter,
    LoggingLevel
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Generator Service';

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
const MOCK_TABLES: TableMetadataDto[] = [
    { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
    { tableName: 'produit', className: 'Produit', isView: false },
    { tableName: 'categorie', className: 'Categorie', isView: false },
    { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
    { tableName: 'commande', className: 'Commande', isView: false },
];

const MOCK_PARENT_TABLES: TableMetadataDto[] = [
    { tableName: 'utilisateur', className: 'Utilisateur', isView: false },
    { tableName: 'produit', className: 'Produit', isView: false },
    { tableName: 'categorie', className: 'Categorie', isView: false },
    { tableName: 'vue_clients_actifs', className: 'VueClientsActifs', isView: true },
];

const MOCK_CHILD_TABLES: TableMetadataDto[] = [
    { tableName: 'commande', className: 'Commande', isView: false },
    { tableName: 'facture', className: 'Facture', isView: false },
    { tableName: 'detail_commande', className: 'DetailCommande', isView: false },
    { tableName: 'vue_ventes_mensuelles', className: 'VueVentesMensuelles', isView: true },
];

const MOCK_RELATIONS: RelationParameter[] = [
    { parentTable: 'Utilisateur', childTable: 'Commande', mandatory: true, hasForm: true },
    { parentTable: 'Categorie', childTable: 'Produit', mandatory: true, hasForm: false },
    { parentTable: 'Commande', childTable: 'DetailCommande', mandatory: true, hasForm: true },
];

const MOCK_LOGGING_LEVELS: LoggingLevel[] = [
    { id: 'trace', name: 'Trace' },
    { id: 'debug', name: 'Debug' },
    { id: 'info', name: 'Info' },
    { id: 'warn', name: 'Warn' },
    { id: 'error', name: 'Error' },
];

export class VsCodeGeneratorService implements IGeneratorService {
    
    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata_all');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadata API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_TABLES;
        }
    }

    async fetchTablesMetadataParents(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/parents');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataParents API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_PARENT_TABLES;
        }
    }

    async fetchTablesMetadataChilds(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/childs');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataChilds API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_CHILD_TABLES;
        }
    }

    async fetchRelations(): Promise<RelationParameter[]> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/api/relations');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchRelations API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_RELATIONS;
        }
    }

    async fetchLoggingLevels(): Promise<LoggingLevel[]> {
        try {
            const { data } = await getAxiosInstance().get<LoggingLevel[]>('/api/logging_levels');
            return data;
        } catch (error) {
            logger.log(
                LOG_CHANNEL,
                `⚠️ fetchLoggingLevels API échouée. Activation du FALLBACK MOCK.`
            );
            return MOCK_LOGGING_LEVELS;
        }
    }
}