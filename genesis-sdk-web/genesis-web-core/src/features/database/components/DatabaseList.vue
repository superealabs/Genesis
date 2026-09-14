<template>
    <GenesisList :display="display" minColWidth="200px">
        <GenesisItem
            v-for="engine in engines"
            :key="engine.id"
            :label="engine.name"
            :sublabel="`Port: ${engine.port}`"
            :selected="selectedId === engine.id"
            :badge="databaseSlots?.get(engine.id) ?? null" 
            @click="$emit('select', engine, $event)"
        >
        </GenesisItem>
    </GenesisList>
</template>

<script setup lang="ts">
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@/core/components/layouts/display/GenesisItem.vue';
import type { DatabaseEngineDto } from '@/features/database/types/database.types';

defineProps<{
    engines: DatabaseEngineDto[];
    selectedId?: number | null;
    display: 'grid' | 'list';
    databaseSlots?: Map<number, string>; 
}>();

defineEmits<{
    select: [engine: DatabaseEngineDto, event?: MouseEvent];
}>();
</script>