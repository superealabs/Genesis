import { computed } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

/**
 * Liaisons bidirectionnelles avec le store pour le script.
 * Utilisé par TOUS les panneaux qui touchent au script (import, éditeur de code…) :
 * l'état vit dans le store, donc tous les panneaux voient exactement la même chose.
 */
export function useScriptFields() {
    const { updateScript, stepperData } = useGenerator();

    /** Chemin du script (get/set : utilisable directement avec v-model) */
    const scriptPath = computed({
        get: () => stepperData.value.script.path,
        set: (val) => updateScript('path', val),
    });

    /** Contenu du script */
    const scriptContent = computed({
        get: () => stepperData.value.script.content,
        set: (val) => updateScript('content', val),
    });

    const scriptFileName = computed(() => {
        if (!scriptPath.value) return '';
        return scriptPath.value.split(/[\\/]/).pop() ?? scriptPath.value;
    });

    return { scriptPath, scriptContent, scriptFileName };
}