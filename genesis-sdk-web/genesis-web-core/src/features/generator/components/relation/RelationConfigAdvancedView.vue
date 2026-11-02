<template>
  <div class="flex gap-6 flex-1 min-h-0 overflow-hidden px-6 pb-6">
    <!-- Colonne gauche -->
    <AdvancedRelationLeftPanel
        class="w-1/5 min-w-0 scroll-auto h-full"
        v-model:mode="mode"
        v-model:mass-selection="massSelection"
        :is-child-first="isChildFirst"
        :anchor="anchor"
        :targets="targets"
        :parents="tablesParents"
        :childs="tablesChilds"
        @update:is-child-first="setDirection"
        @update:anchor="setAnchor"
        @update:targets="setTargets"
    />

    <!-- Colonne centre -->
    <AdvancedRelationCenterActions
        class="max-w-2/5 scroll-auto h-full"
        :can-add="canAdd"
        :can-add-all="canAddAll"
        :can-remove="canRemove"
        :can-remove-all="canRemoveAll"
        @add="handleAdd"
        @add-all="handleAddAll"
        @remove="handleRemove"
        @remove-all="handleRemoveAll"
    />

    <!-- Colonne droite -->
    <AdvancedRelationRightPanel
        class="w-2/5 min-w-0 scroll-auto h-full"
        v-model:has-form="hasForm"
        v-model:is-mandatory="isMandatory"
        :relations="getRelations"
        :selected-relations="selectedRelations"
        @update:selected-relations="selectedRelations = $event"
        @assign="handleAssign"
        @assign-all="handleAssignAll"
    />

    <!-- Warning des doublons (custom slot) -->
    <WarningPopup
      v-if="duplicateWarning.show"
      title="Relations déjà existantes"
      :message="`${duplicateWarning.duplicates.length} relation(s) existent déjà et seront ignorées.`"
      confirm-label="Continuer"
      cancel-label="Annuler"
      variant="warning"
      size="lg"
      @confirm="resolveDuplicateWarning(true)"
      @cancel="resolveDuplicateWarning(false)"
    >
      <template #custom>
        <GenesisContainer
          :min-height="100"
          :max-height="300"
          :is-empty="duplicateWarning.duplicates.length === 0"
          empty-text="Aucune relation."
        >
          <div class="flex flex-col gap-1 p-2">
            <div
              v-for="dup in duplicateWarning.duplicates"
              :key="`${dup.parentTable}::${dup.childTable}`"
              class="text-sm text-text-muted font-mono"
            >
              {{ dup.parentTable }} → {{ dup.childTable }}
            </div>
          </div>
        </GenesisContainer>
      </template>
    </WarningPopup>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useRelationBuilder } from './composables/useRelationBuilder.ts';

import AdvancedRelationLeftPanel from './AdvancedRelationLeftPanel.vue';
import AdvancedRelationCenterActions from './AdvancedRelationCenterActions.vue';
import AdvancedRelationRightPanel from './AdvancedRelationRightPanel.vue';

import WarningPopup from '@genesis-labs/web-core/core/components/layouts/Popup/WarningPopup.vue';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';

// ============================================================================
// PROPS
// ============================================================================
const props = withDefaults(defineProps<{
  /** Demander confirmation quand des doublons sont détectés */
  confirmOnDuplicate?: boolean;
}>(), {
  confirmOnDuplicate: true,
});

// ============================================================================
// COMPOSABLES & STORE
// ============================================================================
const {
  tablesParents,
  tablesChilds,
  getRelations,
  addRelation,
  removeRelation,
} = useGenerator();

// État PARTAGÉ avec RelationConfigSimpleView (sens, ancre, cibles + chargements API)
const {
  isChildFirst,
  anchor,
  targets,
  setDirection,
  setAnchor,
  setTargets,
  resetSelection,
} = useRelationBuilder();

// ============================================================================
// ÉTAT LOCAL (propre à Advanced)
// ============================================================================

// ── Mode & sélection de masse ────────────────────────────────────────────
const mode = ref<'A' | 'B'>('A');

/** Mode B : tables cochées (parents si ParentFirst, enfants si ChildFirst) */
const massSelection = ref<string[]>([]);

// Mode B croise avec les listes COMPLÈTES : on vide l'ancre (→ rechargement complet)
watch(mode, (m) => {
  massSelection.value = [];
  if (m === 'B') resetSelection();
});
// Changement de sens : la sélection de masse n'a plus de sens
watch(isChildFirst, () => { massSelection.value = []; });

// ── Modèle de relation ──────────────────────────────────────────────────
const hasForm = ref(false);
const isMandatory = ref(false);

// ── Sélection dans la liste de droite ────────────────────────────────────
const selectedRelations = ref<string[]>([]);

// ── Warning des doublons ────────────────────────────────────────────────
const duplicateWarning = ref<{
  show: boolean;
  duplicates: RelationParameter[];
  resolve?: (ok: boolean) => void;
}>({
  show: false,
  duplicates: [],
});

// ============================================================================
// COMPUTEDS DE DISPONIBILITÉ DES BOUTONS
// ============================================================================

