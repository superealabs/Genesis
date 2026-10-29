<template>
    <GenesisDetails :title="framework.name">
        <template #logo>
            <GenesisLogo :src="resolveFrameworkLogo(framework) ?? ''" :size="'xl'" />
        </template>
        
        <!-- ✅ AJOUT de @container : la responsivité dépend maintenant de la largeur de ce div, pas de l'écran -->
        <div class="flex flex-col gap-y-12 px-4 @md:px-12 @container">
            
            <!-- ═══ GROUPE 1 : INFORMATIONS GÉNÉRALES ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">Informations Générales</h3>
                
                <!-- ✅ Utilisation de @md: au lieu de md: -->
                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in generalRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
                        <dt class="text-xs font-semibold uppercase tracking-wider text-muted">{{ row.label }}</dt>
                        <dd class="text-sm font-medium text-text break-words">{{ row.value }}</dd>
                    </div>
                </dl>
            </section>

            <!-- ═══ GROUPE 2 : CAPACITÉS & INTÉGRATIONS ═══ -->
            <section class="flex flex-col gap-y-6">
                <h3 class="pb-3 text-xl font-semibold text-text border-b border-bg-neutral-genesis">Capacités & Intégrations</h3>
                
                <!-- ✅ Utilisation de @md: au lieu de md: -->
                <dl class="grid grid-cols-1 @md:grid-cols-2 gap-x-8 gap-y-5">
                    <div v-for="row in techRows" :key="row.label" class="flex flex-col gap-1 min-w-0">
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
import type { Framework } from '@genesis-labs/shared-types';
import GenesisDetails from '@genesis-labs/web-core/core/components/layouts/GenesisDetails.vue';
import { useFrameworkStore } from '@genesis-labs/web-core/features/frameworks/store/useFramework.store';
import { resolveFrameworkLogo } from '../assets/frameworkLogos';
import GenesisLogo from '@genesis-labs/web-core/core/components/ui/logo/GenesisLogo.vue';

const props = defineProps<{ framework: Framework }>();
const store = useFrameworkStore();

const yesNo = (value: boolean | undefined) => (value ? 'Oui' : 'Non');

// Groupe 1 : Identité du framework
const generalRows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Type', value: f.type },
        { label: 'Langage', value: store.languages.find(l => l.id === f.languageId)?.name ?? 'Inconnu' },
        { label: 'Core framework', value: f.coreFramework },
    ];
});

// Groupe 2 : Fonctionnalités techniques
const techRows = computed(() => {
    const f = props.framework;
    return [
        { label: 'Production', value: yesNo(f.isProd) },
        { label: 'Base de données', value: yesNo(f.useDB) },
        { label: 'Cloud', value: yesNo(f.useCloud) },
        { label: 'Serveur Eureka', value: yesNo(f.useEurekaServer) },
        { label: 'Passerelle', value: yesNo(f.isGateway) },
        { label: 'Application front', value: yesNo(f.useFrontendApp) },
    ];
});
</script>