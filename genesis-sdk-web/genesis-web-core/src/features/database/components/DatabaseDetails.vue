<!-- genesis-sdk-web/genesis-web-core/src/features/database/components/DatabaseDetails.vue -->
<template>
    <GenesisDetails :title="engine.name">
        <template #logo>
            <GenesisLogo :src="resolveDatabaseLogo(engine) ?? ''" :size="'xl'" />
        </template>

        <!-- La responsivité dépend de la largeur de ce conteneur, pas de l'écran -->
        <div class="flex flex-col gap-y-12 px-4 @md:px-12 @container">

            <!-- ═══ GROUPE 1 : IDENTIFICATION ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Identification
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in identityRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

            <!-- ═══ GROUPE 2 : DRIVER ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Driver
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in driverRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

            <!-- ═══ GROUPE 3 : CONNEXION ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Connexion
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in connectionRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

        </div>
    </GenesisDetails>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';
import GenesisLogo from '@genesis-labs/web-core/core/components/ui/logo/GenesisLogo.vue';
import { resolveDatabaseLogo } from '../assets/databaseLogos';

const props = defineProps<{ engine: DatabaseEngineDto }>();

const orFallback = (value: string | number | null | undefined, fallback = 'Non spécifié'): string =>
    value !== null && value !== undefined && value !== '' ? String(value) : fallback;

// Groupe 1 : ce qui identifie le SGBD
const identityRows = computed(() => {
    const e = props.engine;
    return [
        { label: 'Nom du SGBD',     value: orFallback(e.name) },
        { label: 'Version majeure', value: orFallback(e.databaseMajorVersion) },
    ];
});

// Groupe 2 : informations sur le driver JDBC
const driverRows = computed(() => {
    const e = props.engine;
    return [
        { label: 'Classe du Driver',  value: orFallback(e.driver) },
        { label: 'Nom du Driver',     value: orFallback(e.driverName) },
        { label: 'Type de Driver',    value: orFallback(e.driverType) },
        { label: 'Version du Driver', value: orFallback(e.driverVersion) },
    ];
});

// Groupe 3 : paramètres réseau / spécifiques
const connectionRows = computed(() => {
    const e = props.engine;
    return [
        { label: 'Port par défaut', value: orFallback(e.port) },
        { label: 'SID',             value: orFallback(e.sid, 'Non applicable') },
    ];
});
</script>