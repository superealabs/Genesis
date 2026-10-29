//  1. Import via le manifeste du core (meilleure pratique pour l'encapsulation)
import type { 
    IDatabaseService, 
    DatabaseEngineDto, 
    DatabaseConfig, 
    DatabaseConnectionTestResult 
} from '@genesis-labs/core/features/database/manifest';

//  2. Import de l'INSTANCE singleton du service de communication VS Code
import { vscodeService } from '@/core/services/vscode.service';

export class DatabaseServiceVsc implements IDatabaseService {
    
    //  3. Injection de l'instance singleton par défaut (facilite les tests unitaires)
    constructor(private vscode = vscodeService) {}

    /**
     * Récupère la liste des moteurs de base de données disponibles depuis l'Extension Host.
     *  Retourne une Promise et ne touche PAS au store (Single Responsibility).
     */
    fetchDatabaseEngines(): Promise<DatabaseEngineDto[]> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('GET_DATABASE_ENGINES');
            
            // Écoute la réponse UNE SEULE FOIS, puis nettoie le listener pour éviter les fuites mémoire
            const cleanup = this.vscode.onMessage<DatabaseEngineDto[]>('DATABASE_ENGINES_LOADED', (data) => {
                cleanup();
                resolve(data);
            });

            // Optionnel : ajouter un timeout pour éviter les Promises en attente infinie
            setTimeout(() => {
                cleanup();
                reject(new Error('Délai d\'attente dépassé pour le chargement des moteurs de base de données.'));
            }, 10000); // 10 secondes
        });
    }

    /**
     * Teste la connexion à une base de données avec la configuration fournie.
     * Retourne une Promise avec le résultat du test.
     */
    testDatabaseConnection(config: DatabaseConfig): Promise<DatabaseConnectionTestResult> {
        return new Promise((resolve, reject) => {
            let timer: ReturnType<typeof setTimeout>;
            const cleanups: Array<() => void> = [];
            const done = () => { clearTimeout(timer); cleanups.forEach(c => c()); };

            cleanups.push(this.vscode.onMessage<DatabaseConnectionTestResult>(
                'DATABASE_CONNECTION_TESTED',
                (result) => { done(); resolve(result); }
            ));

            cleanups.push(this.vscode.onMessage<{ command: string; message: string }>(
                'API_ERROR',
                (err) => {
                    if (err.command !== 'TEST_DATABASE_CONNECTION') return;
                    done();
                    reject(new Error(err.message));
                }
            ));

            timer = setTimeout(() => {
                done();
                reject(new Error('Délai d\'attente dépassé pour le test de connexion.'));
            }, 45000); // > timeout axios / JDBC

            this.vscode.sendMessage('TEST_DATABASE_CONNECTION', config);
        });
    }


    selectDatabase(engineId: number): Promise<void> {
        return new Promise((resolve, reject) => {
            this.vscode.sendMessage('SELECT_DATABASE', { id: engineId });
            
            const cleanup = this.vscode.onMessage<any>('DATABASE_SELECTED', (data) => {
                cleanup();
                if (data.success) resolve();
                else reject(new Error('Échec de la sélection de la base de données'));
            });

            const errorCleanup = this.vscode.onMessage<any>('API_ERROR', (data) => {
                if (data.command === 'SELECT_DATABASE') {
                    cleanup(); errorCleanup();
                    reject(new Error(data.message));
                }
            });
        });
    }
}

// 4. Export de l'instance unique (Singleton) prête à être injectée
export const databaseServiceVsc = new DatabaseServiceVsc();