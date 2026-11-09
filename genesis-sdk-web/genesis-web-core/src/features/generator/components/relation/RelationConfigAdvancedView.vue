<template>
  <div class="flex gap-6 flex-1 min-h-0 overflow-hidden px-6 pb-6">
    <!-- Colonne gauche : wrapper tutorial englobant (ancre dynamique selon le mode) -->
    <GenesisTutorialItem :id="leftPanelTutorialId">
      <AdvancedRelationLeftPanel
          class="w-1/5 min-w-0 scroll-auto h-full"
          v-model:mode="mode"
          v-model:mass-selection="massSelection"
          :is-child-first="isChildFirst"
          :anchor="anchor"
          :targets="targets"
          :parents="tablesParents"
          :childs="tablesChilds"
          :tutorial-id="leftPanelTutorialId"
          :include-sub-tutorial="true"
          @update:is-child-first="setDirection"
          @update:anchor="setAnchor"
          @update:targets="setTargets"
          @update:visible-mass-ids="visibleMassIds = $event"
      />
    </GenesisTutorialItem>

    <!-- Colonne centre -->
    <GenesisTutorialItem id="relation-advanced-center">
      <AdvancedRelationCenterActions
          class="max-w-2/5 scroll-auto h-full"
          :can-add="canAdd"
          :can-add-all="canAddAll"
          :can-remove="canRemove"
          :can-remove-all="canRemoveAll"
          tutorial-id="relation-advanced-center"
          :include-sub-tutorial="true"
          @add="handleAdd"
          @add-all="handleAddAll"
          @remove="handleRemove"
          @remove-all="handleRemoveAll"
      />
    </GenesisTutorialItem>

    <!-- Colonne droite -->
    <GenesisTutorialItem id="relation-advanced-right">
      <AdvancedRelationRightPanel
          class="w-2/5 min-w-0 scroll-auto h-full"
          v-model:has-form="hasForm"
          v-model:is-mandatory="isMandatory"
          :relations="getRelations"
          :selected-relations="selectedRelations"
          tutorial-id="relation-advanced-right"
          :include-sub-tutorial="true"
          @update:selected-relations="selectedRelations = $event"
          @assign="handleAssign"
          @assign-all="handleAssignAll"
      />
    </GenesisTutorialItem>

    <!-- Warning des doublons -->
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
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

import AdvancedRelationLeftPanel from './AdvancedRelationLeftPanel.vue';
import AdvancedRelationCenterActions from './AdvancedRelationCenterActions.vue';
import AdvancedRelationRightPanel from './AdvancedRelationRightPanel.vue';

import WarningPopup from '@genesis-labs/web-core/core/components/layouts/Popup/WarningPopup.vue';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';

