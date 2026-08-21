<template>
    <GenesisList :display="display" minColWidth="200px">
        <GenesisItem
            v-for="framework in frameworks"
            :key="framework.id"
            :label="framework.name"
            :sublabel="framework.coreFramework"
            :layoutMode="display === 'grid' ? 'card' : 'list'"
            :selected="selectedId === framework.id"
            :slot="frameworkSlots?.get(framework.id) ?? null"
            :showInfoButton="true"
            @click="$emit('select', framework)"
            @info="$emit('info', framework)"
        >
            <!-- Logo custom (optionnel, pour futur SVG) -->
            <!-- <template #logo>...</template> -->

            <!-- Informations complémentaires (uniquement en mode list) -->
            <template v-if="display === 'list'" #complementary>
                <span
                    v-if="framework.isProd"
                    class="px-2 py-0.5 bg-green-500/10 text-green-500 text-xs rounded-full font-medium whitespace-nowrap"
                >
                    Prod Ready
                </span>
                <span class="px-2 py-0.5 bg-secondary/20 text-text text-xs rounded-full whitespace-nowrap">
                    {{ framework.type }}
                </span>
                <!-- <span class="px-2 py-0.5 bg-accent/10 text-accent text-xs rounded-full whitespace-nowrap">
                    {{ framework.language }}
                </span> -->
            </template>
        </GenesisItem>
    </GenesisList>
</template>

<script setup lang="ts">
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@/core/components/layouts/display/GenesisItem.vue';
import type { Framework } from '../types/framework.types';

defineProps<{
    /** Liste des frameworks à afficher */
    frameworks: Framework[];
    /** ID du framework actuellement sélectionné (mode sélection) */
    selectedId?: number;
    /** Mode d'affichage : grid (cartes) ou list (lignes) */
    display: 'grid' | 'list';
    /**
     * Map des framework.id → slot de comparaison (A, B, C, D)
     * Utilisé pour afficher les badges sur les cartes en mode comparaison
     */
    frameworkSlots?: Map<number, string>;
}>();

defineEmits<{
    select: [framework: Framework];
    info: [framework: Framework];
}>();
</script>