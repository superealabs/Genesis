import { getAxiosInstance } from '../http/genesisAxiosInstance';
import { logger } from '../LoggerService';
import type { 
    IDatabaseService, 
    DatabaseEngineDto, 
    DatabaseConfig, 
    DatabaseConnectionTestResult 
} from '@genesis-labs/shared-types';

const LOG_CHANNEL = 'Genesis Database Service';

// ═══ DONNÉES STATIQUES (FALLBACK) ═══
// Déplacées ici depuis le Handler
export const MOCK_DATABASE_ENGINES: DatabaseEngineDto[] = [
    { id: 1, name: 'PostgreSQL', driver: 'org.postgresql.Driver', driverName: 'PostgreSQL JDBC Driver', port: '5432', driverType: 'jdbc' },
    { id: 2, name: 'MySQL', driver: 'com.mysql.cj.jdbc.Driver', driverName: 'MySQL Connector/J', port: '3306', driverType: 'jdbc' },
    { id: 3, name: 'SQL Server', driver: 'com.microsoft.sqlserver.jdbc.SQLServerDriver', driverName: 'Microsoft JDBC Driver for SQL Server', port: '1433', driverType: 'jdbc' },
    { id: 4, name: 'Oracle', driver: 'oracle.jdbc.OracleDriver', driverName: 'Oracle JDBC Driver', port: '1521', driverType: 'jdbc', sid: 'ORCL' },
    { id: 5, name: 'MongoDB', driver: 'mongodb.jdbc.MongoDriver', driverName: 'Mongo JDBC Driver', port: '27017', driverType: 'jdbc', sid: 'Mongo' }
];

export class VsCodeDatabaseService implements IDatabaseService {
    
    // ✅ LECTURE : Le service garantit un retour (API ou Mock)
    async fetchDatabaseEngines(): Promise<DatabaseEngineDto[]> {
        try {
            const { data } = await getAxiosInstance().get<DatabaseEngineDto[]>('/api/database/engines');
            return data;
        } catch (error) {
            logger.log(LOG_CHANNEL, `⚠️ fetchDatabaseEngines API échouée. Activation du FALLBACK MOCK.`);
            return MOCK_DATABASE_ENGINES;
        }
    }

    // ✅ ACTION : Laisse l'erreur remonter si le réseau/backend échoue
    async testDatabaseConnection(config: DatabaseConfig): Promise<DatabaseConnectionTestResult> {
        // L'API est censée retourner { success: boolean, message: string }
        const { data } = await getAxiosInstance().post<DatabaseConnectionTestResult>('/api/database/test-connection', config);
        return data;
    }

    async selectDatabase(engineId: number): Promise<void> {
        await getAxiosInstance().post(`/api/database/${engineId}/select`);
    }
}