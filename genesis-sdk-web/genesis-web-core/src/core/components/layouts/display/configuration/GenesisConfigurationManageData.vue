<template>
  <div class="flex flex-col justify-end gap-3">
    <!-- Liste de configurations (avec actions internes) -->
    <GenesisConfigurationPanel
      :configurations="configurations"
      :selected-config-id="selectedConfigId"
      :filtered-configs="filteredConfigs"
      :search-query="searchQuery"
      :can-move-up="canMoveUp"
      :can-move-down="canMoveDown"
      :readonly="readonly"
      :show-toggle-visibility="showToggleVisibility"
      :tutorial-id="tutorialId"
      :include-sub-tutorial="includeSubTutorial"
      :tutorial-texts="tutorialTexts"
      @update:search-query="(val) => $emit('update:searchQuery', val)"
      @add="$emit('add')"
      @delete="(id) => $emit('delete', id)"
      @rename="(id, newName) => $emit('rename', id, newName)"
      @edit="(id) => $emit('edit', id)"
      @toggle-visibility="(id) => $emit('toggleVisibility', id)"
      @move-up="(id) => $emit('moveUp', id)"
      @move-down="(id) => $emit('moveDown', id)"
      @select-configuration="(id) => $emit('selectConfiguration', id)"
    />

    <!-- Trio Import / Export / Export All -->
    <GenesisConfigurationActions
      v-if="showImportExport"
      :can-export="canExportConfig ?? !!selectedConfigId"
      :show-import="showImport"
      :show-export="showExport"
      :show-export-all="showExportAll"
      :tutorial-id="actionsTutorialId"
      :include-sub-tutorial="includeSubTutorial"
      :tutorial-texts="actionsTutorialTexts"
      @import="$emit('import')"
      @export="$emit('export')"
      @export-all="$emit('exportAll')"
    />
  </div>
</template>

<script setup lang="ts">
import GenesisConfigurationPanel from './GenesisConfigurationPanel.vue';
import GenesisConfigurationActions from './GenesisConfigurationActions.vue';

// ============================================================================
// PROPS
// ============================================================================
/*const props = */withDefaults(defineProps<{
  // ── Panel ────────────────────────────────────────────────
  configurations: { id: string | number; name: string; isHidden?: boolean }[];
  selectedConfigId: string | number | null;
  filteredConfigs: { id: string | number; name: string; isHidden?: boolean }[];
  searchQuery: string;
  canMoveUp: boolean;
  canMoveDown: boolean;
  readonly?: boolean;
  showToggleVisibility?: boolean;

  // ── Actions Import/Export ───────────────────────────────
  /** Affiche/masque le trio de boutons (défaut : true) */
  showImportExport?: boolean;
  /** Désactive le bouton Export (défaut : !!selectedConfigId) */
  canExportConfig?: boolean;
  showImport?: boolean;
  showExport?: boolean;
  showExportAll?: boolean;

  // ── Tutoriel ────────────────────────────────────────────
  /** Ancrage pour le panel (recherche, liste, actions) */
  tutorialId?: string;
  /** Ancrage pour le trio Import/Export */
  actionsTutorialId?: string;
  includeSubTutorial?: boolean;
  /** Textes personnalisés pour le panel (recherche, liste, actions) */
  tutorialTexts?: Partial<Record<'search' | 'list' | 'actions', string>>;
  /** Textes personnalisés pour le trio (import, export, exportAll) */
  actionsTutorialTexts?: Partial<Record<'import' | 'export' | 'exportAll', string>>;
}>(), {
  readonly: false,
  showToggleVisibility: false,
  showImportExport: true,
  showImport: true,
  showExport: true,
  showExportAll: true,
  includeSubTutorial: false,
});

// ============================================================================
// EMITS
// ============================================================================
defineEmits<{
  // Panel
  'update:searchQuery': [value: string];
  add: [];
  delete: [id: string | number];
  rename: [id: string | number, newName: string];
  edit: [id: string | number];
  toggleVisibility: [id: string | number];
  moveUp: [id: string | number];
  moveDown: [id: string | number];
  selectConfiguration: [id: string | number];
  // Actions
  import: [];
  export: [];
  exportAll: [];
}>();
</script>