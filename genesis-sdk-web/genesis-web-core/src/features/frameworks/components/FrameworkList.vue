<template>
    <GenesisList :display="display" minColWidth="200px">
        <GenesisItem
            v-for="framework in frameworks"
            :key="framework.id"
            :label="framework.name"
            :sublabel="framework.coreFramework"
            :selected="selectedId === framework.id"
            :badge="frameworkSlots?.get(framework.id) ?? null"
            :show-info-button="true"
            @click="$emit('select', framework, $event)"
            @info="$emit('info', framework)"
        >
            <!-- <template #complementary>
                <span
                    v-if="framework.isProd"
                    class="px-2 py-0.5 bg-green-500/10 text-green-500 text-xs rounded-full font-medium whitespace-nowrap"
                >
                    Prod Ready
                </span>
                <span class="px-2 py-0.5 bg-secondary/20 text-text text-xs rounded-full whitespace-nowrap">
                    {{ framework.type }}
                </span>
            </template> -->

            <template #default>
                <td class="p-3 text-center">{{ framework.name }}</td>
                <td class="p-3 text-center">{{ framework.coreFramework }}</td>
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