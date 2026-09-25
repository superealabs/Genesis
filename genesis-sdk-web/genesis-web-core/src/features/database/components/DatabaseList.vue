<template>
  <GenesisList :display="display" minColWidth="200px">
    <GenesisItem
      v-for="engine in engines"
      :key="engine.id"
      :label="engine.name"
      :sublabel="`Port: ${engine.port}`"
      :selected="selectedId === engine.id"
      :badge="databaseSlots?.get(engine.id) ?? null"
      @click="$emit('select', engine, $event)"
    >
      <!-- 
        Préservation de la structure pour le mode 'table'.
        Ces balises <td> seront injectées dans les colonnes définies par le parent 
        si le mode d'affichage est défini sur 'table'.
      -->
      <template #default>
        <td class="p-3 text-center">{{ engine.name }}</td>
        <td class="p-3 text-center text-text-muted">{{ engine.driver }}</td>
      </template>
    </GenesisItem>
  </GenesisList>
</template>

<script setup lang="ts">
// ============================================================================
// 1. IMPORTS
// ============================================================================
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.vue';

// ============================================================================
// 2. PROPS
// ============================================================================
/**
 * Composant de liste spécifique aux moteurs de base de données.
 * Il délègue la mise en page (grille, liste, tableau) au composant générique GenesisList.
 */
defineProps<{
  /** Liste des moteurs de base de données à afficher. */
  engines: DatabaseEngineDto[];
  
  /** ID du moteur actuellement sélectionné. */
  selectedId?: number | null;
  
  /** Mode d'affichage actuel (grid, list, ou table). */
  display: DisplayMode;
  
  /** Map des slots de comparaison occupés (ex: Map(1 => 'A')). */
  databaseSlots?: Map<number, string>;
}>();

// ============================================================================
// 3. EMITS
// ============================================================================
defineEmits<{
  /** Émis lorsque l'utilisateur clique sur un moteur de base de données. */
  select: [engine: DatabaseEngineDto, event?: MouseEvent];
}>();
</script>