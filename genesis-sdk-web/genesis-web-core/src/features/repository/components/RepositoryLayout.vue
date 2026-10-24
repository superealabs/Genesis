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
    :showFilter="false" 
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
    <!-- ✅ MENU DE REGROUPEMENT -->
    <template #custom-menu>
      <GenesisDropdown
        v-if="providerGroupOptions && providerGroupOptions.length > 0"
        triggerVariant="secondary"
        trigger-size="lg"
      >
        <template #triggerIcon>
          <IconCategory />
        </template>
        
        <template #default="{ close }">
          <div class="flex flex-col gap-1 p-1 min-w-[180px]">
            <button
              v-for="opt in providerGroupOptions"
              :key="String(opt.value ?? 'null')"
              class="w-full text-left px-3 py-2 text-sm rounded hover:bg-bg-secondary transition-colors flex items-center justify-between"
              :class="groupBy === opt.value ? 'bg-bg-secondary font-medium text-text' : 'text-muted'"
              @click="handleUpdateGroupBy(opt.value); close()"
            >
              {{ opt.label }}
              <IconCheck v-if="groupBy === opt.value" class="w-4 h-4 text-primary shrink-0" />
            </button>
          </div>
        </template>
      </GenesisDropdown>
    </template>

    <RepositoryList
      :providers="providers"
      :selectedId="selectedId"
      :display="displayMode"
      :provider-slots="providerSlots"
      :group-by="groupBy"
      :group-options="providerGroupOptions"
      :group-labels="dynamicGroupLabels"
      @select="(p, ev) => $emit('select', p, ev)"
      @info="(p) => $emit('info', p)"
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

  <!-- ✅ Popup d'affichage des détails du fournisseur sélectionné -->
  <BaseFormPopup
    v-if="detailProvider"
    title="Détails du fournisseur"
    size="lg"
    @close="$emit('closeDetail')"
  >
    <RepositoryDetails 
      :provider="detailProvider" 
    />
  </BaseFormPopup>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel.ts';

import RepositoryList from './RepositoryList.vue'; 
import RepositoryDetails from './RepositoryDetails.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisItemComparator from '@genesis-labs/web-core/core/components/layouts/GenesisItemComparator.vue';
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import IconCategory from '@genesis-labs/web-core/core/components/ui/icons/IconCategory.vue';
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue';

// ============================================================================
// 1. INTERFACE DES PROPS
// ============================================================================
export interface RepositoryLayoutProps {
  title?: string;
  searchQuery: string;
  displayMode: DisplayMode;
  compareMode: 'selection' | 'compare';
  searchPlaceholder?: string;
  showBackButton?: boolean;
  providers: RepositoryProviderMetadata[];
  selectedId?: string; // ✅ String
  providerSlots: Map<string, string>; // ✅ String
  replaceOptions: SelectionOption[];
  showReplacePopup: boolean;
  mouseX: number | null;
  mouseY: number | null;
  detailProvider: RepositoryProviderMetadata | null;
  isLoading: boolean;
  groupBy?: keyof RepositoryProviderMetadata | null; 
  groupLabels?: Record<string | number, string>;
  
  canCompare?: boolean;
  compareCount?: number;
  comparing?: boolean;
  comparatorEditors?: PanelEditorDefinition[];
  comparatorEditorIds?: string[];
}

/*const props = */withDefaults(defineProps<RepositoryLayoutProps>(), {
  title: 'Fournisseurs de Dépôt',
  searchPlaceholder: 'Rechercher un fournisseur...',
  showBackButton: true,
  isLoading: false,
  groupBy: null,
  groupLabels: () => ({}),
  detailProvider: null,
  canCompare: false,
  compareCount: 0,
  comparing: false,
  comparatorEditors: () => [],
  comparatorEditorIds: () => []
});

// ============================================================================
// 2. EMITS
// ============================================================================
const emit = defineEmits<{
  'back': [];
  'closeDetail': [];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [provider: RepositoryProviderMetadata, event?: MouseEvent];
  'info': [provider: RepositoryProviderMetadata];
  'compare': [];
  'close-compare': [];
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'update:groupBy': [value: string | null]; 
}>();

// ============================================================================
// 3. CONSTANTES DE CONFIGURATION
// ============================================================================
const providerGroupOptions: { label: string; value: keyof RepositoryProviderMetadata | null }[] = [
  { label: 'Tous les fournisseurs', value: null }
  // Tu pourras ajouter ici plus tard: { label: 'Par support Self-Hosted', value: 'defaultBaseUrl' } etc.
];

// ============================================================================
// 4. COMPUTEDS
// ============================================================================
const dynamicGroupLabels = computed<Record<string | number, string>>(() => {
  return {
    'null': 'Non spécifié',
    'github.com': 'Cloud (GitHub)',
    'gitlab.com': 'Cloud (GitLab)',
    // Ajoute des labels dynamiques si tu groupes par URL par défaut
  };
});

function handleUpdateGroupBy(value: keyof RepositoryProviderMetadata | null) {
  emit('update:groupBy', value as string | null);
}
</script>