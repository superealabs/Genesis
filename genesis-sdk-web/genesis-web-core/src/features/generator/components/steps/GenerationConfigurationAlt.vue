<template>
  <div class="flex gap-6 p-4 max-w-5xl mx-auto h-full">

    <!-- 1. PANNEAU GAUCHE : TABLES ET VUES -->
    <div class="space-y-3 flex-1 min-h-0 flex flex-col">
      <div class="flex items-center justify-between flex-shrink-0">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          Tables et Vues à inclure
          <span class="text-accent text-sm font-normal">*</span>
        </h3>
        <button 
          v-if="tables.length > 0"
          type="button"
          class="text-xs text-accent hover:text-accent/80 font-medium transition-colors"
          @click="toggleAllTables"
        >
          {{ areAllTablesSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
        </button>
      </div>
      
      <p class="text-sm text-text-muted flex-shrink-0">
        Choisissez les entités de votre base de données à inclure.
      </p>

      <div class="relative flex-shrink-0">
        <GenesisInput
          v-model="tableSearchQuery"
          type="text"
          placeholder="Rechercher une table ou une vue..."
          size="sm"
          fill-width
        >
          <template #left>
            <IconSearch :size="16" class="text-text-muted" />
          </template>
        </GenesisInput>
      </div>

      <div class="border border-secondary rounded-md overflow-hidden bg-bg-light flex-1 min-h-0 flex flex-col">
        <!-- État de chargement -->
        <div v-if="isLoading" class="p-6 text-center text-text-muted text-sm flex items-center justify-center gap-2">
          <svg class="animate-spin h-4 w-4 text-text-muted" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
          <span>Chargement des métadonnées...</span>
        </div>

        <!-- Liste des éléments -->
        <template v-else>
          <div class="flex-1 overflow-y-auto custom-scrollbar">
            <div 
              v-for="item in filteredCombinedItems" 
              :key="item.tableName"
              class="flex items-center justify-between p-3 border-b border-secondary last:border-b-0 hover:bg-secondary/30 transition-colors cursor-pointer"
              @click="toggleItem(item)"
            >
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <GenesisCheckboxSimple :model-value="isItemSelected(item)" />
                <div class="flex flex-col min-w-0">
                  <span class="text-sm font-medium text-text truncate">{{ item.tableName }}</span>
                  <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
                </div>
              </div>
              
              <div 
                class="flex-shrink-0 text-xs text-text-muted ml-4 text-right min-w-[120px] truncate" 
                :title="getAssignedConfig(item)"
              >
                {{ getAssignedConfig(item) }}
              </div>
            </div>

            <!-- État vide ou aucun résultat -->
            <div v-if="filteredCombinedItems.length === 0" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
              {{ combinedItems.length === 0 ? 'Aucune table ou vue trouvée.' : 'Aucun résultat pour cette recherche.' }}
            </div>
          </div>
        </template>
      </div>
    </div>

    <!-- 2. PANNEAU DROIT : GESTION DES CONFIGURATIONS -->
    <section class="flex flex-col gap-4 w-80 flex-shrink-0">
      <div>
        <h3 class="text-lg font-semibold text-text mb-3 flex items-center gap-2">
          Profils de Génération
          <span class="text-accent text-sm font-normal">*</span>
        </h3>
        
        <GenesisConfigurationSelector
          :configurations="configManager.configurations.value"
          :selected-config-id="configManager.selectedConfigId.value"
          :filtered-configs="configManager.filteredConfigs.value"
          :search-query="configManager.searchQuery.value"
          :can-move-up="configManager.canMoveUp.value"
          :can-move-down="configManager.canMoveDown.value"
          :selected-items="selectedItemsInList" 
          @update:search-query="(val) => configManager.searchQuery.value = val"
          @add="() => configManager.addConfiguration(['model'])"
          @delete="configManager.deleteConfiguration"
          @rename="handleRename"
          @toggle-visibility="configManager.toggleVisibility"
          @move-up="configManager.moveUp"
          @move-down="configManager.moveDown"
          @select-configuration="configManager.selectConfiguration"
          @assign="handleAssign"
          @remove="handleRemove"
        />
      </div>

      <div class="border-t border-secondary"></div>

      <!-- 3. PANNEAU DROIT : COMPOSANTS DE LA CONFIGURATION ACTIVE -->
      <div v-if="activeConfig" class="p-4 bg-bg-light/50 rounded-lg border border-secondary space-y-3">
        <div class="flex items-center justify-between">
          <h4 class="text-sm font-semibold text-text">
            Composants pour : <span class="text-accent">{{ activeConfig.name }}</span>
          </h4>
        </div>
        
        <div class="flex flex-col gap-2">
          <GenesisCheckboxSimple
            v-for="comp in AVAILABLE_COMPONENTS"
            :key="comp.value"
            :model-value="activeConfig.components.includes(comp.value)"
            :label="comp.label"
            size="md"
            @update:model-value="(isChecked) => handleComponentToggle(comp.value, isChecked)"
          />
        </div>
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

import type { ComponentType, TableMetadataDto } from '@genesis-labs/shared-types';
import { AVAILABLE_COMPONENTS } from '@genesis-labs/shared-types';

import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';

// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const configManager = useConfigurationManager([
  { id: 1, name: 'Configuration par défaut', isHidden: false, components: ['model', 'dao', 'service', 'controller'] },
  { id: 2, name: 'Modèles uniquement', isHidden: false, components: ['model'] },
]);

