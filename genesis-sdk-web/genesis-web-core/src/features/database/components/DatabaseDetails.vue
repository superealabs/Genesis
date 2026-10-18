<!-- genesis-sdk-web/genesis-web-core/src/features/database/components/DatabaseDetails.vue -->
<template>
    <GenesisDetails :title="engine.name">
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
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';

const props = defineProps<{ engine: DatabaseEngineDto }>();

// Un seul tableau : même ordre dans toutes les colonnes, donc les lignes se comparent d'un coup d'œil
const rows = computed(() => {
    const e = props.engine;
    return [
        { label: 'Nom du SGBD',       value: e.name },
        { label: 'Classe du Driver',   value: e.driver },
        { label: 'Nom du Driver',      value: e.driverName },
        { label: 'Port par défaut',    value: e.port },
        { label: 'Type de Driver',     value: e.driverType ?? 'Non spécifié' },
        { label: 'Version du Driver',  value: e.driverVersion ?? 'Non spécifié' },
        { label: 'Version majeure',    value: e.databaseMajorVersion ? String(e.databaseMajorVersion) : 'Non spécifié' },
        { label: 'SID (Spécifique)',   value: e.sid ?? 'Non applicable' },
    ];
});
</script>