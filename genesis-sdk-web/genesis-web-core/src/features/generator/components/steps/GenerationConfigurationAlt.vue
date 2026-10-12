<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <div class="flex gap-6">
        <!-- 1. PANNEAU GAUCHE : TABLES ET VUES -->
        <div class="space-y-3 flex-1 min-h-0 flex flex-col">
          <div class="flex items-center justify-between flex-shrink-0">
            <h3 class="text-lg font-semibold text-text flex items-center gap-2">
              Tables et Vues à inclure
              <span class="text-primary text-sm font-normal">*</span>
            </h3>
            <button 
              v-if="tables.length > 0"
              type="button"
              class="text-xs text-primary hover:text-primary/80 font-medium transition-colors"
              @click="toggleAllVisibleItems"
            >
              {{ areAllVisibleSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
            </button>
          </div>
          
          <p class="text-sm text-text-muted flex-shrink-0">
            Choisissez les entités de votre base de données à inclure.
          </p>

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

          <div class="rounded-md overflow-hidden min-h-[300px] h-[400px] flex flex-col border border-bg-neutral-genesis relative" :style="resizeStyle">
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
                  class="flex items-center justify-between p-3 border-b border-bg-neutral-genesis last:border-b-0 hover:bg-bg-secondary hover:text-secondary transition-colors cursor-pointer"
                  @click="toggleItem(item)"
                >
                  <div class="flex items-center gap-3 flex-1 min-w-0">
                    <!-- ✅ MODIFIÉ : Utilise le nouveau computed du store -->
                    <GenesisCheckboxSimple :model-value="isItemSelected(item)" />
                    <div class="flex flex-col min-w-0">
                      <span class="text-sm font-medium truncate">{{ item.tableName }}</span>
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

            <GenesisResizeHandle @resize-start="startResizeBottom"/>
          </div>
        </div>

        <!-- 2. PANNEAU DROIT : GESTION DES CONFIGURATIONS -->
        <section class="flex flex-col gap-4 w-80 flex-shrink-0">
          <div>
            <h3 class="text-lg font-semibold text-text mb-3 flex items-center gap-2">
              Profils de Génération
              <span class="text-primary text-sm font-normal">*</span>
            </h3>
            
            <GenesisConfigurationSelector
              :configurations="configManager.configurations.value"
              :selected-config-id="configManager.selectedConfigId.value"
              :filtered-configs="configManager.filteredConfigs.value"
              :search-query="configManager.searchQuery.value"
              :can-move-up="configManager.canMoveUp.value"
              :can-move-down="configManager.canMoveDown.value"
              :selected-items="selectedItemsInListRef" 
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

          <!-- 3. PANNEAU DROIT : COMPOSANTS DE LA CONFIGURATION ACTIVE -->
          <div v-if="activeConfig" class="p-4 rounded-lg border border-neutral-light-genesis space-y-3">
            <div class="flex items-center justify-between">
              <h4 class="text-sm font-semibold text-text">
                Composants pour : <span class="text-primary">{{ activeConfig.name }}</span>
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
    </div>

    <!-- PARTIE DROITE : CARROUSEL TUTORIEL (Fixe) -->
    <div class="w-2/5 min-w-[300px] h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import type { ComponentType, TableMetadataDto, GenesisConfig, GeneratorGenerationOptionsPayload, GenerationAssociatedToConfig } from '@genesis-labs/shared-types';
import { AVAILABLE_COMPONENTS } from '@genesis-labs/shared-types';

import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { useConfig } from '@genesis-labs/web-core/core/features/config/composables/useConfig';

import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable';
import GenesisResizeHandle from '@genesis-labs/web-core/core/components/ui/actions/GenesisResizeHandle.vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import { useGeneratorStore } from '../../manifest'; // ✅ AJOUTÉ pour accéder aux nouvelles actions

// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const configManager = useConfigurationManager([], { singleConfiguration: true });
const localPersistedConfigs = ref<GenesisConfig[]>([]);
const store = useGeneratorStore(); // ✅ AJOUTÉ

const { tables, views, fetchTablesMetadata } = useGenerator();

const isLoading = ref(false);
const tableSearchQuery = ref('');

const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: 300,
    maxHeight: () => window.innerHeight * 0.8,
    resizableX: ref(false),
    resizableY: ref(true)
});

const { 
  loadGenerationOptionsConfigurations, 
  saveConfiguration, 
  deleteConfiguration,
} = useConfig();

// ============================================================================
// 2. CONSTANTES (CARROUSEL)
// ============================================================================
const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Sélection des tables' },
  { color: '#EF4444', label: 'Étape 2 : Profils de génération' },
  { color: '#10B981', label: 'Étape 3 : Composants' },
];

const filterOptions = [
  { label: 'Tous', value: 'all' },
  { label: 'Tables', value: 'table' },
  { label: 'Vues', value: 'view' }
];

const activeFilter = ref<'all' | 'table' | 'view'>('all');

// ============================================================================
// 3. COMPUTEDS (Données dérivées)
// ============================================================================

// ✅ MODIFIÉ : Utilise le tableau temporaire du store au lieu de l'ancien tableSelection
const selectedItemsInListRef = computed(() => store.selectedItemsInList);

