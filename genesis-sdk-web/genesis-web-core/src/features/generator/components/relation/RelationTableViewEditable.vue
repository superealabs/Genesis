<template>
  <div class="flex flex-col gap-3 w-full">
    <!-- Barre de recherche + 2 boutons (Clear / Select All) -->
    <div class="flex items-center gap-2">
      <GenesisInput
        v-model="searchQuery"
        type="text"
        :placeholder="searchPlaceholder"
        size="md"
        fill-width
        class="flex-1"
      >
        <template #left>
          <IconSearch :size="12" class="text-text-muted" />
        </template>
      </GenesisInput>

      <div v-if="filteredRelations.length > 0" class="flex items-center gap-2 shrink-0">
        <GenesisButton
          :disabled="!hasSelected"
          variant="secondary"
          size="md"
          is-perspective
          @click="clear"
          title="Désélectionner les relations visibles"
        >
          Clear
        </GenesisButton>
        <GenesisButton
          :disabled="allSelected"
          variant="primary"
          size="md"
          is-perspective
          @click="selectAll"
          title="Sélectionner toutes les relations visibles"
        >
          Select All
        </GenesisButton>
      </div>
    </div>

    <div class="flex flex-row justify-between text-text-muted">
        <div>Selected element : <span>{{ selectedRelations.length }}/{{ relations.length }}</span></div>
        <div>Element left : <span>{{ relations.length }}</span></div>
    </div>
    <GenesisContainer
      :min-height="minHeight"
      :max-height="maxHeight"
      :is-empty="filteredRelations.length === 0"
      :empty-text="relations.length === 0 ? 'Aucune relation configurée.' : 'Aucune relation ne correspond à ces critères.'"
    >
      <GenesisLoaderOverlay
        v-if="isLoading"
        :visible="true"
        size="md"
        title="Chargement des relations"
        class="relative h-[400px]"
      />
      <table v-else class="w-full text-left text-sm">
        <thead>
          <tr>
            <th class="sticky top-0 z-20 p-3 w-10 bg-bg-dark"></th>

            <!-- Header Parent -->
            <th
              class="sticky top-0 z-20 p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
              :class="searchTarget === 'parent' ? 'text-secondary bg-bg-secondary' : 'text-muted bg-bg-dark hover:bg-bg-secondary'"
              @click="toggleSearchTarget('parent')"
            >
              <span>Parent</span>
              <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                <IconSearch v-if="searchTarget === 'parent'" :size="16" class="w-4 h-4" />
              </div>
            </th>

            <!-- Header Child -->
            <th
              class="sticky top-0 z-20 p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
              :class="searchTarget === 'child' ? 'text-secondary bg-bg-secondary' : 'text-muted bg-bg-dark hover:bg-bg-secondary'"
              @click="toggleSearchTarget('child')"
            >
              <span>Child</span>
              <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                <IconSearch v-if="searchTarget === 'child'" :size="16" class="w-4 h-4" />
              </div>
            </th>

            <!-- Header Has Form -->
            <th
              class="sticky top-0 z-20 p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
              :class="{
                'text-muted bg-bg-dark hover:bg-bg-secondary': filterForm === 'all',
                'text-secondary bg-bg-secondary': filterForm === 'yes',
                'text-red-600 bg-red-600/50 hover:bg-red-300': filterForm === 'no'
              }"
              @click="toggleFilterForm"
            >
              <span>Has Form</span>
              <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                <IconCheck v-if="filterForm === 'yes'" :size="16" class="w-4 h-4" />
                <IconX v-else-if="filterForm === 'no'" :size="16" class="w-4 h-4" />
              </div>
            </th>

            <!-- Header Mandatory -->
            <th
              class="sticky top-0 z-20 p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
              :class="{
                'text-muted bg-bg-dark hover:bg-bg-secondary': filterMandatory === 'all',
                'text-secondary bg-bg-secondary': filterMandatory === 'yes',
                'text-red-600 bg-red-600/50 hover:bg-red-300': filterMandatory === 'no'
              }"
              @click="toggleFilterMandatory"
            >
              <span>Mandatory</span>
              <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                <IconCheck v-if="filterMandatory === 'yes'" :size="16" class="w-4 h-4" />
                <IconX v-else-if="filterMandatory === 'no'" :size="16" class="w-4 h-4" />
              </div>
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-bg-neutral-genesis">
          <tr
            v-for="rel in filteredRelations"
            :key="relationKey(rel)"
            class="hover:bg-bg-neutral-genesis transition-colors cursor-pointer select-none"
            @mousedown.prevent=""
            @click="(e) => handleRowClick(rel, e)"
          >
            <td class="p-3">
              <GenesisCheckboxSimple :model-value="selectedRelations.includes(relationKey(rel))" />
            </td>
            <td class="p-4 font-medium text-text">{{ rel.parentTable }}</td>
            <td class="p-4 font-medium text-text">{{ rel.childTable }}</td>
            <td class="p-4 text-center">
              <IconCheck v-if="rel.hasForm" class="w-12 h-12 text-secondary inline-block" />
              <IconX v-else class="w-12 h-12 text-red-500 inline-block" />
            </td>
            <td class="p-4 text-center">
              <IconCheck v-if="rel.mandatory" class="w-12 h-12 text-secondary inline-block" />
              <IconX v-else class="w-12 h-12 text-red-500 inline-block" />
            </td>
          </tr>
        </tbody>
      </table>
    </GenesisContainer>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';
