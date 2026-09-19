import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IGeneratorService, 
    TableMetadataDto, 
    RelationParameter
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

// CORRECTION : Simples tableaux de strings (plus de Record<number, string[]>)
const MOCK_LOGGING_LEVELS: string[] = ['TRACE', 'DEBUG', 'INFO', 'WARN', 'ERROR'];
const MOCK_SECURITY_TYPES: string[] = ['NONE', 'Basic Authentication', 'JWT', 'OAuth 2.0'];
const MOCK_CACHE_PROVIDERS: string[] = ['NONE', 'Redis', 'Ehcache', 'Caffeine'];

const MOCK_LANGUAGE_VERSIONS: string[] = ['11', '17', '21', '18', '20', '22', '3.9', '3.10', '3.11', '3.12', '8.x', '9.x', '10.x'];
const MOCK_FRAMEWORK_VERSIONS: string[] = ['3.2.0', '3.1.5', '3.0.0', '4.2', '4.1', '10.x', '9.x', '4.4', '4.3'];
const MOCK_BUILD_TOOLS: string[] = ['maven', 'gradle', 'npm', 'yarn', 'pip'];
const MOCK_HIBERNATE_DDL_AUTO: string[] = ['none', 'update', 'validate', 'create-drop', 'create']; // ✅ AJOUT


export class VsCodeGeneratorService implements IGeneratorService {
    
    async fetchTablesMetadata(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata_all');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadata API échouée. Fallback mock.`);
            return MOCK_TABLES;
        }
    }

    async fetchTablesMetadataParents(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/parents');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataParents API échouée. Fallback mock.`);
            return MOCK_PARENT_TABLES;
        }
    }

    async fetchTablesMetadataChilds(): Promise<TableMetadataDto[]> {
        try {
            const { data } = await getAxiosInstance().get<TableMetadataDto[]>('/api/tables_metadata/childs');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchTablesMetadataChilds API échouée. Fallback mock.`);
            return MOCK_CHILD_TABLES;
        }
    }

    async fetchRelations(): Promise<RelationParameter[]> {
        try {
            const { data } = await getAxiosInstance().get<RelationParameter[]>('/api/relations');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchRelations API échouée. Fallback mock.`);
            return MOCK_RELATIONS;
        }
    }

    async fetchLoggingLevels(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/logging_levels/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchLoggingLevels API échouée. Fallback mock.`);
            return MOCK_LOGGING_LEVELS;
        }
    }

    async fetchSecurityTypes(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/security_types/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchSecurityTypes API échouée. Fallback mock.`);
            return MOCK_SECURITY_TYPES;
        }
    }

    async fetchCacheProviders(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/cache_providers/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchCacheProviders API échouée. Fallback mock.`);
            return MOCK_CACHE_PROVIDERS;
        }
    }

    // CORRECTION : Retourne directement le tableau simple en cas d'erreur
    async fetchLanguageVersions(languageId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/language-versions/${languageId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchLanguageVersions échouée. Fallback mock.`);
            return MOCK_LANGUAGE_VERSIONS;
        }
    }

    async fetchFrameworkVersions(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/framework-versions/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchFrameworkVersions échouée. Fallback mock.`);
            return MOCK_FRAMEWORK_VERSIONS;
        }
    }

    async fetchBuildTools(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/generator/build-tools/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchBuildTools échouée. Fallback mock.`);
            return MOCK_BUILD_TOOLS;
        }
    }

    async fetchHibernateDdlAutoOptions(frameworkId: number): Promise<string[]> {
        try {
            const { data } = await getAxiosInstance().get<string[]>(`/api/hibernate_ddl_auto_options/${frameworkId}`);
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchHibernateDdlAutoOptions API échouée. Fallback mock.`);
            return MOCK_HIBERNATE_DDL_AUTO;
        }
    }
}