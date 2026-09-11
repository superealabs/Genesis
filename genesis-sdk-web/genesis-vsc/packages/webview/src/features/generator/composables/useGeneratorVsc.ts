import { useGenerator, useGeneratorStore } from '@genesis-labs/core/features/generator/manifest';
import { 
    selectFolderPathVsc, 
    selectFilePathVsc, 
    selectSqlFilePathVsc 
} from '../services/vsc-generator.utils';

export function useGeneratorVsc() {
    const base = useGenerator();
    const store = useGeneratorStore();

    async function handleSelectFolderPath() {
        console.log("handle folder path from composable of vsc");
        const path = await selectFolderPathVsc();
        if (path) {
            store.updateConfig('projectLocation', path);
            // Optionnel : tu pourrais aussi lire le nom du dossier pour pré-remplir projectName ici
        }
    }

    /**
     * Handler générique pour n'importe quel champ de fichier
     */
    async function handleSelectAnyFile(field: 'script' | 'logoPath' | 'faviconPath', extensions?: string[]) {
        const data = await selectFilePathVsc(extensions);
        if (data.path) {
            if (field === 'script') {
                store.updateScript('path', data.path);
                store.updateScript('content', data.content);
            } else if (field === 'logoPath') {
                store.updateFrontendLayout('logoPath', data.path);
            } else if (field === 'faviconPath') {
                store.updateFrontendLayout('faviconPath', data.path);
            }
        }
    }

    /**
     * Handler spécifique pour le script SQL (utilise la fonction dédiée)
     */
    async function handleSelectSqlFile() {
        const data = await selectSqlFilePathVsc();
        if (data.path) {
            store.updateScript('path', data.path);
            store.updateScript('content', data.content);
        }
    }

    return {
        ...base,
        handleSelectFolderPath,
        handleSelectAnyFile,
        handleSelectSqlFile
    };
}