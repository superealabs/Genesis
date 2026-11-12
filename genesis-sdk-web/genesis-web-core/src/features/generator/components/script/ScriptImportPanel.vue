<template>
    <div class="flex flex-col gap-4 p-4">
        <h3 class="text-lg font-semibold text-text">Import de script</h3>

        <div class="flex items-end gap-2">
            <div class="flex-1">
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

            <!--  Bouton Clear : toujours visible, grisé si rien à effacer -->
            <GenesisButton
                variant="secondary"
                size="lg"
                is-perspective
                :disabled="!scriptPath && !scriptContent"
                @click="handleClear"
                class="mb-0.5 shrink-0"
                title="Effacer le script"
            >
                Clear
            </GenesisButton>

            <!--  Bouton Éditer : conditionné par showEditButton -->
            <GenesisButton
                v-if="showEditButton"
                :disabled="!scriptPath && !scriptContent"
                variant="primary"
                size="lg"
                is-perspective
                @click="handleEdit"
                class="mb-0.5 shrink-0"
                title="Ouvrir l'éditeur de script SQL"
            >
                <template #leftIcon>
                    <IconEdit />
                </template>
                Éditer
            </GenesisButton>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { FileRequestPayload } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import { useScriptFields } from '@genesis-labs/web-core/features/generator/composables/UseScriptFields';
import IconEdit from '@genesis-labs/web-core/core/components/ui/icons/IconEdit.vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

//  Props
/*const props = */withDefaults(defineProps<{
    /**  Affiche le bouton "Éditer" (défaut : false) */
    showEditButton?: boolean;
}>(), {
    showEditButton: false,
});

const emit = defineEmits<{
    'request-file-path': [payload: FileRequestPayload];
    edit: [];
}>();

//  Refs du script (v-model géré par useScriptFields)
const { scriptPath, scriptContent } = useScriptFields();

//  Action de reset (store)
const { resetScript } = useGenerator();

function handleBrowse() {
    emit('request-file-path', { field: 'script', extensions: ['sql'] });
}

function handleEdit() {
    emit('edit');
}

function handleClear() {
    resetScript();
}
</script>