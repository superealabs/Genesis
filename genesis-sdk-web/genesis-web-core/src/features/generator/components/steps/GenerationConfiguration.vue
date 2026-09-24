<template>
  <div class="flex flex-col gap-6 p-4 max-w-4xl mx-auto">
    
    <!-- 1. COMPOSANTS À GÉNÉRER -->
    <div class="space-y-3">
      <h3 class="text-lg font-semibold text-text flex items-center gap-2">
        Composants à générer
        <span class="text-accent text-sm font-normal">*</span>
      </h3>
      <p class="text-sm text-text-muted">
        Sélectionnez les couches de l'architecture que vous souhaitez créer.
      </p>
      
      <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
        <button
          v-for="comp in AVAILABLE_COMPONENTS"
          :key="comp.value"
          type="button"
          class="relative flex items-center justify-center gap-2 px-4 py-3 rounded-md border transition-all duration-200 font-medium"
          :class="isSelectedComponent(comp.value) 
            ? 'border-accent bg-accent/10 text-accent' 
            : 'border-secondary bg-bg-light text-text hover:border-accent/50 hover:bg-secondary'"
          @click="toggleComponent(comp.value)"
        >
          <svg v-if="isSelectedComponent(comp.value)" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="20 6 9 17 4 12"></polyline>
          </svg>
          <span>{{ comp.label }}</span>
        </button>
      </div>
    </div>

    <div class="border-t border-secondary"></div>

    <!-- 2. TABLES ET VUES -->
    <div class="space-y-3">
      <div class="flex items-center justify-between">
        <h3 class="text-lg font-semibold text-text flex items-center gap-2">
          Tables et Vues
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
      
      <p class="text-sm text-text-muted">
        Choisissez les entités de votre base de données à inclure dans la génération.
      </p>

      <div class="border border-secondary rounded-md overflow-hidden bg-bg-light">
        
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
          <div 
            v-for="item in combinedItems" 
            :key="item.tableName"
            class="flex items-center justify-between p-3 border-b border-secondary last:border-b-0 hover:bg-secondary/30 transition-colors cursor-pointer"
            @click="toggleItem(item)"
          >
            <div class="flex items-center gap-3 flex-1 min-w-0">
              <!-- Checkbox custom stylisée -->
              <div 
                class="w-5 h-5 rounded border flex items-center justify-center flex-shrink-0 transition-colors"
                :class="isItemSelected(item) ? 'bg-accent border-accent' : 'border-secondary bg-bg'"
              >
                <svg v-if="isItemSelected(item)" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
              </div>

              <div class="flex flex-col min-w-0">
                <span class="text-sm font-medium text-text truncate">{{ item.tableName }}</span>
                <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
              </div>
            </div>
          </div>

          <!-- État vide -->
          <div v-if="combinedItems.length === 0" class="p-6 text-center text-text-muted text-sm">
            Aucune table ou vue trouvée pour cette configuration.
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { 
  AVAILABLE_COMPONENTS, 
  type ComponentType,
  type TableMetadataDto 
} from '@genesis-labs/shared-types';

// ============================================================================
// 1. COMPOSABLES & ÉTAT LOCAL
// ============================================================================
const { 
  stepperData, 
  toggleTable, 
  toggleView, 
  toggleComponent,
  tables,
  views,
  fetchTablesMetadata 
} = useGenerator();

const isLoading = ref(false);

// ============================================================================
// 2. COMPUTEDS (Données dérivées)
// ============================================================================
const tableSelection = computed(() => stepperData.value.tableSelection);

/**
 * Fusionne les listes réactives de tables et de vues en une seule liste 
 * pour faciliter l'itération dans le template, tout en conservant 
 * la distinction du type pour la logique de sélection.
 */
const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => {
  return [
    ...tables.value.map(t => ({ ...t, type: 'table' as const })),
    ...views.value.map(v => ({ ...v, type: 'view' as const }))
  ];
});

/**
 * Vérifie si toutes les tables disponibles sont actuellement sélectionnées.
 * Retourne false s'il n'y a aucune table pour éviter un état "Tout sélectionner" ambigu.
 */
const areAllTablesSelected = computed(() => {
  if (tables.value.length === 0) return false;
  return tables.value.every(t => tableSelection.value.selectedTables.includes(t.tableName));
});

// ============================================================================
// 3. ACTIONS
// ============================================================================

const isSelectedComponent = (comp: ComponentType): boolean => {
  return tableSelection.value.selectedComponents.includes(comp);
};

const isItemSelected = (item: TableMetadataDto): boolean => {
  if (item.isView) {
    return tableSelection.value.selectedViews.includes(item.tableName);
  }
  return tableSelection.value.selectedTables.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto): void => {
  if (item.isView) {
    toggleView(item.tableName);
  } else {
    toggleTable(item.tableName);
  }
};

/**
 * Bascule la sélection de toutes les tables.
 * Au lieu de manipuler le tableau élément par élément avec splice/push, 
 * on réaffecte proprement le tableau réactif pour une meilleure lisibilité 
 * et des performances optimales.
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

/**
 * Déclenche le chargement des métadonnées des tables au montage du composant.
 * Ce composant est responsable d'initier ce fetch s'il n'a pas déjà été fait 
 * par le wizard-step-config.
 */
onMounted(async () => {
  isLoading.value = true;
  try {
    await fetchTablesMetadata();
  } catch (error) {
    console.error('[GenerationConfiguration] Erreur lors du chargement des tables:', error);
  } finally {
    isLoading.value = false;
  }
});
</script>