/** "Add" actif si des entités sont sélectionnées à gauche. */
const canAdd = computed(() => {
  if (mode.value === 'A') return !!anchor.value && targets.value.length > 0;
  return massSelection.value.length > 0;
});

/** "Add All" : mode A = ancre choisie + liste liée non vide ; mode B = comme "Add". */
const canAddAll = computed(() => {
  if (mode.value === 'A') {
    const linked = isChildFirst.value ? tablesParents.value : tablesChilds.value;
    return !!anchor.value && linked.length > 0;
  }
  return canAdd.value;
});

/** "Remove" actif si au moins une relation cochée à droite. */
const canRemove = computed(() => selectedRelations.value.length > 0);

/** "Remove All" actif si au moins une relation existe. */
const canRemoveAll = computed(() => getRelations.value.length > 0);

// ============================================================================
// LOGIQUE MÉTIER
// ============================================================================

/**
 * Construit la liste des relations à ajouter.
 * @param all true = "Add All" (tous les enfants/parents possibles, pas seulement cochés)
 */
function computeRelationsToAdd(all: boolean): RelationParameter[] {
  const toRelation = (parentTable: string, childTable: string): RelationParameter => ({
    parentTable,
    childTable,
    mandatory: isMandatory.value,
    hasForm: hasForm.value,
  });

  // ── MODE A : 1 parent + N enfants (ou 1 enfant + N parents) ─────────
  if (mode.value === 'A') {
    if (!anchor.value) return [];

    if (!isChildFirst.value) {
      const childNames = all ? tablesChilds.value.map(c => c.tableName) : targets.value;
      return childNames.map(child => toRelation(anchor.value, child));
    }
    const parentNames = all ? tablesParents.value.map(p => p.tableName) : targets.value;
    return parentNames.map(parent => toRelation(parent, anchor.value));
  }

  // ── MODE B : N parents × tous les enfants (ou N enfants × tous les parents)
  if (!isChildFirst.value) {
    return massSelection.value.flatMap(parent =>
      tablesChilds.value.map(child => toRelation(parent, child.tableName))
    );
  }
  return massSelection.value.flatMap(child =>
    tablesParents.value.map(parent => toRelation(parent.tableName, child))
  );
}

/**
 * Ajoute les relations, en filtrant les doublons.
 * Si `confirmOnDuplicate` est activé et qu'il y a des doublons,
 * affiche un WarningPopup et attend la confirmation.
 */
async function addRelations(candidates: RelationParameter[]): Promise<void> {
  if (candidates.length === 0) return;

  // Déduplique en interne (au cas où la même paire apparaîtrait plusieurs fois)
  const seen = new Set<string>();
  const uniqueCandidates = candidates.filter(r => {
    const key = `${r.parentTable}::${r.childTable}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });

  const existingKeys = new Set(
    getRelations.value.map(r => `${r.parentTable}::${r.childTable}`)
  );

  const duplicates = uniqueCandidates.filter(r =>
    existingKeys.has(`${r.parentTable}::${r.childTable}`)
  );
  const newOnes = uniqueCandidates.filter(r =>
    !existingKeys.has(`${r.parentTable}::${r.childTable}`)
  );

  // Confirmation utilisateur si doublons
  if (duplicates.length > 0 && props.confirmOnDuplicate) {
    const ok = await new Promise<boolean>((resolve) => {
      duplicateWarning.value = { show: true, duplicates, resolve };
    });
    if (!ok) return;
  }

  // Ajout effectif (addRelation du store est censé être idempotent,
  // mais on a déjà filtré les doublons donc pas de rejet attendu)
  newOnes.forEach(r => addRelation(r));
}

// ============================================================================
// ACTIONS UTILISATEUR
// ============================================================================

async function handleAdd() {
  const candidates = computeRelationsToAdd(false);
  await addRelations(candidates);
}

async function handleAddAll() {
  const candidates = computeRelationsToAdd(true);
  await addRelations(candidates);
}

function handleRemove() {
  const toRemove = new Set(selectedRelations.value);

  // Itère à l'envers pour éviter les décalages d'index pendant le splice
  for (let i = getRelations.value.length - 1; i >= 0; i--) {
    const rel = getRelations.value[i];
    if (toRemove.has(`${rel.parentTable}::${rel.childTable}`)) {
      removeRelation(i);
    }
  }

  selectedRelations.value = [];
}

function handleRemoveAll() {
  for (let i = getRelations.value.length - 1; i >= 0; i--) {
    removeRelation(i);
  }
  selectedRelations.value = [];
}

function handleAssign() {
  const toAssign = new Set(selectedRelations.value);
  getRelations.value.forEach(rel => {
    const key = `${rel.parentTable}::${rel.childTable}`;
    if (toAssign.has(key)) {
      rel.hasForm = hasForm.value;
      rel.mandatory = isMandatory.value;
    }
  });
}

function handleAssignAll() {
  getRelations.value.forEach(rel => {
    rel.hasForm = hasForm.value;
    rel.mandatory = isMandatory.value;
  });
}

// ============================================================================
// WARNING DES DOUBLONS
// ============================================================================

function resolveDuplicateWarning(ok: boolean) {
  duplicateWarning.value.resolve?.(ok);
  duplicateWarning.value = { show: false, duplicates: [] };
}
</script>