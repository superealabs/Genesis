import { vscodeService } from '../../../core/services/vscode.service';

/**
 * 1. Sélection de dossier (inchangé)
 */
export async function selectFolderPathVsc(): Promise<string> {
    console.log("handle folder path from service of vsc ")
    return new Promise((resolve) => {
        vscodeService.sendMessage('REQUEST_FOLDER_PATH');
        const cleanup = vscodeService.onMessage<string>('FOLDER_PATH_SELECTED', (path) => {
            cleanup();
            resolve(path || '');
        });
    });
}

/**
 * 2. Fonction GÉNÉRIQUE : Permet de lire n'importe quel fichier (avec ou sans filtre)
 */
export async function selectFilePathVsc(extensions?: string[]): Promise<{ path: string; content: string }> {
    return new Promise((resolve) => {
        vscodeService.sendMessage('REQUEST_FILE_PATH', { extensions });
        const cleanup = vscodeService.onMessage<{ path: string; content: string }>('FILE_PATH_SELECTED', (data) => {
            cleanup();
            resolve(data || { path: '', content: '' });
        });
    });
}

/**
 * 3. Fonction CONTRAINTE : Utilise la générique en imposant une liste stricte
 */
export async function selectConstrainedFilePathVsc(allowedExtensions: string[]): Promise<{ path: string; content: string }> {
    return selectFilePathVsc(allowedExtensions);
}

/**
 * 4. Fonction SPÉCIFIQUE SQL : Utilise la contrainte avec le paramètre 'sql'
 */
export async function selectSqlFilePathVsc(): Promise<{ path: string; content: string }> {
    return selectConstrainedFilePathVsc(['sql']);
}