import { useRelationFilters } from './composables/useRelationFilters';
// import { useSelectVisible } from './composables/useSelectVisible';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import { useMultiSelect } from '@genesis-labs/web-core/core/composables/ux/useMultiSelect';
import GenesisLoaderOverlay from '@genesis-labs/web-core/core/components/ui/feedback/GenesisLoaderOverlay.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';

const props = withDefaults(defineProps<{
  relations: RelationParameter[];
  selectedRelations: string[];
  minHeight?: number;
  maxHeight?: number;
  isLoading?: boolean;   // ← nouveau, défaut false
}>(), {
  minHeight: 250,
  maxHeight: 600,
});

const emit = defineEmits<{
  'update:selectedRelations': [value: string[]];
}>();

function relationKey(rel: RelationParameter): string {
  return `${rel.parentTable}::${rel.childTable}`;
}

// Recherche & filtres (logique partagée avec RelationTableView)
const {
  searchQuery,
  filterForm,
  filterMandatory,
  searchTarget,
  searchPlaceholder,
  filteredRelations,
  toggleFilterForm,
  toggleFilterMandatory,
  toggleSearchTarget,
} = useRelationFilters(() => props.relations);

// Ref interne mutable, synchronisée avec les props
const selectedIdsRef = ref<string[]>([...props.selectedRelations]);

watch(
  () => props.selectedRelations,
  (v) => { selectedIdsRef.value = [...v]; },
);

// `items` = lignes AFFICHÉES : la sélection par plage (Shift) suit l'ordre filtré
const { handleClick } = useMultiSelect<RelationParameter>({
  items: filteredRelations,
  getId: relationKey,
  selectedIds: selectedIdsRef,
});

// Select All : uniquement les lignes visibles (recherche + filtres)
function updateSelection(ids: string[]) {
  selectedIdsRef.value = ids;
  emit('update:selectedRelations', [...ids]);
}

// const { visibleCount, allSelected, toggleAll } = useSelectVisible(
//   () => filteredRelations.value.map(relationKey),
//   selectedIdsRef,
//   updateSelection,
// );

// ═══ Select All / Clear ═══
/** Y a-t-il au moins une relation visible sélectionnée ? */
const hasSelected = computed(() => {
  const selected = new Set(selectedIdsRef.value);
  return filteredRelations.value.some(rel => selected.has(relationKey(rel)));
});

/** Toutes les relations visibles sont-elles sélectionnées ? */
const allSelected = computed(() => {
  if (filteredRelations.value.length === 0) return false;
  const selected = new Set(selectedIdsRef.value);
  return filteredRelations.value.every(rel => selected.has(relationKey(rel)));
});

/** Sélectionne TOUTES les relations visibles. */
function selectAll(): void {
  const current = new Set(selectedIdsRef.value);
  filteredRelations.value.forEach(rel => current.add(relationKey(rel)));
  updateSelection([...current]);
}

/** Désélectionne TOUTES les relations visibles. */
function clear(): void {
  const visibleKeys = new Set(filteredRelations.value.map(relationKey));
  updateSelection(selectedIdsRef.value.filter(key => !visibleKeys.has(key)));
}

function handleRowClick(rel: RelationParameter, event: MouseEvent) {
  handleClick(rel, event);
  // Émet l'état courant après que le composable a muté `selectedIdsRef.value`
  emit('update:selectedRelations', [...selectedIdsRef.value]);
}
</script>