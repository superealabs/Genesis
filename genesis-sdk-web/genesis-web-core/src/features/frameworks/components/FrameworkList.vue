<template>
  <!-- État vide -->
  <div v-if="frameworks.length === 0" class="p-6 text-center text-muted bg-bg-light rounded-lg">
    Aucun framework disponible.
  </div>

  <!-- Liste groupée générique -->
  <GenesisGroupedList
    v-else
    :items="frameworks"
    :group-by="groupBy"
    :group-options="groupOptions"
    :group-labels="groupLabels"
    :display="display"
    minColWidth="200px"
    @update:groupBy="$emit('update:groupBy', $event)"
  >
    <!-- 
      Scoped slot : Reçoit les éléments filtrés pour la section actuelle.
      Note : La structure avec les balises <td> est conservée ici pour garantir 
      la compatibilité avec le mode d'affichage 'table' du composant GenesisList parent.
    -->
    <template #default="{ items }">
      <GenesisItem
        v-for="framework in items"
        :key="framework.id"
        :label="framework.name"
        :sublabel="framework.coreFramework"
        :selected="selectedId === framework.id"
        :badge="frameworkSlots?.get(framework.id) ?? null"
        :show-info-button="true"
        @click="$emit('select', framework, $event)"
        @info="$emit('info', framework)"
      >
        <template #default>
          <td class="p-3 text-center">{{ framework.name }}</td>
          <td class="p-3 text-center">{{ framework.coreFramework }}</td>
        </template>
      </GenesisItem>
    </template>
  </GenesisGroupedList>
</template>

<script setup lang="ts">
import type { Framework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

import GenesisGroupedList from '@genesis-labs/web-core/core/components/layouts/display/GenesisGroupedList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.vue';

// ============================================================================
// 1. PROPS
// ============================================================================
defineProps<{
  frameworks: Framework[];
  selectedId?: number;
  display: DisplayMode;
  frameworkSlots?: Map<number, string>;
  
  // Options de regroupement
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
</script>