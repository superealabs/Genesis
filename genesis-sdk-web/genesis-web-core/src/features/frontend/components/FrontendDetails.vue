<!-- genesis-sdk-web/genesis-web-core/src/features/frontend/components/FrontendDetails.vue -->
<template>
    <GenesisDetails :title="framework.name">
        <dl class="grid grid-cols-2 gap-x-8 gap-y-5">
            <div v-for="row in rows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
            </div>
        </dl>
    </GenesisDetails>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';

const props = defineProps<{ framework: FrontendFramework }>();

const rows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Core Framework',      value: f.coreFramework },
        { label: 'Moteur de Template',  value: f.viewTemplateEngine ?? 'Non applicable' },
        { label: 'Extension composants',value: f.componentExtension },
        { label: 'Port par défaut',     value: f.defaultPort },
        { label: 'ID du Langage',       value: String(f.languageId) } 
        // 💡 Astuce : Si tu veux afficher le nom du langage au lieu de l'ID, 
        // nous pourrons injecter le store des langages frontend ici, comme pour les Frameworks.
    ];
});
</script>