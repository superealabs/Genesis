import { vscodeService } from '../../../core/services/vscode.service';

/**
 * Utilitaire VSC uniquement : Ouvre le sélecteur de dossier natif de VS Code.
 * Ne fait PAS partie du contrat IGeneratorService.
 */
export async function selectFolderPathVsc(): Promise<string> {
    return new Promise((resolve) => {
        vscodeService.sendMessage('REQUEST_FOLDER_PATH');
        const cleanup = vscodeService.onMessage<string>('FOLDER_PATH_SELECTED', (path) => {
            cleanup();
            resolve(path || '');
        });
    });
}

/**
 * Utilitaire VSC uniquement : Ouvre le sélecteur de fichier (filtré sur .sql).
 */
export async function selectScriptPathVsc(): Promise<{ path: string; content: string }> {
    return new Promise((resolve) => {
        vscodeService.sendMessage('REQUEST_FILE_PATH', { extensions: ['sql'] });
        const cleanup = vscodeService.onMessage<{ path: string; content: string }>('FILE_PATH_SELECTED', (data) => {
            cleanup();
            resolve(data);
        });
    });
}