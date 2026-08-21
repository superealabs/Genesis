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
            @click="$emit('select', framework, $event)"
            @info="$emit('info', framework)"
        >
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
            </template>
        </GenesisItem>
    </GenesisList>
</template>

<script setup lang="ts">
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@/core/components/layouts/display/GenesisItem.vue';
import type { Framework } from '../types/framework.types';

defineProps<{
    frameworks: Framework[];
    selectedId?: number;
    display: 'grid' | 'list';
    frameworkSlots?: Map<number, string>;
}>();

defineEmits<{
    select: [framework: Framework, event?: MouseEvent];
    info: [framework: Framework];
}>();
</script>