<template>
  <div class="flex flex-col gap-4 h-full overflow-y-auto custom-scrollbar pr-1">
    <!-- Partie 1 : Modèle de relation -->
    <div class="p-4 rounded-lg border border-bg-neutral-genesis space-y-3 shrink-0">
      <div class="flex items-center justify-between">
        <h4 class="text-sm font-semibold text-text">Modèle de relation</h4>
      </div>
      <div class="flex gap-4">
        <!-- ÉTAPE : Has Form -->
        <GenesisTutorialItem :id="sub('hasForm')">
          <GenesisInput
            v-model="hasForm"
            type="boolean"
            label="Has Form"
            size="md"
            one-line
          />
        </GenesisTutorialItem>

        <!-- ÉTAPE : Mandatory -->
        <GenesisTutorialItem :id="sub('isMandatory')">
          <GenesisInput
            v-model="isMandatory"
            type="boolean"
            label="Mandatory"
            size="md"
            one-line
          />
        </GenesisTutorialItem>
      </div>
      <div class="flex gap-2 justify-end">
        <!-- ÉTAPE : Assign -->
        <GenesisTutorialItem :id="sub('assign')">
          <GenesisButton
            variant="secondary"
            size="md"
            :disabled="selectedRelations.length === 0"
            @click="$emit('assign')"
          >
            Assign
          </GenesisButton>
        </GenesisTutorialItem>

        <!-- ÉTAPE : Assign All -->
        <GenesisTutorialItem :id="sub('assignAll')">
          <GenesisButton
            variant="neutral"
            size="md"
            :disabled="relations.length === 0"
            @click="$emit('assignAll')"
          >
            Assign All
          </GenesisButton>
        </GenesisTutorialItem>
      </div>
    </div>

    <!-- Partie 2 : Liste cochable -->
    <div class="shrink-0">
      <!-- ÉTAPE : Tableau des relations -->
      <GenesisTutorialItem :id="sub('table')">
        <RelationTableViewEditable
          :relations="relations"
          :selected-relations="selectedRelations"
          :max-height="600"
          @update:selected-relations="$emit('update:selectedRelations', $event)"
        />
      </GenesisTutorialItem>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import RelationTableViewEditable from '../relation/RelationTableViewEditable.vue';

// ✅ Tutoriel
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useTutorialGroup } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// ============================================================================
// PROPS
// ============================================================================
const props = withDefaults(defineProps<{
  hasForm: boolean;
  isMandatory: boolean;
  relations: RelationParameter[];
  selectedRelations: string[];

  // ✅ Tutoriel (optionnel : composant transparent si absent)
  tutorialId?: string;
  includeSubTutorial?: boolean;
  tutorialTexts?: Partial<Record<'hasForm' | 'isMandatory' | 'assign' | 'assignAll' | 'table', string>>;
}>(), {
  includeSubTutorial: false,
});

// ============================================================================
// EMITS
// ============================================================================
const emit = defineEmits<{
  'update:hasForm': [value: boolean];
  'update:isMandatory': [value: boolean];
  'update:selectedRelations': [value: string[]];
  assign: [];
  assignAll: [];
}>();

// ============================================================================
// V-MODEL
// ============================================================================
const hasForm = computed({
  get: () => props.hasForm,
  set: (v) => emit('update:hasForm', v),
});
const isMandatory = computed({
  get: () => props.isMandatory,
  set: (v) => emit('update:isMandatory', v),
});

// ============================================================================
// TUTORIEL
// ============================================================================
/** Textes par défaut, surchargés individuellement par `tutorialTexts`. */
const DEFAULT_STEPS = {
  hasForm:     'Activez « Has Form » pour générer un formulaire associé à la relation.',
  isMandatory: 'Rendez la relation obligatoire (contrainte côté UI générée).',
  assign:      'Applique le modèle courant (Has Form / Mandatory) aux relations sélectionnées.',
  assignAll:   'Applique le modèle courant à toutes les relations configurées.',
  table:       'Sélectionnez les relations à modifier, ou supprimez-les via les actions de ligne.',
} as const;

const { sub } = useTutorialGroup({
  anchorId: () => props.tutorialId,
  include: () => props.includeSubTutorial,
  steps: [
    { key: 'hasForm',     text: props.tutorialTexts?.hasForm     ?? DEFAULT_STEPS.hasForm     },
    { key: 'isMandatory', text: props.tutorialTexts?.isMandatory ?? DEFAULT_STEPS.isMandatory },
    { key: 'assign',      text: props.tutorialTexts?.assign      ?? DEFAULT_STEPS.assign      },
    { key: 'assignAll',   text: props.tutorialTexts?.assignAll   ?? DEFAULT_STEPS.assignAll   },
    { key: 'table',       text: props.tutorialTexts?.table       ?? DEFAULT_STEPS.table       },
  ],
});
</script>