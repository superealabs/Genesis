import { useGenerator, useGeneratorStore } from '@genesis-labs/web-core/features/generator/manifest';
import type { FileRequestPayload } from '@genesis-labs/web-core/features/generator/manifest';
import { 
    selectFolderPathVsc, 
    selectFilePathVsc, 
    selectSqlFilePathVsc 
} from '../services/vsc-generator.utils';

export function useGeneratorVsc() {
    const base = useGenerator();
    const store = useGeneratorStore();

    // dqjhdkqjshd
    async function handleSelectFolderPath() {
        const path = await selectFolderPathVsc();
        if (path) {
            store.updateConfig('projectLocation', path);
        }
    }

    function getMimeTypeFromExtension(filePath: string): string {
        const ext = filePath.split('.').pop()?.toLowerCase() || '';
        const mimeTypes: Record<string, string> = {
            'png': 'image/png',
            'jpg': 'image/jpeg',
            'jpeg': 'image/jpeg',
            'gif': 'image/gif',
            'svg': 'image/svg+xml',
            'ico': 'image/x-icon',
            'webp': 'image/webp',
        };
        return mimeTypes[ext] || 'application/octet-stream';
    }

    async function handleSelectAnyFile(field: 'script' | 'logoPath' | 'faviconPath', extensions?: string[]) {
        const data = await selectFilePathVsc(extensions);
        if (data.path) {
            if (field === 'script') {
                store.updateScript('path', data.path);
                store.updateScript('content', data.content);
            } else if (field === 'logoPath') {
                store.updateFrontendLayout('logoPath', data.path);
                // 👇 Stocker le contenu + MIME type
                const mimeType = getMimeTypeFromExtension(data.path);
                store.setFrontendAssetContent('logo', data.content, mimeType);
            } else if (field === 'faviconPath') {
                store.updateFrontendLayout('faviconPath', data.path);
                const mimeType = getMimeTypeFromExtension(data.path);
                store.setFrontendAssetContent('favicon', data.content, mimeType);
            }
        }
    }

    async function handleSelectSqlFile() {
        const data = await selectSqlFilePathVsc();
        if (data.path) {
            store.updateScript('path', data.path);
            store.updateScript('content', data.content);
        }
    }

    // NOUVEAU : Le composable gère la décision (routing) du type de fichier
    async function handleFileRequest(payload: FileRequestPayload) {
        if (payload.field === 'script') {
            await handleSelectSqlFile();
        } else {
            await handleSelectAnyFile(payload.field, payload.extensions);
        }
    }

    return {
        ...base, // Contient déjà setFramework, setDatabaseEngine, setSelectedFrontendFramework, goToNextStep, etc.
        handleSelectFolderPath,
        handleFileRequest, 
        handleSelectSqlFile 
    };
}