import type { InjectionKey } from 'vue';
import type { 
    DatabaseEngineDto, 
    DatabaseConfig, 
    DatabaseConnectionTestResult 
} from './database.types';

/**
 * Contrat du service de base de données.
 * Les méthodes retournent des Promises et ne mutent PAS le store directement.
 * C'est au composable (useDatabase) de décider quoi faire des résultats.
 */
export interface IDatabaseService {
    /**
     * Récupère la liste des moteurs de base de données disponibles 
     * (ex: PostgreSQL, MySQL, Oracle) depuis l'API backend.
     */
    fetchDatabaseEngines(): Promise<DatabaseEngineDto[]>;

    /**
     * Teste la connexion à une base de données avec la configuration fournie.
     * @param config La configuration de connexion à tester.
     * @returns Un objet indiquant le succès/échec et un message descriptif.
     */
    testDatabaseConnection(config: DatabaseConfig): Promise<DatabaseConnectionTestResult>;
}

// ═══ Clé d'injection typée (Obligatoire pour provide/inject) ═══
export const DATABASE_SERVICE_KEY: InjectionKey<IDatabaseService> = Symbol('DatabaseService');