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
            <GenesisSegmentedControl
                v-model="internalDirection"
                :options="[
                { label: 'Parent', value: 'parent' },
                { label: 'Enfant', value: 'enfant' }
                ]"
                size="lg"
            />
        </div>

      <!-- Recherche + liste checkbox des enfants/parents liés à l'ancre -->
      <div class="flex gap-2">
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
        <GenesisEntityTable
          is-editable
          :items="modeAItems"
          :selected-items="targets"
          @update:selected-items="emit('update:targets', $event)"
        />
      </div>
    </template>

    <!-- Mode B -->
    <template v-else>
        <div class="flex gap-2 items-center">
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
            <GenesisSegmentedControl
                v-model="internalDirection"
                :options="[
                { label: 'Parent', value: 'parent' },
                { label: 'Enfant', value: 'enfant' }
                ]"
                size="lg"
            />
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
            <GenesisEntityTable
            is-editable
            :items="modeBItems"
            :selected-items="massSelection"
            @update:selected-items="emit('update:massSelection', $event)"
            />
        </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { TableMetadataDto } from '@genesis-labs/shared-types';

import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisEntityTable from '@genesis-labs/web-core/core/components/layouts/display/table/GenesisEntityTable.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import { useSelectVisible } from './composables/useSelectVisible';

// ============================================================================
// PROPS & EMITS
// ============================================================================
const props = defineProps<{
  parents: TableMetadataDto[];
  childs: TableMetadataDto[];
  isChildFirst: boolean;
  mode: 'A' | 'B';
  /** Mode A : table choisie en premier (parent si ParentFirst, enfant si ChildFirst) — PARTAGÉ avec la vue Simple */
  anchor: string;
  /** Mode A : tables cochées de l'autre côté — PARTAGÉ avec la vue Simple */
  targets: string[];
  /** Mode B : tables cochées (parents si ParentFirst, enfants si ChildFirst) — local à Advanced */
  massSelection: string[];
}>();

const emit = defineEmits<{
  'update:mode': [value: 'A' | 'B'];
  'update:isChildFirst': [value: boolean];
  'update:anchor': [value: string];
  'update:targets': [value: string[]];
  'update:massSelection': [value: string[]];
}>();

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

/** Options du select (mode A) : parents ou enfants selon isChildFirst. */
const anchorOptions = computed(() => {
  const source = props.isChildFirst ? props.childs : props.parents;
  return source.map(t => ({
    label: `${t.tableName}${t.isView ? ' (Vue)' : ''}`,
    value: t.tableName,
  }));
});

/**
 * Liste cible (mode A) : enfants du parent choisi (ParentFirst) ou parents de l'enfant choisi (ChildFirst).
 * Pas d'ancre → pas de liste.
 */
const modeAItems = computed<TableMetadataDto[]>(() => {
  if (!props.anchor) return [];
  return filterByQuery(props.isChildFirst ? props.parents : props.childs, searchA.value);
});

/** Liste du mode B (parents si ParentFirst, enfants si ChildFirst). */
const modeBItems = computed<TableMetadataDto[]>(() =>
  filterByQuery(props.isChildFirst ? props.childs : props.parents, searchB.value)
);

// ============================================================================
// SELECT ALL (éléments visibles uniquement)
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
</script>