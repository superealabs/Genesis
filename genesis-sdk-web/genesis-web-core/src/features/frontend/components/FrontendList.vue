<template>
  <!-- État vide global -->
  <div v-if="frontends.length === 0" class="p-6 text-center text-muted bg-bg-light rounded-lg">
    Aucun framework frontend disponible.
  </div>

  <!-- ═══ SECTION DÉDIÉE : FRAMEWORKS FRONTEND RÉCENTS ═══ -->
  <GenesisDisclosure
    v-if="recentFrontends.length > 0"
    title="Récents"
    :default-open="true"
    variant="secondary"
    class="mb-6"
  >
    <template #title>
      <span class="text-2xl font-semibold">Récemment</span>
      <span class="text-xs font-normal text-muted bg-bg-light px-2 py-0.5 rounded-full border border-secondary">
        {{ recentFrontends.length }}
      </span>
    </template>

    <div class="section-body">
      <GenesisList :display="display" minColWidth="100px">
        <GenesisItem
          v-for="fw in recentFrontends"
          :key="`recent-${fw.id}`"
          :logo-src="resolveFrontendLogo(fw)"
          :selected="selectedId === fw.id"
          :badge="frameworkSlots?.get(fw.id) ?? null"
          :size="'sm'"
          @click="$emit('select', fw, $event)"
          @info="$emit('info', fw)"
        >
          <template #default>
            <td class="p-3 text-center">{{ fw.name }}</td>
          </template>
        </GenesisItem>
      </GenesisList>
    </div>
  </GenesisDisclosure>

  <!-- ═══ LISTE GROUPÉE GÉNÉRIQUE (TOUS LES FRAMEWORKS FRONTEND) ═══ -->
  <GenesisGroupedList
    v-if="frontends.length > 0"
    :items="frontends"
    :group-by="groupBy"
    :group-options="groupOptions"
    :display="display"
    minColWidth="200px"
    @update:group-by="$emit('update:groupBy', $event)"
  >
    <template #default="{ items }">
      <GenesisItem
        v-for="fw in items"
        :key="fw.id"
        :label="fw.name"
        :logo-src="resolveFrontendLogo(fw)"
        :selected="selectedId === fw.id"
        :show-info-button="true"
        :badge="frameworkSlots?.get(fw.id) ?? null"
        @click="$emit('select', fw, $event)"
        @info="$emit('info', fw)"
      >
        <template #default>
          <td class="p-3 text-center">{{ fw.name }}</td>
        </template>
      </GenesisItem>
    </template>
  </GenesisGroupedList>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';

import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisGroupedList from '@genesis-labs/web-core/core/components/layouts/display/GenesisGroupedList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import { resolveFrontendLogo } from '../assets/frontendLogo';

// ✅ Import du composable de récence
import { useRecency } from '@genesis-labs/web-core/core/features/recent/composables/useRecency';

// ============================================================================
// 1. PROPS
// ============================================================================
const props = defineProps<{
  frontends: FrontendFramework[];
  selectedId?: number;
  display: DisplayMode;
  frameworkSlots?: Map<number, string>;
  groupBy?: keyof FrontendFramework | null;
  groupOptions?: { label: string; value: keyof FrontendFramework | null }[];
}>();

// ============================================================================
// 2. EMITS
// ============================================================================
defineEmits<{
  select: [framework: FrontendFramework, event?: MouseEvent];
  info: [framework: FrontendFramework];
  'update:groupBy': [value: keyof FrontendFramework | null];
}>();

// ============================================================================
// 3. LOGIQUE DE RÉCENCE
// ============================================================================
const { recentFrontends: recentFrontendItems } = useRecency();

/**
 * Mappe les IDs des frameworks frontend récents vers les objets complets 
 * présents dans la prop `frontends`, en conservant l'ordre de récence.
 */
const recentFrontends = computed<FrontendFramework[]>(() => {
  return recentFrontendItems.value
    .map(recent => props.frontends.find(f => f.id === recent.id))
    .filter((f): f is FrontendFramework => f !== undefined); // Sécurité
});
</script>

<style scoped>
.section-body {
  padding-top: 1rem; 
}
</style>