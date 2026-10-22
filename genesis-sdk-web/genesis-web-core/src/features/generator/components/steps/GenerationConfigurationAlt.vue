<template>
  <main class="flex gap-6 h-full p-6 overflow-hidden">
    
    <!-- ═══ COLONNE 1 : TABLES ET VUES NON ASSIGNÉES (Disponibles) ═══ -->
    <div class="flex-1 min-h-0 flex flex-col space-y-3">
      <div class="flex items-center justify-between flex-shrink-0">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          Disponibles (Non assignées)
          <span class="text-primary text-sm font-normal">*</span>
        </h3>
        <button 
          v-if="unassignedItems.length > 0"
          type="button"
          class="text-xs text-primary hover:text-primary/80 font-medium transition-colors"
          @click="toggleAllUnassigned"
        >
          {{ areAllUnassignedSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
        </button>
      </div>
      
      <p class="text-sm text-text-muted flex-shrink-0">
        Choisissez les entités à inclure dans vos profils.
      </p>

      <div class="flex flex-row gap-2 flex-shrink-0">
        <GenesisInput
          v-model="searchQueryUnassigned"
          type="text"
          placeholder="Rechercher une table ou une vue..."
          size="md"
          fill-width
        >
          <template #left>
            <IconSearch :size="16" class="text-text-muted" />
          </template>
        </GenesisInput>
        <GenesisSegmentedControl v-model="activeFilterLeft" :options="filterOptions" size="md" />
      </div>

      <GenesisContainer
        :is-loading="isLoading"
        loading-text="Chargement des métadonnées..."
        :is-empty="unassignedItems.length === 0"
        empty-text="Aucune table disponible."
        :min-height="200"
        :max-height="500"
        :height="400" 
      >
        <div 
          v-for="item in unassignedItems" 
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
        </div>
      </GenesisContainer>
    </div>

    <!-- ═══ COLONNE 2 : GESTION DES CONFIGURATIONS (Centrale) ═══ -->
    <section class="w-80 flex-shrink-0 flex flex-col gap-6 overflow-y-auto custom-scrollbar pr-2">
      <div>
        <h3 class="text-lg font-semibold text-text mb-3 flex items-center gap-2">
          Profils de Génération
          <span class="text-primary text-sm font-normal">*</span>
          
          <!-- ✅ BOUTON D'AIDE / TUTORIEL -->
          <button 
            type="button" 
            class="text-text-muted hover:text-primary transition-colors ml-1 flex items-center justify-center"
            title="Voir le tutoriel d'utilisation"
            @click="isTutorialOpen = true"
          >
            <IconHelpCircle :size="18" />
          </button>
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

      <div v-if="activeConfig" class="p-4 rounded-lg border border-bg-neutral-genesis space-y-3">
        <div class="flex items-center justify-between">
          <h4 class="text-sm font-semibold text-text">
            Composants : <span class="text-primary">{{ activeConfig.name }}</span>
          </h4>
        </div>
        
        <div class="flex items-center justify-between py-2 border-b border-secondary/50 mb-2">
          <span class="text-xs font-medium text-text-muted">
            Dépendances hiérarchiques
          </span>
          <GenesisSwitch v-model="enforceDependencies" size="sm" />
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

    <!-- ═══ COLONNE 3 : TABLES ET VUES ASSIGNÉES ═══ -->
    <div class="flex-1 min-h-0 flex flex-col space-y-3">
      <div class="flex items-center justify-between flex-shrink-0">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          Déjà assignées
        </h3>
        <button 
          v-if="assignedItems.length > 0"
          type="button"
          class="text-xs text-primary hover:text-primary/80 font-medium transition-colors"
          @click="toggleAllAssigned"
        >
          {{ areAllAssignedSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
        </button>
      </div>
      
      <p class="text-sm text-text-muted flex-shrink-0">
        Entités liées à un profil de génération.
      </p>

      <div class="flex flex-row gap-2 flex-shrink-0">
        <GenesisInput
          v-model="searchQueryAssigned"
          type="text"
          placeholder="Rechercher une table ou une vue..."
          size="md"
          fill-width
        >
          <template #left>
            <IconSearch :size="16" class="text-text-muted" />
          </template>
        </GenesisInput>
        <GenesisSegmentedControl v-model="activeFilterRight" :options="filterOptions" size="md" />
      </div>

      <GenesisContainer
        :is-loading="false"
        :is-empty="assignedItems.length === 0"
        empty-text="Aucune table assignée."
        :min-height="200"
        :max-height="500"
        :height="400" 
      >
        <div 
          v-for="item in assignedItems" 
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
          
          <div class="flex-shrink-0 text-xs text-primary ml-4 text-right min-w-[120px] truncate font-medium" :title="getAssignedConfig(item)">
            {{ getAssignedConfig(item) }}
          </div>
        </div>
      </GenesisContainer>
    </div>
    <BaseFormPopup
      v-if="isTutorialOpen"
      title="Comment configurer la génération ?"
      size="md"
      @close="isTutorialOpen = false"
    >
      <div class="space-y-6 p-2">
        <CarrouselPanel :slides="tutorialSlides" />
        
        <div class="bg-bg-light/50 p-4 rounded-lg border border-bg-neutral-genesis">
          <h4 class="text-sm font-semibold text-text mb-2">💡 Astuce rapide</h4>
          <p class="text-sm text-text-muted leading-relaxed">
            Utilisez les cases à cocher dans les colonnes de gauche ou de droite pour sélectionner des entités en masse, 
            puis utilisez les boutons <strong>Assigner</strong> ou <strong>Retirer</strong> dans le panneau central pour les lier à votre profil actif.
          </p>
        </div>
      </div>
    </BaseFormPopup>
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
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue';
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import { useGeneratorStore } from '../../manifest';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';


// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const configManager = useConfigurationManager([], { singleConfiguration: true });
const store = useGeneratorStore();
const { tables, views, fetchTablesMetadata } = useGenerator();

const isLoading = ref(false);
const enforceDependencies = ref(false);
const isTutorialOpen = ref(false); 

//  Recherches et filtres INDÉPENDANTS pour chaque colonne
const searchQueryUnassigned = ref('');
const activeFilterLeft = ref<'all' | 'table' | 'view'>('all');

const searchQueryAssigned = ref('');
const activeFilterRight = ref<'all' | 'table' | 'view'>('all');

const { loadGenerationOptionsConfigurations, saveConfiguration, deleteConfiguration } = useConfig();

// ============================================================================
// 2. CONSTANTES & HIÉRARCHIE
// ============================================================================
const filterOptions = [
  { label: 'Tous', value: 'all' },
  { label: 'Tables', value: 'table' },
  { label: 'Vues', value: 'view' }
];

const tutorialSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: '1. Sélectionnez les tables/vues disponibles à gauche.' },
  { color: '#EF4444', label: '2. Créez ou sélectionnez un profil de génération au centre.' },
  { color: '#10B981', label: '3. Cliquez sur "Assigner" pour lier les tables au profil.' },
];

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

// Fonction utilitaire pour appliquer les filtres et la recherche
const applyFilters = (items: typeof combinedItems.value, searchQuery: string, filter: string) => {
  let filtered = items;
  if (filter === 'table') filtered = filtered.filter(item => !item.isView);
  else if (filter === 'view') filtered = filtered.filter(item => item.isView);
  
  if (searchQuery.trim()) {
    const query = searchQuery.toLowerCase();
    filtered = filtered.filter(item => item.tableName.toLowerCase().includes(query));
  }
  return filtered;
};

//  Tables NON assignées (déjà filtrées)
const unassignedItems = computed(() => {
  const items = combinedItems.value.filter(item => getAssignedConfig(item) === 'Non assigné');
  return applyFilters(items, searchQueryUnassigned.value, activeFilterLeft.value);
});

//  Tables ASSIGNÉES (déjà filtrées)
const assignedItems = computed(() => {
  const items = combinedItems.value.filter(item => getAssignedConfig(item) !== 'Non assigné');
  return applyFilters(items, searchQueryAssigned.value, activeFilterRight.value);
});

//  Vérifie si TOUS les éléments VISIBLES (filtrés) sont sélectionnés
const areAllUnassignedSelected = computed(() => {
  if (unassignedItems.value.length === 0) return false;
  return unassignedItems.value.every(item => store.selectedItemsInList.includes(item.tableName));
});

const areAllAssignedSelected = computed(() => {
  if (assignedItems.value.length === 0) return false;
  return assignedItems.value.every(item => store.selectedItemsInList.includes(item.tableName));
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
    payload: { components: [...uiConfig.components] }
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

function handleComponentToggle(comp: ComponentType, isChecked: boolean) {
  if (!activeConfig.value) return;
  
  let newComponents = [...activeConfig.value.components];

  if (enforceDependencies.value) {
    if (isChecked) {
      const prereqs = getPrerequisites(comp);
      [...prereqs, comp].forEach(c => {
        if (!newComponents.includes(c)) newComponents.push(c);
      });
    } else {
      const dependents = getDependents(comp);
      newComponents = newComponents.filter(c => c !== comp && !dependents.includes(c as ComponentType));
    }
  } else {
    const idx = newComponents.indexOf(comp);
    if (isChecked && idx === -1) {
      newComponents.push(comp);
    } else if (!isChecked && idx !== -1) {
      newComponents.splice(idx, 1);
    } else {
      return;
    }
  }
  
  configManager.editConfiguration(activeConfig.value.id, newComponents);
  persistConfig(activeConfig.value.id);
}

function isComponentDisabled(comp: ComponentType): boolean {
  if (!enforceDependencies.value || !activeConfig.value) return false;
  const currentIndex = COMPONENT_HIERARCHY.indexOf(comp);
  const isChecked = activeConfig.value.components.includes(comp);

  if (isChecked) {
    const higherComponents = COMPONENT_HIERARCHY.slice(currentIndex + 1);
    return higherComponents.some(higherComp => activeConfig.value!.components.includes(higherComp));
  }
  return false;
}

const isItemSelected = (item: TableMetadataDto): boolean => {
  return store.selectedItemsInList.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto): void => {
  store.toggleItemInList(item.tableName);
};

//  "Tout sélectionner" agit UNIQUEMENT sur les éléments visibles (filtrés) de la colonne de gauche
const toggleAllUnassigned = (): void => {
  const allSelected = areAllUnassignedSelected.value;
  unassignedItems.value.forEach(item => {
    if (allSelected) {
      // Désélectionne l'élément (toggleItemInList le retirera s'il est présent)
      store.toggleItemInList(item.tableName);
    } else {
      // Sélectionne l'élément s'il ne l'est pas déjà
      if (!store.selectedItemsInList.includes(item.tableName)) {
        store.selectedItemsInList.push(item.tableName);
      }
    }
  });
};

//  "Tout sélectionner" agit UNIQUEMENT sur les éléments visibles (filtrés) de la colonne de droite
const toggleAllAssigned = (): void => {
  const allSelected = areAllAssignedSelected.value;
  assignedItems.value.forEach(item => {
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
    return { id: c.id, name: c.name, isHidden: false, components: cleanComponents };
  });
  
  if (configManager.configurations.value.length > 0) {
    configManager.selectConfiguration(configManager.configurations.value[0].id);
  }
}

onMounted(async () => {
  isLoading.value = true;
  try {
    await Promise.all([ fetchTablesMetadata(), syncGenerationConfigs() ]);
  } catch (error) {
    console.error('[GenerationConfigurationAlt] Erreur lors de l\'initialisation:', error);
  } finally {
    isLoading.value = false;
  }
});

watch(
  () => configManager.configurations.value,
  (newConfigs) => {
    store.setGenerationProfiles(newConfigs.map(c => ({
      id: c.id, name: c.name, components: c.components as ComponentType[]
    })));
  },
  { deep: true }
);

watch(enforceDependencies, (isEnforced) => {
  if (isEnforced && activeConfig.value) {
    let newComponents = [...activeConfig.value.components];
    let hasChanged = false;
    
    newComponents.forEach((comp) => {
      const prereqs = getPrerequisites(comp as ComponentType);
      prereqs.forEach((prereq) => {
        if (!newComponents.includes(prereq)) {
          newComponents.push(prereq);
          hasChanged = true;
        }
      });
    });

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