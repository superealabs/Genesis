<template>
  <GenesisList :display="display" minColWidth="200px">
    <GenesisItem
      v-for="fw in frontends"
      :key="fw.id"
      :label="fw.name"
      :sublabel="fw.coreFramework"
      :selected="selectedId === fw.id"
      :show-info-button="true"
      :badge="frameworkSlots?.get(fw.id) ?? null"
      @click="$emit('select', fw, $event)"
      @info="$emit('info', fw)"
    >
      <!-- 
        Préservation de la structure pour le mode 'table'.
        Ces balises <td> seront injectées dans les colonnes définies par le parent 
        si le mode d'affichage est défini sur 'table'.
      -->
      <template #default>
        <td class="p-3 text-center">{{ fw.name }}</td>
        <td class="p-3 text-center text-text-muted">{{ fw.coreFramework }}</td>
      </template>
    </GenesisItem>
  </GenesisList>
</template>

<script setup lang="ts">
// ============================================================================
// 1. IMPORTS
// ============================================================================
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';

import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.vue';

// ============================================================================
// 2. PROPS
// ============================================================================
/**
 * Composant de liste spécifique aux frameworks frontend.
 * Il délègue la mise en page (grille, liste, tableau) au composant générique GenesisList.
 */
defineProps<{
  /** Liste des frameworks frontend à afficher. */
  frontends: FrontendFramework[];
  
  /** ID du framework actuellement sélectionné. */
  selectedId?: number;
  
  /** Mode d'affichage actuel (grid, list, ou table). */
  display: DisplayMode;
  
  /** Map des slots de comparaison occupés (ex: Map(1 => 'A')). */
  frameworkSlots?: Map<number, string>;
}>();

// ============================================================================
// 3. EMITS
// ============================================================================
defineEmits<{
  /** Émis lorsque l'utilisateur clique sur un framework frontend. */
  select: [framework: FrontendFramework, event?: MouseEvent];
  
  /** Émis lorsque l'utilisateur clique sur le bouton d'information. */
  info: [framework: FrontendFramework];
}>();
</script>