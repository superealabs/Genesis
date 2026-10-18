<template>
  <GenesisCollectionLayout
    :title="title"
    :searchValue="searchQuery"
    :displayMode="displayMode"
    @update:searchValue="$emit('update:searchValue', $event)"
    @update:displayMode="$emit('update:displayMode', $event)"
    :mode="compareMode"
    @update:mode="$emit('update:mode', $event)"
    :searchPlaceholder="searchPlaceholder"
    :showBackButton="showBackButton"
    :showFilter="false"
    :showSort="false"
    :showCarousel="true"
    @back="$emit('back')"
    :replace-options="replaceOptions"
    :show-replace-popup="showReplacePopup"
    :mouse-x="mouseX"
    :mouse-y="mouseY"
    @select-replace="$emit('select-replace', $event)"
    @close-replace="$emit('close-replace')"
    :is-loading="isLoading"
    
    :can-compare="canCompare"
    :compare-count="compareCount"
    :comparing="comparing"
    @compare="$emit('compare')"
    @close-compare="$emit('close-compare')"
  >
    <FrontendList
      :frontends="frontends"
      :selectedId="selectedId"
      :display="displayMode"
      :framework-slots="frontendFrameworkSlots"
      @select="(fw, ev) => $emit('select', fw, ev)"
      @info="(fw) => $emit('info', fw)"
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

  <!-- ✅ Popup d'affichage des détails du framework sélectionné -->
  <BaseFormPopup
    v-if="detailFramework"
    title="Détails du framework frontend"
    size="lg"
    @close="$emit('close-detail')"
  >
    <FrontendDetails 
      :framework="detailFramework" 
    />
  </BaseFormPopup>
</template>

<script setup lang="ts">
// ============================================================================
// 1. IMPORTS
// ============================================================================
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

import FrontendList from './FrontendList.vue';
import FrontendDetails from './FrontendDetails.vue'; // ✅ Composant de détails créé précédemment
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import GenesisItemComparator from '@genesis-labs/web-core/core/components/layouts/GenesisItemComparator.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';

// ============================================================================
// 2. INTERFACE DES PROPS
// ============================================================================
/**
 * Propriétés attendues par le composant de mise en page des frameworks frontend.
 * Ce composant sert de pont entre la logique du wizard et l'affichage de la liste,
 * en gérant l'agencement global (recherche, mode d'affichage, popup de remplacement).
 */
export interface FrontendLayoutProps {
  title?: string;
  searchQuery: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  frontends: FrontendFramework[];
  selectedId?: number;
  frontendFrameworkSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  isLoading: boolean;
  
  // ✅ État des détails
  detailFramework?: FrontendFramework | null;

  // ✅ Props de comparaison
  canCompare?: boolean;
  compareCount?: number;
  comparing?: boolean;
  comparatorEditors?: PanelEditorDefinition[];
  comparatorEditorIds?: string[];
}

withDefaults(defineProps<FrontendLayoutProps>(), {
  title: 'Framework Frontend',
  searchPlaceholder: 'Rechercher par nom (ex: React, Vue)...',
  showBackButton: true,
  isLoading: false,
  detailFramework: null,
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
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  
  // Actions utilisateur
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [framework: FrontendFramework, event?: MouseEvent];
  'info': [framework: FrontendFramework];
  
  // ✅ Actions de comparaison
  'compare': [];
  'close-compare': [];
}>();
</script>