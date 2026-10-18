<template>
  <GenesisCollectionLayout
    :title="title"
    :searchValue="searchQuery"
    :displayMode="displayMode"
    @update:searchValue="$emit('update:searchValue', $event)"
    @update:displayMode="$emit('update:displayMode', $event)"
    @update:mode="$emit('update:mode', $event)"
    :mode="compareMode"
    :searchPlaceholder="searchPlaceholder"
    :showBackButton="showBackButton"
    @back="$emit('back')"
    @openFilter="$emit('openFilter')"
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
    <FrameworkList
      :frameworks="frameworks"
      :selectedId="selectedId"
      :display="displayMode"
      :frameworkSlots="frameworkSlots"
      :group-by="groupBy"
      :group-options="frameworkGroupOptions"
      :group-labels="dynamicGroupLabels"
      @select="(fw, ev) => $emit('select', fw, ev)"
      @info="(fw) => $emit('info', fw)"
      @update:groupBy="$emit('update:groupBy', $event)"
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

  <!-- ✅ Popup de Filtres -->
  <BaseFormPopup
    v-if="isFilterOpen"
    title="Filtres des Frameworks"
    size="lg"
    @close="$emit('closeFilter')"
  >
    <FrameworkFilter 
      :filters="filters"
      @update:filters="$emit('update:filters', $event)"
      @close="$emit('closeFilter')" 
    />
  </BaseFormPopup>

  <!-- ✅ CORRECTION : Popup d'affichage des détails du framework sélectionné -->
  <BaseFormPopup
    v-if="detailFramework"
    title="Détails du framework"
    size="lg"
    @close="$emit('closeDetail')"
  >
    <FrameworkDetails 
      :framework="detailFramework" 
    />
  </BaseFormPopup>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { Framework, Language } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel.ts';

import FrameworkList from './FrameworkList.vue'; 
import FrameworkFilter from './FrameworkFilter.vue'; 
import FrameworkDetails from './FrameworkDetails.vue'; // ✅ Import du composant de détails
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisItemComparator from '@genesis-labs/web-core/core/components/layouts/GenesisItemComparator.vue';

// ============================================================================
// 1. INTERFACE DES PROPS
// ============================================================================
export interface FrameworkLayoutProps {
  title?: string;
  searchQuery: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  frameworks: Framework[];
  languages?: Language[];
  selectedId?: number;
  frameworkSlots: Map<number, string>;
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  filters?: any; 
  detailFramework: Framework | null; // ✅ Déjà présent
  isFilterOpen: boolean;
  pendingFramework: Framework | null;
  isLoading: boolean;
  groupBy?: keyof Framework | null; 
  groupLabels?: Record<string | number, string>;
  
  // ✅ Props de comparaison
  canCompare?: boolean;
  compareCount?: number;
  comparing?: boolean;
  comparatorEditors?: PanelEditorDefinition[];
  comparatorEditorIds?: string[];
}

const props = withDefaults(defineProps<FrameworkLayoutProps>(), {
  title: 'Frameworks',
  searchPlaceholder: 'Rechercher par nom, core, type...',
  showBackButton: true,
  isLoading: false,
  groupBy: null,
  groupLabels: () => ({}),
  detailFramework: null, // ✅ Valeur par défaut ajoutée pour la propreté
  canCompare: false,
  compareCount: 0,
  comparing: false,
  comparatorEditors: () => [],
  comparatorEditorIds: () => []
});

// ============================================================================
// 2. EMITS
// ============================================================================
defineEmits<{
  // Navigation
  'back': [];
  'openFilter': [];
  'closeFilter': [];
  'closeDetail': []; // ✅ Déjà présent
  
  // Actions utilisateur
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [framework: Framework, event?: MouseEvent];
  'info': [framework: Framework];
  
  // Actions de comparaison
  'compare': [];
  'close-compare': [];
  
  // Mises à jour réactives (v-model)
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'update:filters': [filters: any];
  'update:groupBy': [value: keyof Framework | null];
}>();

// ============================================================================
// 3. CONSTANTES DE CONFIGURATION
// ============================================================================
const frameworkGroupOptions: { label: string; value: keyof Framework | null }[] = [
  { label: 'Tous les frameworks', value: null },
  { label: 'Par Core Framework', value: 'coreFramework' },
  { label: 'Par Langage', value: 'languageId' },
  { label: 'Statut Production', value: 'isProd' }
];

// ============================================================================
// 4. COMPUTEDS (LOGIQUE METIER LOCALE)
// ============================================================================
const dynamicGroupLabels = computed<Record<string | number, string>>(() => {
  const labels: Record<string | number, string> = {
    'null': 'Non spécifié',
    'undefined': 'Non spécifié',
    'true': 'Prêt pour la Production',
    'false': 'En Développement'
  };

  if (props.languages && props.languages.length > 0) {
    props.languages.forEach((lang) => {
      labels[lang.id] = lang.name;
    });
  }
  
  return labels;
});
</script>