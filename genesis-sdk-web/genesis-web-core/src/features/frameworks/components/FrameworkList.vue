<template>
  <!-- État vide global -->
  <div v-if="frameworks.length === 0" class="p-6 text-center text-muted bg-bg-light rounded-lg">
    Aucun framework disponible.
  </div>

  <!-- ═══ SECTION DÉDIÉE : FRAMEWORKS RÉCENTS ═══ -->
  <GenesisDisclosure
    v-if="recentFrameworks.length > 0"
    title="Récents"
    :default-open="true"
    class="mb-6"
  >
    <template #title>
      <span class="text-2xl font-semibold">Récemment</span>
      <span class="text-xs font-normal text-muted bg-bg-light px-2 py-0.5 rounded-full border border-secondary">
        {{ recentFrameworks.length }}
      </span>
    </template>

    <div class="section-body">
      <GenesisList :display="display" minColWidth="100px">
        <GenesisItem
          v-for="framework in recentFrameworks"
          :key="`recent-${framework.id}`"
          :logo-src="resolveFrameworkLogo(framework)"
          :selected="selectedId === framework.id"
          :badge="frameworkSlots?.get(framework.id) ?? null"
          @click="$emit('select', framework, $event)"
          @info="$emit('info', framework)"
          :feature="getLanguageName(framework.languageId)" 
          :size="'sm'"
        >
          <template #default>
            <td class="p-3 text-center">{{ framework.name }}</td>
          </template>
        </GenesisItem>
      </GenesisList>
    </div>
  </GenesisDisclosure>

  <!-- ═══ LISTE GROUPÉE GÉNÉRIQUE (TOUS LES FRAMEWORKS) ═══ -->
  <GenesisGroupedList
    v-if="frameworks.length > 0"
    :items="frameworks"
    :group-by="groupBy"
    :group-options="groupOptions"
    :group-labels="groupLabels"
    :display="display"
    minColWidth="200px"
    @update:groupBy="$emit('update:groupBy', $event)"
  >
    <template #default="{ items }">
        <GenesisItem
          v-for="framework in items"
          :key="framework.id"
          :label="framework.name"
          :logo-src="resolveFrameworkLogo(framework)"
          :selected="selectedId === framework.id"
          :badge="frameworkSlots?.get(framework.id) ?? null"
          :show-info-button="true"
          @click="$emit('select', framework, $event)"
          @info="$emit('info', framework)"
          :feature="getLanguageName(framework.languageId)" 
        >
          <template #default>
            <td class="p-3 text-center">{{ framework.name }}</td>
          </template>
        </GenesisItem>
    </template>
  </GenesisGroupedList>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Framework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';

import GenesisGroupedList from '@genesis-labs/web-core/core/components/layouts/display/GenesisGroupedList.vue';
import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';
import { resolveFrameworkLogo } from '../assets/frameworkLogos';
import { useRecency } from '@genesis-labs/web-core/core/features/recent/composables/useRecency';

// ============================================================================
// 1. PROPS
// ============================================================================
const props = defineProps<{
  frameworks: Framework[];
  languages?: { id: number; name: string }[]; // ✅ AJOUT : On reçoit la liste des langages
  selectedId?: number;
  display: DisplayMode;
  frameworkSlots?: Map<number, string>;
  
  groupBy?: keyof Framework | null;
  groupOptions?: { label: string; value: keyof Framework | null }[];
  groupLabels?: Record<string | number, string>;
}>();

// ============================================================================
// 2. EMITS
// ============================================================================
defineEmits<{
  select: [framework: Framework, event?: MouseEvent];
  info: [framework: Framework];
  'update:groupBy': [value: keyof Framework | null];
}>();

// ============================================================================
// 3. LOGIQUE DE RÉCENCE & UTILITAIRES
// ============================================================================
const { recentFrameworks: recentFrameworkItems } = useRecency();

const recentFrameworks = computed<Framework[]>(() => {
  return recentFrameworkItems.value
    .map(recent => props.frameworks.find(f => f.id === recent.id))
    .filter((f): f is Framework => f !== undefined);
});

// ✅ NOUVEAU : Fonction fiable pour récupérer le nom du langage
const getLanguageName = (languageId: number): string => {
  const lang = props.languages?.find(l => l.id === languageId);
  return lang ? lang.name : 'Inconnu';
};
</script>

<style scoped>
.section-body {
  padding-top: 1rem; 
}
</style>