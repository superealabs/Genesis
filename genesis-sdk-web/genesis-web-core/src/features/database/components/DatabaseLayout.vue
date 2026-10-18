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
    
    :can-compare="canCompare"
    :compare-count="compareCount"
    :comparing="comparing"
    @compare="$emit('compare')"
    @close-compare="$emit('close-compare')"
  >
    <DatabaseList
      :engines="engines"
      :selectedId="selectedId"
      :display="displayMode"
      :databaseSlots="databaseSlots"
      @select="(engine, ev) => $emit('select', engine, ev)"
      @info="(engine :DatabaseEngineDto) => $emit('info', engine)" 
    />

    <!-- ✅ Slot pour la vue de comparaison -->
    <template #comparator>
      <GenesisItemComparator
        :editors="comparatorEditors"
        :initial-editor-ids="comparatorEditorIds"
        @close="$emit('close-compare')"
      />
    </template>
  </GenesisCollectionLayout>

  <!-- ✅ Popup d'affichage des détails du moteur sélectionné -->
  <BaseFormPopup
    v-if="detailEngine"
    title="Détails du moteur"
    size="lg"
    @close="$emit('close-detail')"
  >
    <DatabaseDetails 
      :engine="detailEngine" 
    />
  </BaseFormPopup>
</template>

<script setup lang="ts">
// ============================================================================
// 1. IMPORTS
// ============================================================================
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

import DatabaseList from './DatabaseList.vue';
import DatabaseDetails from './DatabaseDetails.vue'; // ✅ Composant de détails créé précédemment
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import GenesisItemComparator from '@genesis-labs/web-core/core/components/layouts/GenesisItemComparator.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';

// ============================================================================
// 2. INTERFACE DES PROPS
// ============================================================================
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
  
  // ✅ État des détails
  detailEngine?: DatabaseEngineDto | null;

  // ✅ Props de comparaison
  canCompare?: boolean;
  compareCount?: number;
  comparing?: boolean;
  comparatorEditors?: PanelEditorDefinition[];
  comparatorEditorIds?: string[];
}

withDefaults(defineProps<DatabaseLayoutProps>(), {
  title: 'Base de Données',
  searchPlaceholder: 'Rechercher un moteur...',
  showBackButton: true,
  isLoading: false,
  detailEngine: null,
  canCompare: false,
  compareCount: 0,
  comparing: false,
  comparatorEditors: () => [],
  comparatorEditorIds: () => []
});

// ============================================================================
// 3. EMITS
// ============================================================================
defineEmits<{
  // Navigation
  'back': [];
  'close-detail': []; // ✅ Ajouté pour fermer le popup de détails
  
  // Mises à jour réactives (v-model)
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  
  // Actions utilisateur
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [engine: DatabaseEngineDto, event?: MouseEvent];
  'info': [engine: DatabaseEngineDto];
  
  // ✅ Actions de comparaison
  'compare': [];
  'close-compare': [];
}>();
</script>