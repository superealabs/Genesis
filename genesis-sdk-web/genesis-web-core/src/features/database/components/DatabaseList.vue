<template>
  <!-- État vide global -->
  <div v-if="engines.length === 0" class="p-6 text-center text-muted bg-bg-light rounded-lg">
    Aucun moteur de base de données disponible.
  </div>

  <!-- ═══ SECTION DÉDIÉE : BASES DE DONNÉES RÉCENTES ═══ -->
  <GenesisDisclosure
    v-if="recentDatabases.length > 0"
    title="Récents"
    :default-open="true"
    variant="secondary"
    class="mb-6"
  >
    <template #title>
      <span class="text-2xl font-semibold">Récemment</span>
      <span class="text-xs font-normal text-muted bg-bg-light px-2 py-0.5 rounded-full border border-secondary">
        {{ recentDatabases.length }}
      </span>
    </template>

    <div class="section-body">
      <GenesisList :display="display" minColWidth="100px">
        <GenesisItem
          v-for="engine in recentDatabases"
          :key="`recent-${engine.id}`"
          :logo-src="resolveDatabaseLogo(engine)"
          :selected="selectedId === engine.id"
          :badge="databaseSlots?.get(engine.id) ?? null"
          :size="'sm'"
          @click="$emit('select', engine, $event)"
          @info="$emit('info', engine)"
        >
          <template #default>
            <td class="p-3 text-center">{{ engine.name }}</td>
          </template>
        </GenesisItem>
      </GenesisList>
    </div>
  </GenesisDisclosure>

  <!-- ═══ LISTE GROUPÉE GÉNÉRIQUE (TOUS LES MOTEURS) ═══ -->
  <GenesisGroupedList
    v-if="engines.length > 0"
    :items="engines"
    :group-by="groupBy"
    :group-options="groupOptions"
    :display="display"
    minColWidth="200px"
    @update:group-by="$emit('update:groupBy', $event)"
  >
    <template #default="{ items }">
      <GenesisItem
        v-for="engine in items"
        :key="engine.id"
        :label="engine.name"
        :logo-src="resolveDatabaseLogo(engine)"
        :selected="selectedId === engine.id"
        :show-info-button="true"
        :badge="databaseSlots?.get(engine.id) ?? null"
        @click="$emit('select', engine, $event)"
        @info="$emit('info', engine)"
      >
        <template #default>
          <td class="p-3 text-center">{{ engine.name }}</td>
          <td class="p-3 text-center text-text-muted">{{ engine.driver }}</td>
        </template>
      </GenesisItem>
    </template>
  </GenesisGroupedList>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';

import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisGroupedList from '@genesis-labs/web-core/core/components/layouts/display/GenesisGroupedList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import { resolveDatabaseLogo } from '../assets/databaseLogos';

// ✅ Import du composable de récence
import { useRecency } from '@genesis-labs/web-core/core/features/recent/composables/useRecency';

// ============================================================================
// 1. PROPS
// ============================================================================
const props = defineProps<{
  engines: DatabaseEngineDto[];
  selectedId?: number | null;
  display: DisplayMode;
  databaseSlots?: Map<number, string>;
  groupBy?: keyof DatabaseEngineDto | null;
  groupOptions?: { label: string; value: keyof DatabaseEngineDto | null }[];
}>();

// ============================================================================
// 2. EMITS
// ============================================================================
defineEmits<{
  select: [engine: DatabaseEngineDto, event?: MouseEvent];
  info: [engine: DatabaseEngineDto];
  'update:groupBy': [value: keyof DatabaseEngineDto | null];
}>();

// ============================================================================
// 3. LOGIQUE DE RÉCENCE
// ============================================================================
const { recentDatabases: recentDatabaseItems } = useRecency();

/**
 * Mappe les IDs des bases de données récentes vers les objets complets 
 * présents dans la prop `engines`, en conservant l'ordre de récence.
 */
const recentDatabases = computed<DatabaseEngineDto[]>(() => {
  return recentDatabaseItems.value
    .map(recent => props.engines.find(e => e.id === recent.id))
    .filter((e): e is DatabaseEngineDto => e !== undefined); // Sécurité
});
</script>

<style scoped>
.section-body {
  padding-top: 1rem; 
}
</style>