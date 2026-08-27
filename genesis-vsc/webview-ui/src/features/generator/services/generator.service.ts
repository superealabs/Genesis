import { VscodeService } from '@/core/services/vscode.service';
import { useGeneratorStore } from '../store/useGenerator.store';

export class GeneratorService extends VscodeService {
    private _store: ReturnType<typeof useGeneratorStore> | null = null;


    private get store() {
        if (!this._store) {
            this._store = useGeneratorStore();
        }
        return this._store;
    }

    /**
     * Initialisation des écouteurs (OUTPUT)
     */
    init(): void {
        // L'extension renvoie le chemin sélectionné
        this.onMessage<string>('FOLDER_PATH_SELECTED', (path) => {
            if (path) {
                this.store.updateConfig('projectLocation', path);
            }
        });

        this.onMessage<{ path: string; content: string }>('FILE_PATH_SELECTED', ({ path, content }) => {
            if (path) {
                this.store.updateScript('path', path);
                this.store.updateScript('content', content);
            }
        });
    }

    /**
     * Demande à l'extension d'ouvrir le sélecteur de dossier (INPUT)
     */
    requestFolderPath(): void {
        this.sendMessage('REQUEST_FOLDER_PATH');
    }


    /**
     * Générique : demande un fichier avec filtre d'extensions optionnel
     */

    requestFilePath(extensions?: string[]): void {
        this.sendMessage('REQUEST_FILE_PATH', { extensions });
    }

    /**
     * Encapsulation : restreint au fichiers SQL uniquement
     */
    requestScriptPath(): void {
        this.requestFilePath(['sql']);
    }

    fetchTablesMetadata(): void {
        this.sendMessage('GET_TABLES_METADATA');
    }
}

export const generatorService = new GeneratorService();