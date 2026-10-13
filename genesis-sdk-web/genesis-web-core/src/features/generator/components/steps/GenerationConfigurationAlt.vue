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

          <!-- APRÈS (Propre et Réutilisable) -->
          <GenesisContainer
            :is-loading="isLoading"
            loading-text="Chargement des métadonnées..."
            :is-empty="filteredCombinedItems.length === 0"
            empty-text="Aucune table ou vue trouvée."
            :min-height="200"
            :max-height="500"
            :height="400" 
          >
            <!-- Le contenu de ta boucle v-for va directement ici -->
            <div 
              v-for="item in filteredCombinedItems" 
              :key="item.tableName"
              class="flex items-center justify-between p-3 border-b border-bg-neutral-genesis last:border-b-0 hover:bg-bg-secondary hover:text-secondary transition-colors cursor-pointer"
              @click="toggleItem(item)"
            >
              <div class="flex items-center gap-3 flex-1 min-w-0">
                <GenesisCheckboxSimple :model-value="isItemSelected(item)" />
                <div class="flex flex-col min-w-0">
                  <span class="text-sm font-medium truncate">{{ item.tableName }}</span>
                  <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
                </div>
              </div>
              
              <div class="flex-shrink-0 text-xs text-text-muted ml-4 text-right min-w-[120px] truncate" :title="getAssignedConfig(item)">
                {{ getAssignedConfig(item) }}
              </div>
            </div>

            <!-- Optionnel : Tu peux aussi utiliser le slot nommé 'empty' pour un contenu personnalisé -->
            <!-- <template #empty>
              <div class="p-6 text-center text-text-muted text-sm flex-shrink-0">
                Message personnalisé si vide
              </div>
            </template> -->
          </GenesisContainer>
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
            
            <!-- ✅ AJOUT : Switch pour activer/désactiver les dépendances hiérarchiques -->
            <div class="flex items-center justify-between py-2 border-b border-secondary/50 mb-2">
              <span class="text-xs font-medium text-text-muted">
                Appliquer les dépendances hiérarchiques
              </span>
              <GenesisSwitch
                v-model="enforceDependencies"
                size="sm"
              />
            </div>

            <div class="flex flex-col gap-2">
              <GenesisCheckboxSimple
                v-for="comp in AVAILABLE_COMPONENTS"
                :key="comp.value"
                :model-value="activeConfig.components.includes(comp.value)"
                :label="comp.label"
                :disabled="isComponentDisabled(comp.value)"
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
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue'; // ✅ AJOUTÉ
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import { useGeneratorStore } from '../../manifest';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';

// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const configManager = useConfigurationManager([], { singleConfiguration: true });
const store = useGeneratorStore();

const { tables, views, fetchTablesMetadata } = useGenerator();

const isLoading = ref(false);
const tableSearchQuery = ref('');

const enforceDependencies = ref(false);


const { 
  loadGenerationOptionsConfigurations, 
  saveConfiguration, 
  deleteConfiguration,
} = useConfig();

// ============================================================================
// 2. CONSTANTES & HIÉRARCHIE
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

// ✅ AJOUT : Définition stricte de l'ordre hiérarchique
const COMPONENT_HIERARCHY: ComponentType[] = ['model', 'dao', 'service', 'controller'];

const getPrerequisites = (comp: ComponentType): ComponentType[] => {
  const index = COMPONENT_HIERARCHY.indexOf(comp);
  return COMPONENT_HIERARCHY.slice(0, index);
};

const getDependents = (comp: ComponentType): ComponentType[] => {
  const index = COMPONENT_HIERARCHY.indexOf(comp);
  return COMPONENT_HIERARCHY.slice(index + 1);
};

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
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
  if (activeFilter.value === 'table') items = items.filter(item => !item.isView);
  else if (activeFilter.value === 'view') items = items.filter(item => item.isView);
  
  if (tableSearchQuery.value.trim()) {
    const query = tableSearchQuery.value.toLowerCase();
    items = items.filter(item => item.tableName.toLowerCase().includes(query));
  }
  return items;
});

const areAllVisibleSelected = computed(() => {
  if (filteredCombinedItems.value.length === 0) return false;
  return filteredCombinedItems.value.every(item => store.selectedItemsInList.includes(item.tableName));
});

// ============================================================================
// 4. ACTIONS
// ============================================================================

async function persistConfig(id: string | number) {
  const uiConfig = configManager.configurations.value.find(c => c.id === id);
  if (!uiConfig) return;

  const updatedConfig: GenesisConfig<GeneratorGenerationOptionsPayload> = {
    id: String(uiConfig.id),
    name: uiConfig.name,
    configType: 'generator_generation-options',
    schemaVersion: '1.0.0',
    createdAt: new Date().toISOString(),
    payload: {
      components: [...uiConfig.components]
    }
  };
  
  await saveConfiguration(updatedConfig);
}

async function handleAddConfig() {
  const defaultComponents: ComponentType[] = ['model', 'dao', 'service', 'controller'];
  const newUiConfig = configManager.addConfiguration(defaultComponents);
  
  const newGenesisConfig: GenesisConfig<GeneratorGenerationOptionsPayload> = {
    id: String(newUiConfig.id),
    name: newUiConfig.name,
    configType: 'generator_generation-options',
    schemaVersion: '1.0.0',
    createdAt: new Date().toISOString(),
    payload: { components: [...defaultComponents] }
  };
  
  await saveConfiguration(newGenesisConfig);
}

