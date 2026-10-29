<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/script/ScriptImportPanel.vue -->
<template>
    <div class="flex flex-col gap-4 p-4">
        <h3 class="text-lg font-semibold text-text">Import de script</h3>

        <div class="flex items-end gap-2">
            <div class="flex-1">
                <!-- ✅ Utilisation directe de v-model grâce au computed get/set de useScriptFields -->
                <GenesisInput
                    v-model="scriptPath"
                    type="file"
                    accept=".sql"
                    label="Chemin du script"
                    placeholder="/chemin/vers/script.sql"
                    size="lg"
                    fill-width
                    @browse="handleBrowse"
                />
            </div>
            
            <!-- ✅ BOUTON D'EXÉCUTION : Visible uniquement si un chemin ou du contenu existe -->
            <GenesisButton
                v-if="scriptPath || scriptContent"
                variant="primary"
                size="lg"
                :disabled="isExecuting"
                @click="handleExecute"
                class="mb-0.5 shrink-0"
            >
                <span v-if="isExecuting">Exécution...</span>
                <span v-else>Exécuter</span>
            </GenesisButton>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import type { FileRequestPayload } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import { useScriptFields } from '@genesis-labs/web-core/features/generator/composables/UseScriptFields';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

const emit = defineEmits<{
    'request-file-path': [payload: FileRequestPayload];
}>();

// ✅ On récupère uniquement les refs nécessaires. Le v-model gère la mise à jour automatiquement.
const { scriptPath, scriptContent } = useScriptFields();

// ✅ On récupère l'action de sauvegarde du composable principal
const { saveScript } = useGenerator();

const isExecuting = ref(false);

function handleBrowse() {
    emit('request-file-path', { field: 'script', extensions: ['sql'] });
}

async function handleExecute() {
    isExecuting.value = true;
    try {
        await saveScript();
    } catch (error) {
        console.error("Erreur lors de l'exécution du script", error);
    } finally {
        isExecuting.value = false;
    }
}
</script>