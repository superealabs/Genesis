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
    <!-- ✅ NOUVEAU : Dropdown de regroupement (même s'il n'y a qu'une option pour l'instant) -->
    <template #custom-menu>
      <GenesisDropdown
        v-if="databaseGroupOptions && databaseGroupOptions.length > 0"
        triggerVariant="secondary"
        trigger-size="lg"
      >
        <template #triggerIcon>
          <IconCategory />
        </template>
        
        <template #default="{ close }">
          <div class="flex flex-col gap-1 p-1 min-w-[180px]">
            <button
              v-for="opt in databaseGroupOptions"
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

    <DatabaseList
      :engines="engines"
      :selectedId="selectedId"
      :display="displayMode"
      :databaseSlots="databaseSlots"
      :group-by="groupBy"
      :group-options="databaseGroupOptions"
      @select="(engine, ev) => $emit('select', engine, ev)"
      @info="(engine: DatabaseEngineDto) => $emit('info', engine)" 
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
import type { DatabaseEngineDto } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types.ts';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { PanelEditorDefinition } from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

import DatabaseList from './DatabaseList.vue';
import DatabaseDetails from './DatabaseDetails.vue';
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
  detailEngine?: DatabaseEngineDto | null;
  
  // ✅ NOUVEAU : Props de regroupement
  groupBy?: keyof DatabaseEngineDto | null;
  
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
  'update:displayMode': [mode: DisplayMode];
  'update:mode': [mode: 'selection' | 'compare'];
  'select-replace': [slotId: string | number];
  'close-replace': [];
  'select': [engine: DatabaseEngineDto, event?: MouseEvent];
  'info': [engine: DatabaseEngineDto];
  'compare': [];
  'close-compare': [];
  // ✅ NOUVEAU
  'update:groupBy': [value: string | null];
}>();

// ============================================================================
// 3. CONSTANTES & HANDLERS
// ============================================================================
const databaseGroupOptions: { label: string; value: keyof DatabaseEngineDto | null }[] = [
  { label: 'Tous les moteurs', value: null }
];

function handleUpdateGroupBy(value: keyof DatabaseEngineDto | null) {
  emit('update:groupBy', value as string | null);
}
</script>