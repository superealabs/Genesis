import { useGenerator, useGeneratorStore } from '@genesis-labs/core/features/generator/manifest';
import { selectFolderPathVsc, selectScriptPathVsc } from '../services/vsc-generator.utils';

/**
 * Composable spécifique à VS Code pour le Generator.
 * Il enrichit le composable du Core avec des actions UI spécifiques à la plateforme.
 */
export function useGeneratorVsc() {
    // 1. On récupère toute la logique métier et l'état du Core
    const base = useGenerator();
    const store = useGeneratorStore();

    // 2. On ajoute les actions spécifiques à VSC
    async function handleSelectFolderPath() {
        const path = await selectFolderPathVsc();
        if (path) {
            // ✅ Le composable VSC met à jour le store du Core
            store.updateConfig('projectLocation', path);
        }
    }

    async function handleSelectScriptPath() {
        const data = await selectScriptPathVsc();
        if (data.path) {
            store.updateScript('path', data.path);
            store.updateScript('content', data.content);
        }
    }

    // 3. On retourne le tout fusionné
    return {
        ...base,
        handleSelectFolderPath,
        handleSelectScriptPath
    };
}