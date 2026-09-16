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

            <template #default>
                <td class="p-3 text-center">{{ engine.name }}</td>
                <td class="p-3 text-center text-text-muted">{{ engine.driver }}</td>
            </template>
        </GenesisItem>
    </GenesisList>
</template>

<script setup lang="ts">
import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

defineProps<{
    engines: DatabaseEngineDto[];
    selectedId?: number | null;
    display: DisplayMode;
    databaseSlots?: Map<number, string>; 
}>();

defineEmits<{
    select: [engine: DatabaseEngineDto, event?: MouseEvent];
}>();
</script>