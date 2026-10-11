<template>
    <GenesisIdeCm
        v-model="scriptContent"
        class="h-full"
        language="sql"
        :filename="scriptFileName"
        :show-line-numbers="true"
        :is-dark="isDark"
    />
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GenesisIdeCm from '@genesis-labs/web-core/core/components/layouts/ide/GenesisIdeCm.vue';
import { useScriptFields } from '@genesis-labs/web-core/features/generator/composables/UseScriptFields';

// Le contenu vit dans le store : chaque panneau a son propre éditeur (curseur et défilement propres),
// mais tous affichent et modifient le même script.
const { scriptContent, scriptFileName } = useScriptFields();

const isDark = computed(() =>
    document.body.classList.contains('vscode-dark') ||
    document.body.classList.contains('genesis-dark')
);
</script>