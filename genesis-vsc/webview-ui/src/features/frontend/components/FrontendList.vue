<template>
    <GenesisList :display="display" minColWidth="200px">
        <GenesisItem
            v-for="fw in frontends"
            :key="fw.id"
            :label="fw.name"
            :sublabel="fw.coreFramework"
            :selected="selectedId === fw.id"
            :show-info-button="true"
            @click="$emit('select', fw, $event)"
            @info="$emit('info', fw)"
        >
            <!-- Affichage des métadonnées utiles pour le choix
            <template #complementary>
                <span class="px-2 py-0.5 bg-blue-500/10 text-blue-500 text-xs rounded-full font-medium whitespace-nowrap border border-blue-500/20">
                    {{ fw.componentExtension }}
                </span>
                <span class="px-2 py-0.5 bg-secondary/20 text-text text-xs rounded-full whitespace-nowrap border border-secondary">
                    Port: {{ fw.defaultPort }}
                </span>
            </template> -->
        </GenesisItem>
    </GenesisList>
</template>

<script setup lang="ts">
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@/core/components/layouts/display/GenesisItem.vue';
import type { FrontendFramework } from '../types/frontend.types';

defineProps<{
    frontends: FrontendFramework[];
    selectedId?: number;
    display: 'grid' | 'list';
}>();

defineEmits<{
    select: [framework: FrontendFramework, event?: MouseEvent];
    info: [framework: FrontendFramework];
}>();
</script>