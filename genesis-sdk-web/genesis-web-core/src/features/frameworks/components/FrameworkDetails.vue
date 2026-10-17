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
import type { Framework } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';

const props = defineProps<{ framework: Framework }>();

const store = useFrameworkStore();

const yesNo = (value: boolean | undefined) => (value ? 'Oui' : 'Non');

// Un seul tableau : même ordre dans toutes les colonnes, donc les lignes se comparent d'un coup d'œil
const rows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Type',              value: f.type },
        { label: 'Langage',           value: store.languages.find(l => l.id === f.languageId)?.name ?? 'Inconnu' },
        { label: 'Core framework',    value: f.coreFramework },
        { label: 'Production',        value: yesNo(f.isProd) },
        { label: 'Base de données',   value: yesNo(f.useDB) },
        { label: 'Cloud',             value: yesNo(f.useCloud) },
        { label: 'Serveur Eureka',    value: yesNo(f.useEurekaServer) },
        { label: 'Passerelle',        value: yesNo(f.isGateway) },
        { label: 'Application front', value: yesNo(f.useFrontendApp) },
    ];
});
</script>