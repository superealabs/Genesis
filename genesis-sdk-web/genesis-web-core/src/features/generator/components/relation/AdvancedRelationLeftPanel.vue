<template>
  <div class="flex-1 min-w-0 h-full flex flex-col gap-3 overflow-y-auto custom-scrollbar pr-1">
    <!-- Segmented A / B -->
    <div>
        <GenesisSegmentedControl
            v-model="internalMode"
            :options="[
                { label: 'Ciblé', value: 'A' },
                { label: 'Masse', value: 'B' }
            ]"
            size="md"
        />
    </div>

    <!-- Mode A : 1 ancre + N cibles -->
    <template v-if="internalMode === 'A'">
        <div class="flex gap-2 items-end">
            <!-- ÉTAPE : Dropdown ancre -->
            <GenesisTutorialItem :id="subTargeted('anchor')">
                <GenesisInput
                    v-model="internalAnchor"
                    type="select"
                    :options="anchorOptions"
                    :label="isChildFirst ? 'Table Enfant' : 'Table Parent'"
                    placeholder="Sélectionner..."
                    size="lg"
                    fill-width
                    class="flex-1"
                />
            </GenesisTutorialItem>

            <!-- ÉTAPE : Segmented parent/enfant -->
            <GenesisTutorialItem :id="subTargeted('direction')">
                <GenesisSegmentedControl
                    v-model="internalDirection"
                    :options="[
                    { label: 'Parent', value: 'parent' },
                    { label: 'Enfant', value: 'enfant' }
                    ]"
                    size="lg"
                />
            </GenesisTutorialItem>
        </div>

      <!-- Recherche + liste checkbox -->
      <div class="flex gap-2">
        <GenesisTutorialItem :id="subTargeted('searchbar-targets-table')">
          <GenesisInput
            v-model="searchA"
            type="text"
            placeholder="Rechercher..."
            size="md"
            fill-width
          >
              <template #left>
                  <IconSearch :size="16" class="text-text-muted" />
              </template>
          </GenesisInput>
        </GenesisTutorialItem>
      </div>
      <div v-if="visibleCountA > 0" class="flex justify-end shrink-0">
        <button
          type="button"
          class="text-xs text-primary hover:text-primary/80 font-medium transition-colors"
          @click="toggleAllA"
        >
          {{ allSelectedA ? 'Tout désélectionner' : 'Tout sélectionner' }}
        </button>
      </div>
      <div class="shrink-0">
        <!-- ÉTAPE : GenesisEntityTable -->
        <GenesisTutorialItem :id="subTargeted('targets-table')">
          <GenesisEntityTable
            is-editable
            :items="modeAItems"
            :selected-items="targets"
            @update:selected-items="emit('update:targets', $event)"
          />
        </GenesisTutorialItem>
      </div>
    </template>

    <!-- Mode B -->
    <template v-else>
        <div class="flex gap-2 items-center">
            <!-- ÉTAPE : barre de recherche -->
            <GenesisTutorialItem :id="subMass('searchbar-mass-table')">
                <GenesisInput
                    v-model="searchB"
                    type="text"
                    placeholder="Rechercher..."
                    size="lg"
                    fill-width
                    class="flex-1"
                >
                    <template #left>
                        <IconSearch :size="16" class="text-text-muted" />
                    </template>
                </GenesisInput>
            </GenesisTutorialItem>

            <!-- ÉTAPE : segmented direction -->
            <GenesisTutorialItem :id="subMass('direction')">
                <GenesisSegmentedControl
                    v-model="internalDirection"
                    :options="[
                    { label: 'Parent', value: 'parent' },
                    { label: 'Enfant', value: 'enfant' }
                    ]"
                    size="lg"
                />
            </GenesisTutorialItem>
        </div>

        <div v-if="visibleCountB > 0" class="flex justify-end shrink-0">
          <button
            type="button"
            class="text-xs text-primary hover:text-primary/80 font-medium transition-colors"
            @click="toggleAllB"
          >
            {{ allSelectedB ? 'Tout désélectionner' : 'Tout sélectionner' }}
          </button>
        </div>

        <div class="shrink-0">
            <!-- ÉTAPE : table de masse -->
            <GenesisTutorialItem :id="subMass('mass-table')">
                <GenesisEntityTable
                is-editable
                :items="modeBItems"
                :selected-items="massSelection"
                @update:selected-items="emit('update:massSelection', $event)"
                />
            </GenesisTutorialItem>
        </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import type { TableMetadataDto } from '@genesis-labs/shared-types';

import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisEntityTable from '@genesis-labs/web-core/core/components/layouts/display/table/GenesisEntityTable.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import { useSelectVisible } from './composables/useSelectVisible';

