<template>
    <div class="flex flex-col gap-4 p-4">
        <h3 class="text-lg font-semibold text-text">Import de script</h3>

        <!-- type="file" : le champ affiche lui-même le bouton dossier, à la taille de l'input -->
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
</template>

<script setup lang="ts">
import type { FileRequestPayload } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import { useScriptFields } from '@genesis-labs/web-core/features/generator/composables/UseScriptFields';

const emit = defineEmits<{
    'request-file-path': [payload: FileRequestPayload];
}>();

const { scriptPath } = useScriptFields();

function handleBrowse() {
    emit('request-file-path', { field: 'script', extensions: ['sql'] });
}
</script>