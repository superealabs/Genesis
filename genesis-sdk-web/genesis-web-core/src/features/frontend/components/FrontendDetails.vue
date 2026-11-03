<!-- genesis-sdk-web/genesis-web-core/src/features/frontend/components/FrontendDetails.vue -->
<template>
    <GenesisDetails :title="framework.name">
        <template #logo>
            <GenesisLogo :src="resolveFrontendLogo(framework) ?? ''" :size="'xl'" />
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

            <!-- ═══ GROUPE 2 : STACK TECHNIQUE ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">
                    Stack technique
                </h3>

                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in stackRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
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
import type { FrontendFramework } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';
import GenesisLogo from '@genesis-labs/web-core/core/components/ui/logo/GenesisLogo.vue';
import { resolveFrontendLogo } from '../assets/frontendLogo'; // 🔧 À adapter au chemin réel
import { useFrontendStore } from '../manifest';

const props = defineProps<{ framework: FrontendFramework }>();
const store = useFrontendStore();

const orFallback = (value: string | number | null | undefined, fallback = 'Non spécifié'): string =>
    value !== null && value !== undefined && value !== '' ? String(value) : fallback;

// Nom du langage résolu via le store (au lieu de l'ID brut)
const languageName = computed(() =>
    store.getAvailableProgrammingLanguages?.find(l => l.id === props.framework.languageId)?.name
    ?? String(props.framework.languageId)
); // 🔧 À adapter au getter réel

// Groupe 1 : ce qui identifie le framework
const identityRows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Core Framework', value: orFallback(f.coreFramework) },
        { label: 'Langage',        value: languageName.value },
    ];
});

// Groupe 2 : moteur / extension / langage côté template
const stackRows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Moteur de Template',   value: orFallback(f.viewTemplateEngine, 'Non applicable') },
        { label: 'Extension composants', value: orFallback(f.componentExtension) },
    ];
});

// Groupe 3 : paramètres réseau
const connectionRows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Port par défaut', value: orFallback(f.defaultPort) },
    ];
});
</script>