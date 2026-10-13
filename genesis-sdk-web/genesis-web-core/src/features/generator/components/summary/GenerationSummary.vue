<!-- genesis-sdk-web/genesis-web-core/src/features/generator/components/summary/TablesSummary.vue -->
<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : LISTE DES TABLES ET VUES -->
    <div class="space-y-3 flex-1 min-h-0 flex flex-col">
      <div class="flex items-center justify-between flex-shrink-0">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          Tables et Vues configurées
        </h3>
      </div>
      
      <p class="text-sm text-text-muted flex-shrink-0">
        Aperçu des entités de base de données et de leur profil d'assignation.
      </p>

      <!-- Filtres -->
      <div class="flex flex-row gap-2">
        <GenesisInput
          v-model="tableSearchQuery"
          type="text"
          placeholder="Rechercher une table ou une vue..."
          size="md"
          fill-width
        >
          <template #left>
            <IconSearch :size="16" class="text-text-muted" />
          </template>
        </GenesisInput>

        <GenesisSegmentedControl v-model="activeFilter" :options="filterOptions" size="md" />
      </div>

      <!-- Liste Redimensionnable -->
      <div class="rounded-md overflow-hidden min-h-[300px] h-[400px] flex flex-col border border-bg-neutral-genesis relative" :style="resizeStyle">
        <div class="flex-1 overflow-y-auto custom-scrollbar">
          <div 
            v-for="item in filteredCombinedItems" 
            :key="item.tableName"
            class="flex items-center justify-between p-3 border-b border-bg-neutral-genesis last:border-b-0 hover:bg-bg-secondary/50 transition-colors"
          >
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <div class="w-5 h-5 rounded flex items-center justify-center flex-shrink-0"
                   :class="item.isView ? 'bg-purple-500/10 text-purple-500' : 'bg-blue-500/10 text-blue-500'">
                <svg v-if="item.isView" class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
                <svg v-else class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4"/></svg>
              </div>
              <div class="flex flex-col min-w-0">
                <span class="text-sm font-medium text-text truncate">{{ item.tableName }}</span>
                <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
              </div>
            </div>
            
            <div class="flex-shrink-0 ml-4 text-right min-w-[120px]">
              <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-primary/10 text-primary">
                {{ getAssignedProfile(item.tableName, item.isView) }}
              </span>
            </div>
          </div>

          <div v-if="filteredCombinedItems.length === 0" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
            {{ combinedItems.length === 0 ? 'Aucune table ou vue trouvée.' : 'Aucun résultat pour cette recherche.' }}
          </div>
        </div>
        <GenesisResizeHandle @resize-start="startResizeBottom"/>
      </div>
    </div>

    <!-- PARTIE DROITE : LISTE DES PROFILS (READONLY) -->
    <section class="flex flex-col gap-4 w-80 flex-shrink-0">
      <div>
        <h3 class="text-lg font-semibold text-text mb-3 flex items-center gap-2">
          Profils de Génération
        </h3>
        
        <GenesisConfigurationPanel
          :configurations="summaryConfigs"
          :selected-config-id="null"
          :filtered-configs="summaryConfigs"
          :search-query="''"
          :can-move-up="false"
          :can-move-down="false"
          :readonly="true"
        />
      </div>
    </section>

  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { TableMetadataDto } from '@genesis-labs/shared-types';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import GenesisResizeHandle from '@genesis-labs/web-core/core/components/ui/actions/GenesisResizeHandle.vue';
import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable';

const { tables, views, stepperData } = useGenerator();

const tableSearchQuery = ref('');
const activeFilter = ref<'all' | 'table' | 'view'>('all');

const filterOptions = [
  { label: 'Tous', value: 'all' },
  { label: 'Tables', value: 'table' },
  { label: 'Vues', value: 'view' }
];

const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: 300,
    maxHeight: () => window.innerHeight * 0.8,
    resizableX: ref(false),
    resizableY: ref(true)
});

// Type local pour éviter la dépendance à ConfigurationItem
interface SummaryConfig {
  id: string | number;
  name: string;
  isHidden: boolean;
}

const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => [
  ...tables.value.map(t => ({ ...t, type: 'table' as const })),
  ...views.value.map(v => ({ ...v, type: 'view' as const }))
]);

const filteredCombinedItems = computed(() => {
  let items = combinedItems.value;
  if (activeFilter.value === 'table') items = items.filter(item => !item.isView);
  else if (activeFilter.value === 'view') items = items.filter(item => item.isView);
  
  if (tableSearchQuery.value.trim()) {
    const query = tableSearchQuery.value.toLowerCase();
    items = items.filter(item => item.tableName.toLowerCase().includes(query));
  }
  return items;
});

const getAssignedProfile = (tableName: string, isView: boolean): string => {
  const param = stepperData.value.generationParameters.find(p => 
    isView ? p.selectedViews.includes(tableName) : p.selectedTables.includes(tableName)
  );
  return param ? param.profileName : 'Non assigné';
};

// ✅ Utilisation du type local SummaryConfig
const summaryConfigs = computed<SummaryConfig[]>(() => 
  stepperData.value.generationParameters.map((p, index) => ({
    id: index,
    name: p.profileName,
    isHidden: false
  }))
);
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>