const { stepperData, toggleTable, toggleView, tables, views, fetchTablesMetadata } = useGenerator();

const isLoading = ref(false);
const tableSearchQuery = ref('');

// ============================================================================
// 2. COMPUTEDS (Données dérivées)
// ============================================================================
const tableSelection = computed(() => stepperData.value.tableSelection);

const activeConfig = computed(() => 
  configManager.configurations.value.find(c => c.id === configManager.selectedConfigId.value)
);

const selectedItemsInList = computed(() => [
  ...tableSelection.value.selectedTables,
  ...tableSelection.value.selectedViews
]);

const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => [
  ...tables.value.map(t => ({ ...t, type: 'table' as const })),
  ...views.value.map(v => ({ ...v, type: 'view' as const }))
]);

const filteredCombinedItems = computed(() => {
  if (!tableSearchQuery.value.trim()) return combinedItems.value;
  const query = tableSearchQuery.value.toLowerCase();
  return combinedItems.value.filter(item => item.tableName.toLowerCase().includes(query));
});

const areAllTablesSelected = computed(() => {
  if (tables.value.length === 0) return false;
  return tables.value.every(t => tableSelection.value.selectedTables.includes(t.tableName));
});

// ============================================================================
// 3. ACTIONS
// ============================================================================

// --- Actions : Gestion des Configurations ---

function handleRename(id: string | number, newName: string) {
  configManager.renameConfiguration(id, newName);
}

function handleAssign(configId: string | number, items: string[]) {
  configManager.assignToConfig(configId, items);
}

function handleRemove(configId: string | number, items: string[]) {
  configManager.removeFromConfig(configId, items);
}

/**
 * Retourne le nom de la configuration à laquelle la table/vue est assignée.
 * Note : Cette logique suppose que les noms des tables sont stockés dans le tableau 
 * 'components' de la configuration. Si la structure de useConfigurationManager évolue, 
 * cette fonction devra être adaptée en conséquence.
 */
function getAssignedConfig(item: TableMetadataDto): string {
  const assignedConfigs = configManager.configurations.value
    .filter(c => c.components.includes(item.tableName))
    .map(c => c.name);
  
  return assignedConfigs.length > 0 ? assignedConfigs[0] : 'Non assigné';
}

function handleComponentToggle(comp: ComponentType, isChecked: boolean) {
  if (!activeConfig.value) return;
  
  const currentComponents = [...activeConfig.value.components];
  const idx = currentComponents.indexOf(comp);
  
  if (isChecked && idx === -1) {
    currentComponents.push(comp);
  } else if (!isChecked && idx !== -1) {
    currentComponents.splice(idx, 1);
  } else {
    return; // Aucun changement nécessaire
  }
  
  configManager.editConfiguration(activeConfig.value.id, currentComponents);
}

// --- Actions : Gestion des Tables/Vues ---

const isItemSelected = (item: TableMetadataDto): boolean => {
  return item.isView 
    ? tableSelection.value.selectedViews.includes(item.tableName)
    : tableSelection.value.selectedTables.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto): void => {
  item.isView ? toggleView(item.tableName) : toggleTable(item.tableName);
};

/**
 * Bascule la sélection de toutes les tables.
 * La réaffectation directe du tableau réactif est préférée aux méthodes 
 * splice/push pour une meilleure lisibilité et des performances optimales.
 */
const toggleAllTables = (): void => {
  if (areAllTablesSelected.value) {
    tableSelection.value.selectedTables = [];
  } else {
    tableSelection.value.selectedTables = tables.value.map(t => t.tableName);
  }
};

// ============================================================================
// 4. LIFECYCLE
// ============================================================================

onMounted(async () => {
  isLoading.value = true;
  try {
    await fetchTablesMetadata();
  } catch (error) {
    console.error('[GenerationConfigurationAlt] Erreur lors du chargement des tables:', error);
  } finally {
    isLoading.value = false;
  }
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>