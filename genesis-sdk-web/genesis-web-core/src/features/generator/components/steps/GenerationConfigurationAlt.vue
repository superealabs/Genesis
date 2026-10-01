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
        
      <!-- Dans le template, remplace la section GenesisConfigurationSelector par ceci : -->
      <GenesisConfigurationSelector
        :configurations="configManager.configurations.value"
        :selected-config-id="configManager.selectedConfigId.value"
        :filtered-configs="configManager.filteredConfigs.value"
        :search-query="configManager.searchQuery.value"
        :can-move-up="configManager.canMoveUp.value"
        :can-move-down="configManager.canMoveDown.value"
        :selected-items="selectedItemsInList" 
        @update:search-query="(val) => configManager.searchQuery.value = val"
        @add="handleAddConfig" 
        @delete="handleDeleteConfig" 
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

import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';
import { GenesisConfig } from '@genesis-labs/shared-types';
import { GeneratorGenerationOptionsPayload } from '@genesis-labs/shared-types';


// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const configManager = useConfigurationManager([], { singleConfiguration: false });
const localPersistedConfigs = ref<GenesisConfig[]>([]);


const { stepperData, toggleTable, toggleView, tables, views, fetchTablesMetadata } = useGenerator();

const isLoading = ref(false);
const tableSearchQuery = ref('');

const { 
  loadGenerationOptionsConfigurations, 
  saveConfiguration, 
  deleteConfiguration,
} = useConfig(); // ✅ AJOUTÉ

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

/**
 * Helper : Sauvegarde l'état actuel d'une configuration spécifique dans le backend.
 */
/**
 * Helper : Sauvegarde l'état actuel d'une configuration spécifique dans le backend.
 */
async function persistConfig(id: string | number) {
  try {
    const uiConfig = configManager.configurations.value.find(c => c.id === id);
    const persistedConfig = localPersistedConfigs.value.find(c => String(c.id) === String(id));

    if (uiConfig && persistedConfig) {
      const updatedConfig: GenesisConfig<GeneratorGenerationOptionsPayload> = {
        ...persistedConfig,
        name: uiConfig.name,
        payload: {
          components: [...uiConfig.components]
        }
      };
    
      await saveConfiguration(updatedConfig);
      
      const index = localPersistedConfigs.value.findIndex(c => String(c.id) === String(id));
      if (index !== -1) {
        localPersistedConfigs.value[index] = updatedConfig;
      }
    }
  } catch (error) {
    console.error(`[persistConfig] Erreur critique lors de la sauvegarde de la configuration ${id}:`, error);
  }
}

async function handleAddConfig() {
  const defaultComponents = ['model', 'dao', 'service', 'controller'];
  const newUiConfig = configManager.addConfiguration(defaultComponents);
  
  const newGenesisConfig: GenesisConfig<GeneratorGenerationOptionsPayload> = {
    id: String(newUiConfig.id),
    name: newUiConfig.name,
    configType: 'generator_generation-options',
    schemaVersion: '1.0.0',
    createdAt: new Date().toISOString(),
    payload: {
      components: [...defaultComponents]
    }
  };
  
  await saveConfiguration(newGenesisConfig);
}

function handleRename(id: string | number, newName: string) {
  configManager.renameConfiguration(id, newName);
  persistConfig(id); // ✅ Sauvegarde immédiate
}

function handleAssign(configId: string | number, items: string[]) {
  configManager.assignToConfig(configId, items);
  persistConfig(configId); // ✅ Sauvegarde immédiate
}

function handleRemove(configId: string | number, items: string[]) {
  configManager.removeFromConfig(configId, items);
  persistConfig(configId); // ✅ Sauvegarde immédiate
}

async function handleDeleteConfig(id: string | number) {
  configManager.deleteConfiguration(id);
  await deleteConfiguration(String(id)); // ✅ Suppression backend
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
    return;
  }
  
  configManager.editConfiguration(activeConfig.value.id, currentComponents);
  persistConfig(activeConfig.value.id); // ✅ Sauvegarde immédiate
}

// ... (isItemSelected, toggleItem, toggleAllTables, getAssignedConfig restent inchangés)
const isItemSelected = (item: TableMetadataDto): boolean => {
  return item.isView 
    ? tableSelection.value.selectedViews.includes(item.tableName)
    : tableSelection.value.selectedTables.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto): void => {
  item.isView ? toggleView(item.tableName) : toggleTable(item.tableName);
};

const toggleAllTables = (): void => {
  if (areAllTablesSelected.value) {
    tableSelection.value.selectedTables = [];
  } else {
    tableSelection.value.selectedTables = tables.value.map(t => t.tableName);
  }
};

function getAssignedConfig(item: TableMetadataDto): string {
  const assignedConfigs = configManager.configurations.value
    .filter(c => c.components.includes(item.tableName))
    .map(c => c.name);
  return assignedConfigs.length > 0 ? assignedConfigs[0] : 'Non assigné';
}





// ============================================================================
// 4. LIFECYCLE
// ============================================================================

async function syncGenerationConfigs() {
  const data = await loadGenerationOptionsConfigurations();
  
  localPersistedConfigs.value = data;
  
  // On mappe pour l'UI
  configManager.configurations.value = data.map(c => {
    const p = c.payload as unknown as GeneratorGenerationOptionsPayload;
    return {
      id: c.id,
      name: c.name,
      isHidden: false,
      components: p.components || []
    };
  });
  
  configManager.selectConfiguration(configManager.configurations.value.length > 0 ? configManager.configurations.value[0].id : null);
}

onMounted(async () => {
  isLoading.value = true;
  try {
    // Chargement en parallèle des tables et des configs de génération
    await Promise.all([
      fetchTablesMetadata(),
      syncGenerationConfigs()
    ]);
  } catch (error) {
    console.error('[GenerationConfigurationAlt] Erreur lors de l\'initialisation:', error);
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