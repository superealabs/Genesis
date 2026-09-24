<template>
  <div class="flex flex-col gap-6 p-4">
    
    <!-- 1. ZONE D'AJOUT DE RELATION -->
    <div class="flex flex-col gap-3">
      <h3 class="text-sm font-semibold text-text">Ajouter une relation</h3>
      <div class="flex flex-wrap items-end gap-3">

        <!-- Sélection Table Parent -->
        <GenesisDropdown 
          dropdown-size="lg" 
          trigger-size="md" 
          align="left" 
          label="Table Parent (Mère)"
          class="flex-1 min-w-[200px]"
        >
          <template #trigger>
            <span class="truncate text-sm">
              {{ getTablesParents.find(t => t.className === newRelation.parentTable)?.className || 'Sélectionner une table...' }}
            </span>
          </template>
          <div class="p-1 max-h-60 overflow-y-auto">
            <MenuItem v-for="table in getTablesParents" :key="table.tableName" v-slot="{ active }" as="template">
              <GenesisButton
                :class="[active ? 'bg-accent/10 text-accent' : '']"
                variant="tertiary" 
                fill-width 
                size="md"
                @click="newRelation.parentTable = table.className"
              >
                {{ table.className }}
                <span v-if="table.isView" class="text-muted text-xs ml-1">(Vue)</span>
              </GenesisButton>
            </MenuItem>
          </div>
        </GenesisDropdown>

        <!-- Sélection Table Child -->
        <GenesisDropdown 
          dropdown-size="lg" 
          trigger-size="md" 
          align="right" 
          label="Table Child (Fille)"
          class="flex-1 min-w-[200px]"
        >
          <template #trigger>
            <span class="truncate text-sm">
              {{ getTablesChilds.find(t => t.className === newRelation.childTable)?.className || 'Sélectionner une table...' }}
            </span>
          </template>
          <div class="p-1 max-h-60 overflow-y-auto">
            <MenuItem v-for="table in getTablesChilds" :key="table.tableName" v-slot="{ active }" as="template">
              <GenesisButton
                :class="[active ? 'bg-accent/10 text-accent' : '']"
                variant="tertiary" 
                fill-width 
                size="md"
                @click="newRelation.childTable = table.className"
              >
                {{ table.className }}
                <span v-if="table.isView" class="text-muted text-xs ml-1">(Vue)</span>
              </GenesisButton>
            </MenuItem>
          </div>
        </GenesisDropdown>

        <!-- Bouton d'ajout et Switcher de vue -->
        <GenesisButton 
          variant="primary" 
          size="lg" 
          :disabled="!canAddRelation" 
          @click="handleAddRelation"
        >
          <template #leftIcon><IconPlus /></template>
          Ajouter
        </GenesisButton>
        
        <LayoutSwitcherAlt v-model="internalDisplayMode" align="right" />
      </div>
    </div>

    <!-- 2. LISTE DES RELATIONS CONFIGURÉES -->
    <div class="flex flex-col gap-2">
      <h3 class="text-sm font-semibold text-text">Relations configurées</h3>
      
      <GenesisList
        :display="listDisplay"
        :have-actions="true"
        :headers="[
          { label: 'Parent (Entity)', class: 'p-3 text-center' },
          { label: 'Child (Entity)',  class: 'p-3 text-center' },
          { label: 'Form',            class: 'p-3 text-center' },
          { label: 'Mandatory',       class: 'p-3 text-center' },
        ]"
        min-col-width="200px"
      >
        <GenesisItem
          v-for="(rel, index) in getRelations"
          :key="`${rel.parentTable}-${rel.childTable}`"
          :label="rel.parentTable"
          :sublabel="rel.childTable"
          :show-logo="false"
          @close="removeRelation(index)"
        >
          <!-- Mode list : Colonnes de tableau -->
          <template #default>
            <td class="p-3 text-center truncate">{{ rel.parentTable }}</td>
            <td class="p-3 text-center truncate">{{ rel.childTable }}</td>
            <td class="p-3 text-center">
              <GenesisSwitch v-model="rel.hasForm" size="sm" />
            </td>
            <td class="p-3 text-center">
              <GenesisSwitch v-model="rel.mandatory" size="sm" />
            </td>
          </template>

          <!-- Mode grid : Affichage en blocs -->
          <template #header>
            <div class="inline-flex flex-col gap-1">
              <label class="text-lg font-medium text-muted">Parent :</label>
              <span>{{ rel.parentTable }}</span>
            </div>
            <div class="inline-flex flex-col gap-1">
              <label class="text-lg font-medium text-muted">Child :</label>
              <span>{{ rel.childTable }}</span>
            </div>
            <GenesisInput v-model="rel.hasForm" size="sm" type="boolean" label="hasForm" />
            <GenesisInput v-model="rel.mandatory" size="sm" type="boolean" label="Mandatory" />
          </template>
        </GenesisItem>
      </GenesisList>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { MenuItem } from '@headlessui/vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

// UI Components
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/GenesisItem.vue';
import LayoutSwitcherAlt from '@genesis-labs/web-core/core/components/ui/dropdown/LayoutSwitcherAlt.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';

// ============================================================================
// 1. COMPOSABLES
// ============================================================================
const { 
  getTablesParents, 
  getTablesChilds, 
  getRelations, 
  addRelation,
  removeRelation,
  fetchTablesMetadataParents, 
  fetchTablesMetadataChilds, 
  fetchRelations 
} = useGenerator();

// ============================================================================
// 2. ÉTAT LOCAL
// ============================================================================
const newRelation = ref<Partial<RelationParameter>>({ 
  parentTable: '', 
  childTable: '', 
  mandatory: false, 
  hasForm: false 
});

const internalDisplayMode = ref<'grid' | 'list'>('list');

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
const listDisplay = computed(() => internalDisplayMode.value);

const canAddRelation = computed(() => 
  !!newRelation.value.parentTable && !!newRelation.value.childTable
);

// ============================================================================
// 4. ACTIONS
// ============================================================================

/**
 * Ajoute une nouvelle relation via le store et réinitialise le formulaire.
 * La réinitialisation n'a lieu que si l'ajout est réussi (retourne true).
 * Cela évite de frustrer l'utilisateur en effaçant sa sélection en cas 
 * de tentative d'ajout d'une relation en double (géré par le store).
 */
function handleAddRelation() {
  if (!canAddRelation.value) return;
  
  const success = addRelation({
    parentTable: newRelation.value.parentTable!,
    childTable:  newRelation.value.childTable!,
    mandatory:   newRelation.value.mandatory ?? false,
    hasForm:     newRelation.value.hasForm   ?? false
  });
  
  if (success) {
    newRelation.value = { parentTable: '', childTable: '', mandatory: false, hasForm: false };
  }
}

// ============================================================================
// 5. LIFECYCLE
// ============================================================================
onMounted(() => {
  fetchTablesMetadataParents();
  fetchTablesMetadataChilds();
  fetchRelations();
});
</script>