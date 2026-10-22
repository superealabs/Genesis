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
    <!-- ✅ NOUVEAU : Dropdown de regroupement -->
    <template #custom-menu>
      <GenesisDropdown
        v-if="frontendGroupOptions && frontendGroupOptions.length > 0"
        triggerVariant="secondary"
        trigger-size="lg"
      >
        <template #triggerIcon>
          <IconCategory />
        </template>
        
        <template #default="{ close }">
          <div class="flex flex-col gap-1 p-1 min-w-[180px]">
            <button
              v-for="opt in frontendGroupOptions"
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

    <FrontendList
      :frontends="frontends"
      :selectedId="selectedId"
      :display="displayMode"
      :framework-slots="frontendFrameworkSlots"
      :group-by="groupBy"
      :group-options="frontendGroupOptions"
      @select="(fw, ev) => $emit('select', fw, ev)"
      @info="(fw) => $emit('info', fw)"
      @update:group-by="handleUpdateGroupBy"
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
import type { FrontendFramework } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

import FrontendList from './FrontendList.vue';
import FrontendDetails from './FrontendDetails.vue';
import GenesisCollectionLayout from '@genesis-labs/web-core/core/components/layouts/GenesisCollectionLayout.vue';
import GenesisItemComparator from '@genesis-labs/web-core/core/components/layouts/GenesisItemComparator.vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';

// ✅ NOUVEAUX IMPORTS
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import IconCategory from '@genesis-labs/web-core/core/components/ui/icons/IconCategory.vue';
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue';

// ============================================================================
// 1. INTERFACE DES PROPS
// ============================================================================
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
  detailFramework?: FrontendFramework | null;
  
  // ✅ NOUVEAU : Props de regroupement
  groupBy?: keyof FrontendFramework | null;
  
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
  groupBy: null,
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
  'close-detail': [];
  'update:searchValue': [value: string];
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [framework: FrontendFramework, event?: MouseEvent];
  'info': [framework: FrontendFramework];
  'compare': [];
  'close-compare': [];
  // ✅ NOUVEAU
  'update:groupBy': [value: string | null];
}>();

// ============================================================================
// 3. CONSTANTES & HANDLERS
// ============================================================================
const frontendGroupOptions: { label: string; value: keyof FrontendFramework | null }[] = [
  { label: 'Tous les frameworks', value: null }
];

function handleUpdateGroupBy(value: keyof FrontendFramework | null) {
  emit('update:groupBy', value as string | null);
}
</script>