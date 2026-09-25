<template>
  <GenesisCollectionLayout
    :title="title"
    :displayMode="displayMode"
    @update:displayMode="$emit('update:displayMode', $event)"
    :mode="compareMode"
    @update:mode="$emit('update:mode', $event)"
    :searchPlaceholder="searchPlaceholder"
    :showBackButton="showBackButton"
    :showFilter="false"
    :showSort="false"
    :showCarousel="true"
    @back="$emit('back')"
    :replaceOptions="replaceOptions"
    :showReplacePopup="showReplacePopup"
    :mouseX="mouseX"
    :mouseY="mouseY"
    @select-replace="$emit('select-replace', $event)"
    @close-replace="$emit('close-replace')"
    :isLoading="isLoading"
  >
    <DatabaseList
      :engines="engines"
      :selectedId="selectedId"
      :display="displayMode"
      :databaseSlots="databaseSlots"
      @select="(engine, ev) => $emit('select', engine, ev)"
    />
  </GenesisCollectionLayout>
</template>

<script setup lang="ts">
// ============================================================================
// 1. IMPORTS
// ============================================================================
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.types';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';

import DatabaseList from './DatabaseList.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';

// ============================================================================
// 2. INTERFACE DES PROPS
// ============================================================================
/**
 * Propriétés attendues par le composant de mise en page des bases de données.
 * Ce composant sert de pont entre la logique du wizard et l'affichage de la liste,
 * en gérant l'agencement global (recherche, mode d'affichage, popup de remplacement).
 */
export interface DatabaseLayoutProps {
  title?: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  engines: DatabaseEngineDto[];
  selectedId?: number;
  databaseSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  isLoading: boolean;
}

withDefaults(defineProps<DatabaseLayoutProps>(), {
  title: 'Base de Données',
  searchPlaceholder: 'Rechercher un moteur...',
  showBackButton: true,
  isLoading: false
});

// ============================================================================
// 3. EMITS
// ============================================================================
defineEmits<{
  // Navigation
  'back': [];
  
  // Mises à jour réactives (v-model)
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  
  // Actions utilisateur
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [engine: DatabaseEngineDto, event?: MouseEvent];
}>();
</script>