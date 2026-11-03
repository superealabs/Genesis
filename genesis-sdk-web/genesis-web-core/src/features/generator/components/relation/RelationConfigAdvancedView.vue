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
        @update:visible-mass-ids="visibleMassIds = $event"
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
      :message="duplicateMessage"
      confirm-label="Mettre à jour"
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

const duplicateMessage = computed(() => {
  const yesNo = (v: boolean) => (v ? 'oui' : 'non');
  return `${duplicateWarning.value.duplicates.length} relation(s) existent déjà : `
    + `Has Form et Mandatory seront mis à jour selon le modèle `
    + `(Has Form : ${yesNo(hasForm.value)}, Mandatory : ${yesNo(isMandatory.value)}).`;
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
  getChildrenOf, getParentsOf,
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
/** Mode B : éléments visibles (après recherche), cochés ou non — pour "Add All" */
const visibleMassIds = ref<string[]>([]);

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

  return visibleMassIds.value.length > 0;
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
async function computeRelationsToAdd(all: boolean): Promise<RelationParameter[]> {
  const toRelation = (parentTable: string, childTable: string): RelationParameter => ({
    parentTable, childTable, mandatory: isMandatory.value, hasForm: hasForm.value,
  });

  // ── MODE A : inchangé (la liste est déjà celle de l'ancre) ──
  if (mode.value === 'A') {
    if (!anchor.value) return [];
    if (!isChildFirst.value) {
      const childNames = all ? tablesChilds.value.map(c => c.tableName) : targets.value;
      return childNames.map(child => toRelation(anchor.value, child));
    }
    const parentNames = all ? tablesParents.value.map(p => p.tableName) : targets.value;
    return parentNames.map(parent => toRelation(parent, anchor.value));
  }

  // ── MODE B : chaque table a ses PROPRES enfants/parents (récupérés via l'API) ──
  // Add = tables cochées ; Add All = toutes les tables visibles.
  // Appels SÉQUENTIELS : getChildrenOf/getParentsOf s'appuient sur un état partagé ; lancés en parallèle,
  // tous les appels lisent le résultat du dernier et un seul parent "gagne".
  // On fige le sens et la liste avant les `await` (la sélection peut changer pendant les appels).
  const childFirst = isChildFirst.value;
  const sources = [...(all ? visibleMassIds.value : massSelection.value)];
  const result: RelationParameter[] = [];

  for (const name of sources) {
    if (!childFirst) {
      const children = await getChildrenOf(name);
      children.forEach(c => result.push(toRelation(name, c.tableName)));
    } else {
      const parents = await getParentsOf(name);
      parents.forEach(p => result.push(toRelation(p.tableName, name)));
    }
  }
  return result;
}


const relationKey = (r: RelationParameter) => `${r.parentTable}::${r.childTable}`;

/** Applique le modèle du panneau de droite (Has Form / Mandatory) à une relation. */
function applyModel(rel: RelationParameter) {
  rel.hasForm = hasForm.value;
  rel.mandatory = isMandatory.value;
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

  const existingByKey = new Map(getRelations.value.map(r => [relationKey(r), r]));

  const newOnes = uniqueCandidates.filter(r => !existingByKey.has(relationKey(r)));

  // Doublons dont les valeurs diffèrent du modèle → à mettre à jour (identiques : rien à faire)
  const toUpdate = uniqueCandidates
    .map(r => existingByKey.get(relationKey(r)))
    .filter((existing): existing is RelationParameter =>
      !!existing &&
      (!!existing.hasForm !== hasForm.value || !!existing.mandatory !== isMandatory.value)
    );

  // Confirmation utilisateur avant d'écraser
  if (toUpdate.length > 0 && props.confirmOnDuplicate) {
    const ok = await new Promise<boolean>((resolve) => {
      duplicateWarning.value = { show: true, duplicates: toUpdate, resolve };
    });
    if (!ok) return;
  }

  // MAJ DE LA VALEUR DES DOUBLONS
  toUpdate.forEach(applyModel);
  newOnes.forEach(r => addRelation(r));
}

// ============================================================================
// ACTIONS UTILISATEUR
// ============================================================================

async function handleAdd()    { await addRelations(await computeRelationsToAdd(false)); }
async function handleAddAll() { await addRelations(await computeRelationsToAdd(true)); }

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