function handleRename(id: string | number, newName: string) {
  configManager.renameConfiguration(id, newName);
  persistConfig(id);
}

function handleAssign(configId: string | number, items: string[]) {
  items.forEach(tableName => {
    const item = combinedItems.value.find(i => i.tableName === tableName);
    if (item) store.assignTableToConfig(configId, tableName, item.isView);
  });
  store.clearSelectedItemsInList();
  persistConfig(configId);
}

function handleRemove(configId: string | number, items: string[]) {
  items.forEach(tableName => {
    const item = combinedItems.value.find(i => i.tableName === tableName);
    if (item) store.removeTableFromConfig(configId, tableName, item.isView);
  });
  store.clearSelectedItemsInList();
  persistConfig(configId);
}

async function handleDeleteConfig(id: string | number) {
  configManager.deleteConfiguration(id);
  await deleteConfiguration(String(id));
}

// ✅ MODIFIÉ : Gère la logique de cascade si enforceDependencies est actif
function handleComponentToggle(comp: ComponentType, isChecked: boolean) {
  if (!activeConfig.value) return;
  
  let newComponents = [...activeConfig.value.components];

  if (enforceDependencies.value) {
    if (isChecked) {
      // Mode strict : On coche le composant ET tous ses prérequis
      const prereqs = getPrerequisites(comp);
      [...prereqs, comp].forEach(c => {
        if (!newComponents.includes(c)) {
          newComponents.push(c);
        }
      });
    } else {
      // Mode strict : On décoche le composant ET tous ses dépendants
      const dependents = getDependents(comp);
      newComponents = newComponents.filter(c => c !== comp && !dependents.includes(c as ComponentType));
    }
  } else {
    // Mode libre : Comportement standard
    const idx = newComponents.indexOf(comp);
    if (isChecked && idx === -1) {
      newComponents.push(comp);
    } else if (!isChecked && idx !== -1) {
      newComponents.splice(idx, 1);
    } else {
      return; // Aucun changement
    }
  }
  
  configManager.editConfiguration(activeConfig.value.id, newComponents);
  persistConfig(activeConfig.value.id);
}

//  AJOUT : Détermine si une checkbox doit être verrouillée (disabled)
function isComponentDisabled(comp: ComponentType): boolean {
  if (!enforceDependencies.value || !activeConfig.value) return false;

  const currentIndex = COMPONENT_HIERARCHY.indexOf(comp);
  const isChecked = activeConfig.value.components.includes(comp);

  if (isChecked) {
    // Si ce composant est coché, on vérifie si un composant de niveau supérieur l'est aussi.
    // Si oui, on ne peut pas le décocher sans casser la hiérarchie.
    const higherComponents = COMPONENT_HIERARCHY.slice(currentIndex + 1);
    const hasHigherChecked = higherComponents.some(higherComp => 
      activeConfig.value!.components.includes(higherComp)
    );
    return hasHigherChecked;
  }

  // S'il n'est pas coché, l'utilisateur peut toujours cliquer pour le cocher 
  // (ce qui déclenchera l'ajout des prérequis).
  return false;
}

const isItemSelected = (item: TableMetadataDto): boolean => {
  return store.selectedItemsInList.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto): void => {
  store.toggleItemInList(item.tableName);
};

const toggleAllVisibleItems = (): void => {
  const allSelected = areAllVisibleSelected.value;
  filteredCombinedItems.value.forEach(item => {
    if (allSelected) {
      store.toggleItemInList(item.tableName);
    } else {
      if (!store.selectedItemsInList.includes(item.tableName)) {
        store.selectedItemsInList.push(item.tableName);
      }
    }
  });
};

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
// 5. LIFECYCLE & SYNCHRONISATION
// ============================================================================

async function syncGenerationConfigs() {
  const data = await loadGenerationOptionsConfigurations();
  
  const validComponentValues = AVAILABLE_COMPONENTS.map(c => c.value);
  
  configManager.configurations.value = data.map(c => {
    const p = c.payload as unknown as GeneratorGenerationOptionsPayload;
    
    const cleanComponents = (p.components || []).filter((comp): comp is ComponentType => 
      validComponentValues.includes(comp as ComponentType)
    );

    return {
      id: c.id,
      name: c.name,
      isHidden: false,
      components: cleanComponents
    };
  });
  
  if (configManager.configurations.value.length > 0) {
    configManager.selectConfiguration(configManager.configurations.value[0].id);
  }
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
        components: c.components as ComponentType[]
      }))
    );
  },
  { deep: true }
);

watch(enforceDependencies, (isEnforced) => {
  if (isEnforced && activeConfig.value) {
    let newComponents = [...activeConfig.value.components];
    let hasChanged = false;
    // tada
    // Pour chaque composant actuellement coché, on s'assure que ses prérequis le sont aussi
    newComponents.forEach((comp) => {
      const prereqs = getPrerequisites(comp as ComponentType);
      prereqs.forEach((prereq) => {
        if (!newComponents.includes(prereq)) {
          newComponents.push(prereq);
          hasChanged = true;
        }
      });
    });

    // Si des prérequis manquants ont été ajoutés, on sauvegarde la mise à jour
    if (hasChanged) {
      configManager.editConfiguration(activeConfig.value.id, newComponents);
      persistConfig(activeConfig.value.id);
    }
  }
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>