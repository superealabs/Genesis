<template>
  <div class="flex flex-col gap-3 w-64 shrink-0 justify-center">
    <!-- ÉTAPE : Add All -->
    <GenesisTutorialItem :id="sub('addAll')">
      <GenesisButton isPerspective :size="'lg'" variant="accent" :disabled="!canAddAll" @click="$emit('addAll')">
        <template #leftIcon><IconChevronsRight /></template>
        Add All
      </GenesisButton>
    </GenesisTutorialItem>

    <!-- ÉTAPE : Add -->
    <GenesisTutorialItem :id="sub('add')">
      <GenesisButton isPerspective :size="'lg'" variant="secondary" :disabled="!canAdd" @click="$emit('add')">
        <template #leftIcon><IconChevronRight /></template>
        Add
      </GenesisButton>
    </GenesisTutorialItem>

    <!-- ÉTAPE : Remove -->
    <GenesisTutorialItem :id="sub('remove')">
      <GenesisButton isPerspective :size="'lg'" :disabled="!canRemove" @click="$emit('remove')">
        <template #leftIcon><IconChevronLeft /></template>
        Remove
      </GenesisButton>
    </GenesisTutorialItem>

    <!-- ÉTAPE : Remove All -->
    <GenesisTutorialItem :id="sub('removeAll')">
      <GenesisButton isPerspective :size="'lg'" variant="neutral" :disabled="!canRemoveAll" @click="$emit('removeAll')">
        <template #leftIcon><IconChevronsLeft /></template>
        Remove All
      </GenesisButton>
    </GenesisTutorialItem>
  </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconChevronLeft from '@genesis-labs/web-core/core/components/ui/icons/IconChevronLeft.vue';
import IconChevronRight from '@genesis-labs/web-core/core/components/ui/icons/IconChevronRight.vue';
import IconChevronsLeft from '@genesis-labs/web-core/core/components/ui/icons/IconChevronsLeft.vue';
import IconChevronsRight from '@genesis-labs/web-core/core/components/ui/icons/IconChevronsRight.vue';

// ✅ Tutoriel
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useTutorialGroup } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// ============================================================================
// PROPS
// ============================================================================
const props = withDefaults(defineProps<{
  canAdd: boolean;
  canAddAll: boolean;
  canRemove: boolean;
  canRemoveAll: boolean;

  // ✅ Tutoriel (optionnel : composant transparent si absent)
  tutorialId?: string;
  includeSubTutorial?: boolean;
  tutorialTexts?: Partial<Record<'addAll' | 'add' | 'remove' | 'removeAll', string>>;
}>(), {
  includeSubTutorial: false,
});

// ============================================================================
// EMITS
// ============================================================================
defineEmits<{
  add: [];
  addAll: [];
  remove: [];
  removeAll: [];
}>();

// ============================================================================
// TUTORIEL
// ============================================================================
/** Textes par défaut, surchargés individuellement par `tutorialTexts`. */
const DEFAULT_ACTION_STEPS = {
  addAll:    'Ajoute toutes les entités visibles dans la colonne de gauche.',
  add:       'Ajoute les entités sélectionnées dans la colonne de gauche.',
  remove:    'Retire les relations sélectionnées dans la colonne de droite.',
  removeAll: 'Retire toutes les relations visibles dans la colonne de droite.',
} as const;

const { sub } = useTutorialGroup({
  anchorId: () => props.tutorialId,
  include: () => props.includeSubTutorial,
  steps: [
    { key: 'addAll',    text: props.tutorialTexts?.addAll    ?? DEFAULT_ACTION_STEPS.addAll    },
    { key: 'add',       text: props.tutorialTexts?.add       ?? DEFAULT_ACTION_STEPS.add       },
    { key: 'remove',    text: props.tutorialTexts?.remove    ?? DEFAULT_ACTION_STEPS.remove    },
    { key: 'removeAll', text: props.tutorialTexts?.removeAll ?? DEFAULT_ACTION_STEPS.removeAll },
  ],
});
</script>