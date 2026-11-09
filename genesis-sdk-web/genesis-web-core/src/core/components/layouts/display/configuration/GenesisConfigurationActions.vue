<template>
  <div class="flex gap-2 justify-end">
    <GenesisTutorialItem :id="sub('import')">
      <GenesisButtonIcon
        v-if="showImport"
        is-perspective
        title="Importer un fichier de configuration"
        @click="$emit('import')"
      >
        <IconUpload />
      </GenesisButtonIcon>
    </GenesisTutorialItem>

    <GenesisTutorialItem :id="sub('export')">
      <GenesisButtonIcon
        v-if="showExport"
        is-perspective
        title="Exporter la configuration sélectionnée"
        :disabled="!canExport"
        @click="$emit('export')"
      >
        <IconSave />
      </GenesisButtonIcon>
    </GenesisTutorialItem>

    <GenesisTutorialItem :id="sub('exportAll')">
      <GenesisButtonIcon
        v-if="showExportAll"
        is-perspective
        title="Exporter toutes les configurations"
        @click="$emit('exportAll')"
      >
        <IconPackage />
      </GenesisButtonIcon>
    </GenesisTutorialItem>
  </div>
</template>

<script setup lang="ts">
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconUpload from '@genesis-labs/web-core/core/components/ui/icons/IconUpload.vue';
import IconSave from '@genesis-labs/web-core/core/components/ui/icons/IconSave.vue';
import IconPackage from '@genesis-labs/web-core/core/components/ui/icons/IconPackage.vue';

// ✅ Tutoriel
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useTutorialGroup } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// ============================================================================
// PROPS
// ============================================================================
const props = withDefaults(defineProps<{
  /** Désactive le bouton Export si false (ex : aucune config sélectionnée) */
  canExport?: boolean;
  /** Visibilité de chaque bouton (défaut : true) */
  showImport?: boolean;
  showExport?: boolean;
  showExportAll?: boolean;

  // ✅ Tutoriel
  tutorialId?: string;
  includeSubTutorial?: boolean;
  tutorialTexts?: Partial<Record<'import' | 'export' | 'exportAll', string>>;
}>(), {
  canExport: true,
  showImport: true,
  showExport: true,
  showExportAll: true,
  includeSubTutorial: false,
});

// ============================================================================
// EMITS
// ============================================================================
defineEmits<{
  import: [];
  export: [];
  exportAll: [];
}>();

// ============================================================================
// TUTORIEL
// ============================================================================
const DEFAULT_STEPS = {
  import:    'Importez une ou plusieurs configurations depuis un fichier JSON.',
  export:    'Exportez la configuration actuellement sélectionnée dans un fichier JSON.',
  exportAll: 'Exportez toutes les configurations en une seule fois.',
} as const;

const { sub } = useTutorialGroup({
  anchorId: () => props.tutorialId,
  include: () => props.includeSubTutorial,
  steps: [
    { key: 'import',    text: props.tutorialTexts?.import    ?? DEFAULT_STEPS.import    },
    { key: 'export',    text: props.tutorialTexts?.export    ?? DEFAULT_STEPS.export    },
    { key: 'exportAll', text: props.tutorialTexts?.exportAll ?? DEFAULT_STEPS.exportAll },
  ],
});
</script>