// ✅ Tutoriel
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import { useTutorialGroup } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// ============================================================================
// PROPS & EMITS
// ============================================================================
const props = withDefaults(defineProps<{
  parents: TableMetadataDto[];
  childs: TableMetadataDto[];
  isChildFirst: boolean;
  mode: 'A' | 'B';
  anchor: string;
  targets: string[];
  massSelection: string[];

  // ✅ Tutoriel
  tutorialId?: string;
  includeSubTutorial?: boolean;
}>(), {
  includeSubTutorial: false,
});

const emit = defineEmits<{
  'update:mode': [value: 'A' | 'B'];
  'update:isChildFirst': [value: boolean];
  'update:anchor': [value: string];
  'update:targets': [value: string[]];
  'update:massSelection': [value: string[]];
  'update:visibleMassIds': [value: string[]];
}>();

// ============================================================================
// TUTORIEL — deux groupes, un par mode
// ============================================================================
/**
 * Le parent fournit UN SEUL `tutorialId` (dynamique selon son propre mode).
 * On crée DEUX groupes indépendants, chacun activé uniquement quand le mode
 * correspondant est courant :
 *   - `subTargeted` → actif si mode === 'A' (Ciblé)
 *   - `subMass`     → actif si mode === 'B' (Masse)
 *
 * Les ancrages produits :
 *   relation-advanced-left-targeted/*  (4 étapes)
 *   relation-advanced-left-mass/*      (3 étapes)
 *
 * Aucune collision : chaque groupe a son propre préfixe d'ancrage.
 */
const { sub: subTargeted } = useTutorialGroup({
  anchorId: () => (props.mode === 'A' ? props.tutorialId : undefined),
  include: () => props.includeSubTutorial,
  steps: [
    { key: 'anchor',                  text: 'Choisissez la table de départ (parent si ParentFirst, enfant si ChildFirst).' },
    { key: 'direction',               text: 'Basculez le sens de la relation : Parent → Enfant ou Enfant → Parent.' },
    { key: 'searchbar-targets-table', text: 'Filtrez les entités cibles par nom.' },
    { key: 'targets-table',           text: 'Cochez les entités cibles à relier à la table de départ.' },
  ],
});

const { sub: subMass } = useTutorialGroup({
  anchorId: () => (props.mode === 'B' ? props.tutorialId : undefined),
  include: () => props.includeSubTutorial,
  steps: [
    { key: 'searchbar-mass-table', text: 'Recherchez une table à inclure dans la sélection de masse.' },
    { key: 'direction',            text: 'Choisissez le sens : Parent → Enfant ou Enfant → Parent.' },
    { key: 'mass-table',           text: 'Cochez les tables sources dont vous voulez ajouter les relations en masse.' },
  ],
});

// ============================================================================
// ÉTAT LOCAL (recherche)
// ============================================================================
const searchA = ref('');
const searchB = ref('');

// ============================================================================
// V-MODEL MANUELS
// ============================================================================
const internalDirection = computed({
  get: (): 'parent' | 'enfant' => (props.isChildFirst ? 'enfant' : 'parent'),
  set: (v: 'parent' | 'enfant') => emit('update:isChildFirst', v === 'enfant'),
});
const internalMode = computed({
  get: () => props.mode,
  set: (v) => emit('update:mode', v),
});
const internalAnchor = computed({
  get: () => props.anchor,
  set: (v) => emit('update:anchor', v ?? ''),
});

// ============================================================================
// COMPUTEDS
// ============================================================================
function filterByQuery(source: TableMetadataDto[], query: string): TableMetadataDto[] {
  const q = query.toLowerCase().trim();
  return q ? source.filter(t => t.tableName.toLowerCase().includes(q)) : source;
}

const anchorOptions = computed(() => {
  const source = props.isChildFirst ? props.childs : props.parents;
  return source.map(t => ({
    label: `${t.tableName}${t.isView ? ' (Vue)' : ''}`,
    value: t.tableName,
  }));
});

const modeAItems = computed<TableMetadataDto[]>(() => {
  if (!props.anchor) return [];
  return filterByQuery(props.isChildFirst ? props.parents : props.childs, searchA.value);
});

const modeBItems = computed<TableMetadataDto[]>(() =>
  filterByQuery(props.isChildFirst ? props.childs : props.parents, searchB.value)
);

// ============================================================================
// SELECT ALL
// ============================================================================
const {
  visibleCount: visibleCountA,
  allSelected: allSelectedA,
  toggleAll: toggleAllA,
} = useSelectVisible(
  () => modeAItems.value.map(t => t.tableName),
  () => props.targets,
  (ids) => emit('update:targets', ids),
);

const {
  visibleCount: visibleCountB,
  allSelected: allSelectedB,
  toggleAll: toggleAllB,
} = useSelectVisible(
  () => modeBItems.value.map(t => t.tableName),
  () => props.massSelection,
  (ids) => emit('update:massSelection', ids),
);

watch(
  () => modeBItems.value.map(t => t.tableName),
  (ids) => emit('update:visibleMassIds', ids),
  { immediate: true },
);
</script>