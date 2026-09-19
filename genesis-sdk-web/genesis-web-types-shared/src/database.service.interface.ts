import type { 
    DatabaseEngineDto, 
    DatabaseConfig, 
    DatabaseConnectionTestResult 
} from './database.shared'; // Ajustez le chemin si nécessaire

/**
 * Contrat du service de base de données (Pure Interface).
 * Les méthodes retournent des Promises et ne mutent PAS le store directement.
 */
export interface IDatabaseService {
    /**
     * Récupère la liste des moteurs de base de données disponibles.
     */
    fetchDatabaseEngines(): Promise<DatabaseEngineDto[]>;

    /**
     * Teste la connexion à une base de données avec la configuration fournie.
     */
    testDatabaseConnection(config: DatabaseConfig): Promise<DatabaseConnectionTestResult>;
    
    
    /**
     * @param engineId L'identifiant du moteur choisi.
     */
    selectDatabase(engineId: number): Promise<void>;
}