// ============================================================================
// PROPS
// ============================================================================
const props = withDefaults(defineProps<{
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
// ÉTAT LOCAL
// ============================================================================
const mode = ref<'A' | 'B'>('A');
const massSelection = ref<string[]>([]);
const visibleMassIds = ref<string[]>([]);

watch(mode, (m) => {
  massSelection.value = [];
  if (m === 'B') resetSelection();
});
watch(isChildFirst, () => { massSelection.value = []; });

const hasForm = ref(false);
const isMandatory = ref(false);
const selectedRelations = ref<string[]>([]);

const duplicateWarning = ref<{
  show: boolean;
  duplicates: RelationParameter[];
  resolve?: (ok: boolean) => void;
}>({
  show: false,
  duplicates: [],
});

// ============================================================================
// TUTORIEL
// ============================================================================
/**
 * Sections par mode :
 *  - 'targeted' = Mode A (Ciblé)
 *  - 'mass'     = Mode B (Masse)
 * Chaque section a SON PROPRE ancrage left-panel, pour éviter les collisions
 * de useTutorialGroup (un groupe fixe par ancrage).
 */
const tutorial = useGenesisTutorial('relation-config-advanced', {
  'targeted': [
    {
      id: 'relation-advanced-left-targeted',
      text: 'Choisissez la table de départ, le sens de la relation, puis cochez les entités cibles.',
    },
    {
      id: 'relation-advanced-center',
      text: 'Actions disponibles pour ajouter ou retirer des relations.',
    },
    {
      id: 'relation-advanced-right',
      text: 'Modèle de relation et liste des relations configurées.',
    },
  ],
  'mass': [
    {
      id: 'relation-advanced-left-mass',
      text: 'Sélectionnez en masse les entités de départ, puis ajoutez leurs relations.',
    },
    {
      id: 'relation-advanced-center',
      text: 'Actions disponibles pour ajouter ou retirer des relations.',
    },
    {
      id: 'relation-advanced-right',
      text: 'Modèle de relation et liste des relations configurées.',
    },
  ],
});

/** Ancrage du wrapper panel gauche, dépendant du mode courant */
const leftPanelTutorialId = computed(() =>
  mode.value === 'A'
    ? 'relation-advanced-left-targeted'
    : 'relation-advanced-left-mass'
);

/** Section de tutoriel correspondant au mode courant */
const currentSection = computed(() =>
  mode.value === 'A' ? 'targeted' : 'mass'
);

// ✅ Relance auto du tutoriel au basculement Ciblé <-> Masse
// (uniquement si un tutoriel est déjà actif sur cet écran — sinon no-op silencieux)
watch(mode, () => {
  if (tutorial && 'restart' in tutorial) {
    // On relance la section du nouveau mode ; si le tutoriel global est éteint,
    // restart() le rallume — c'est voulu.
    // tutorial.restart(currentSection.value);
  }
});

/**
 * Contrat commun exposé au parent (`RelationConfigView`) — déclenche le tutoriel
 * de la section correspondant au mode courant.
 */
defineExpose({
  restart: () => tutorial.restart(currentSection.value),
});

// ============================================================================
// COMPUTEDS DE DISPONIBILITÉ
// ============================================================================
const canAdd = computed(() => {
  if (mode.value === 'A') return !!anchor.value && targets.value.length > 0;
  return massSelection.value.length > 0;
});

const canAddAll = computed(() => {
  if (mode.value === 'A') {
    const linked = isChildFirst.value ? tablesParents.value : tablesChilds.value;
    return !!anchor.value && linked.length > 0;
  }
  return visibleMassIds.value.length > 0;
});

const canRemove = computed(() => selectedRelations.value.length > 0);
const canRemoveAll = computed(() => getRelations.value.length > 0);

// ============================================================================
// LOGIQUE MÉTIER (inchangée)
// ============================================================================
async function computeRelationsToAdd(all: boolean): Promise<RelationParameter[]> {
  const toRelation = (parentTable: string, childTable: string): RelationParameter => ({
    parentTable, childTable, mandatory: isMandatory.value, hasForm: hasForm.value,
  });

  if (mode.value === 'A') {
    if (!anchor.value) return [];
    if (!isChildFirst.value) {
      const childNames = all ? tablesChilds.value.map(c => c.tableName) : targets.value;
      return childNames.map(child => toRelation(anchor.value, child));
    }
    const parentNames = all ? tablesParents.value.map(p => p.tableName) : targets.value;
    return parentNames.map(parent => toRelation(parent, anchor.value));
  }

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

function applyModel(rel: RelationParameter) {
  rel.hasForm = hasForm.value;
  rel.mandatory = isMandatory.value;
}

async function addRelations(candidates: RelationParameter[]): Promise<void> {
  if (candidates.length === 0) return;

  const seen = new Set<string>();
  const uniqueCandidates = candidates.filter(r => {
    const key = `${r.parentTable}::${r.childTable}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });

  const existingByKey = new Map(getRelations.value.map(r => [relationKey(r), r]));
  const newOnes = uniqueCandidates.filter(r => !existingByKey.has(relationKey(r)));

  const toUpdate = uniqueCandidates
    .map(r => existingByKey.get(relationKey(r)))
    .filter((existing): existing is RelationParameter =>
      !!existing &&
      (!!existing.hasForm !== hasForm.value || !!existing.mandatory !== isMandatory.value)
    );

  if (toUpdate.length > 0 && props.confirmOnDuplicate) {
    const ok = await new Promise<boolean>((resolve) => {
      duplicateWarning.value = { show: true, duplicates: toUpdate, resolve };
    });
    if (!ok) return;
  }

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

function resolveDuplicateWarning(ok: boolean) {
  duplicateWarning.value.resolve?.(ok);
  duplicateWarning.value = { show: false, duplicates: [] };
}
</script>