const activeConfig = computed(() => 
  configManager.configurations.value.find(c => c.id === configManager.selectedConfigId.value)
);

const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => [
  ...tables.value.map(t => ({ ...t, type: 'table' as const })),
  ...views.value.map(v => ({ ...v, type: 'view' as const }))
]);

const filteredCombinedItems = computed(() => {
  let items = combinedItems.value;
  if (activeFilter.value === 'table') {
    items = items.filter(item => !item.isView);
  } else if (activeFilter.value === 'view') {
    items = items.filter(item => item.isView);
  }
  if (tableSearchQuery.value.trim()) {
    const query = tableSearchQuery.value.toLowerCase();
    items = items.filter(item => item.tableName.toLowerCase().includes(query));
  }
  return items;
});

const areAllVisibleSelected = computed(() => {
  if (filteredCombinedItems.value.length === 0) return false;
  // ✅ MODIFIÉ : Vérifie contre le tableau temporaire du store
  return filteredCombinedItems.value.every(item => store.selectedItemsInList.includes(item.tableName));
});

// ============================================================================
// 4. ACTIONS
// ============================================================================

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
  persistConfig(id);
}

// ✅ MODIFIÉ : Utilise les nouvelles actions du store pour lier les entités au profil
function handleAssign(configId: string | number, items: string[]) {
  items.forEach(tableName => {
    const item = combinedItems.value.find(i => i.tableName === tableName);
    if (item) {
      store.assignTableToConfig(configId, tableName, item.isView);
    }
  });
  store.clearSelectedItemsInList(); // Nettoie la sélection après assignation
  persistConfig(configId);
}

function handleRemove(configId: string | number, items: string[]) {
  items.forEach(tableName => {
    const item = combinedItems.value.find(i => i.tableName === tableName);
    if (item) {
      store.removeTableFromConfig(configId, tableName, item.isView);
    }
  });
  store.clearSelectedItemsInList();
  persistConfig(configId);
}

async function handleDeleteConfig(id: string | number) {
  configManager.deleteConfiguration(id);
  await deleteConfiguration(String(id));
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
  persistConfig(activeConfig.value.id);
}

// ✅ MODIFIÉ : Vérifie la présence dans le tableau temporaire du store
const isItemSelected = (item: TableMetadataDto): boolean => {
  return store.selectedItemsInList.includes(item.tableName);
};

// ✅ MODIFIÉ : Utilise l'action du store pour gérer la sélection visuelle
const toggleItem = (item: TableMetadataDto): void => {
  store.toggleItemInList(item.tableName);
};

// ✅ MODIFIÉ : Utilise les actions du store pour la sélection de masse
const toggleAllVisibleItems = (): void => {
  const allSelected = areAllVisibleSelected.value;
  
  filteredCombinedItems.value.forEach(item => {
    if (allSelected) {
      store.toggleItemInList(item.tableName); // Retire si déjà sélectionné
    } else {
      if (!store.selectedItemsInList.includes(item.tableName)) {
        store.selectedItemsInList.push(item.tableName);
      }
    }
  });
};

// ✅ MODIFIÉ : Interroge generationAssociations du store au lieu de config.components
function getAssignedConfig(item: TableMetadataDto): string {
  const assoc = store.generationAssociations.find((a: GenerationAssociatedToConfig) => 
    item.isView ? a.associatedViews.includes(item.tableName) : a.associatedTables.includes(item.tableName)
  );
  
  if (assoc) {
    const config = configManager.configurations.value.find(c => c.id === assoc.configId);
    return config ? config.name : 'Non assigné';
  }
  return 'Non assigné';
}

// ============================================================================
// 5. LIFECYCLE
// ============================================================================

const validComponentValues = AVAILABLE_COMPONENTS.map(c => c.value);

async function syncGenerationConfigs() {
  const data = await loadGenerationOptionsConfigurations();
  localPersistedConfigs.value = data;
  
  configManager.configurations.value = data.map(c => {
    const p = c.payload as unknown as GeneratorGenerationOptionsPayload;
    
    // CORRECTION CRUCIALE : On filtre pour ne garder QUE les ComponentType valides.
    // Cela nettoie les anciennes données corrompues où les noms de tables étaient 
    // accidentellement sauvegardés dans le tableau 'components'.
    const cleanComponents = (p.components || []).filter((comp: string) => 
      validComponentValues.includes(comp as ComponentType)
    );

    return {
      id: c.id,
      name: c.name,
      isHidden: false,
      components: cleanComponents // Maintenant garanti d'être uniquement ['model', 'dao', 'service', 'controller']
    };
  });
  
  configManager.selectConfiguration(configManager.configurations.value.length > 0 ? configManager.configurations.value[0].id : null);
}

onMounted(async () => {
  isLoading.value = true;
  try {
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

watch(
  () => configManager.configurations.value,
  (newConfigs) => {
    store.setGenerationProfiles(
      newConfigs.map(c => ({
        id: c.id,
        name: c.name,
        components: c.components as ComponentType[] // Contient déjà les ComponentType valides grâce à handleComponentToggle
      }))
    );
  },
  { deep: true, immediate: true } // immediate: true assure la synchro au chargement initial
);

</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>