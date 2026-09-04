<template>
  <div class="flex flex-col gap-6 p-4">
    <!-- ═══ Zone d'ajout ═══ -->
    <div class="flex flex-col gap-3">
      <h3 class="text-sm font-semibold text-text">Ajouter une relation</h3>
      <div class="flex flex-wrap items-end gap-3">

        <GenesisDropdown dropdown-size="lg" trigger-size="md" :align="'left'" class="flex-1 min-w-[200px]"
          label="Table Parent (Mère)">
          <template #trigger>
            <span class="truncate text-sm">
              {{ getTablesParents.find(t => t.className === newRelation.parentTable)?.className || 'Sélectionner une table...' }}
            </span>
          </template>
          <div class="p-1 max-h-60 overflow-y-auto">
            <MenuItem v-for="table in getTablesParents" :key="table.tableName" v-slot="{ active }" as="template">
              <GenesisButton
                :class="[active ? 'bg-accent/10 text-accent' : '']"
                variant="tertiary" fill-width size="md"
                @click="newRelation.parentTable = table.className"
              >
                {{ table.className }}
                <span v-if="table.isView" class="text-muted text-xs ml-1">(Vue)</span>
              </GenesisButton>
            </MenuItem>
          </div>
        </GenesisDropdown>

        <GenesisDropdown dropdown-size="lg" trigger-size="md" :align="'right'" class="flex-1 min-w-[200px]"
          label="Table Child (Fille)">
          <template #trigger>
            <span class="truncate text-sm">
              {{ getTablesChilds.find(t => t.className === newRelation.childTable)?.className || 'Sélectionner une table...' }}
            </span>
          </template>
          <div class="p-1 max-h-60 overflow-y-auto">
            <MenuItem v-for="table in getTablesChilds" :key="table.tableName" v-slot="{ active }" as="template">
              <GenesisButton
                :class="[active ? 'bg-accent/10 text-accent' : '']"
                variant="tertiary" fill-width size="md"
                @click="newRelation.childTable = table.className"
              >
                {{ table.className }}
                <span v-if="table.isView" class="text-muted text-xs ml-1">(Vue)</span>
              </GenesisButton>
            </MenuItem>
          </div>
        </GenesisDropdown>

        <GenesisButton variant="primary" size="lg" :disabled="!canAddRelation" @click="handleAddRelation">
          <template #leftIcon><IconPlus /></template>
          Ajouter
        </GenesisButton>
        <LayoutSwitcherAlt v-model="internalDisplayMode" :align="'right'"/>
      </div>
    </div>

    <!-- ═══ Liste des relations ═══ -->
    <div class="flex flex-col gap-2">
        <h3 class="text-sm font-semibold text-text">Relations configurées</h3>
        <GenesisList
            :display="listDisplay"
            :haveActions="true"
            :headers="[
                { label: 'Parent (Entity)', class: 'p-3 text-center' },
                { label: 'Child (Entity)',  class: 'p-3 text-center' },
                { label: 'Form',            class: 'p-3 text-center' },
                { label: 'Mandatory',       class: 'p-3 text-center' },
            ]"
            minColWidth="200px"
        >
            <GenesisItem
                v-for="(rel, index) in getRelations"
                :key="`${rel.parentTable}-${rel.childTable}`"
                :label="rel.parentTable"
                :sublabel="rel.childTable"
                :showLogo="false"
                @close="removeRelation(index)"
            >
                <!-- Mode list : <td> dans l'ordre des colonnes, actions injectées auto -->
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

                <!-- Mode grid : slots dédiés -->
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
import { useGenerator } from '../../composables/useGenerator';
import { MenuItem } from '@headlessui/vue';
import GenesisDropdown from '@/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisList from '@/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@/core/components/layouts/display/GenesisItem.vue';
import LayoutSwitcherAlt from '@/core/components/ui/dropdown/LayoutSwitcherAlt.vue';
import GenesisButton from '@/core/components/ui/actions/GenesisButton.vue';
import GenesisSwitch from '@/core/components/ui/inputs/GenesisSwitch.vue';
import IconPlus from '@/core/components/ui/icons/IconPlus.vue';
import type { RelationParameter } from '../../types/generator.types';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';

// 1. On récupère TOUT, y compris les actions du store (pas de fonctions locales du même nom !)
const { 
    getTablesParents, 
    getTablesChilds, 
    getRelations, 
    addRelation,        // <-- Action du store (vérifie déjà les doublons)
    removeRelation,     // <-- Action du store
    fetchTablesMetadataParents, 
    fetchTablesMetadataChilds, 
    fetchRelations 
} = useGenerator();

const newRelation = ref<Partial<RelationParameter>>({ parentTable: '', childTable: '', mandatory: false, hasForm: false });
const internalDisplayMode = ref<'grid' | 'list'>('list');
const listDisplay = computed(() => internalDisplayMode.value);

const canAddRelation = computed(() =>
    !!newRelation.value.parentTable && !!newRelation.value.childTable
);

// 2. On appelle l'action du store. Elle retourne true si réussi, false si doublon.
function handleAddRelation() {
    if (!canAddRelation.value) return;
    
    const success = addRelation({
        parentTable: newRelation.value.parentTable!,
        childTable:  newRelation.value.childTable!,
        mandatory:   newRelation.value.mandatory ?? false,
        hasForm:     newRelation.value.hasForm   ?? false
    });
    
    // On vide le formulaire UNIQUEMENT si l'ajout a réussi (pas de doublon)
    if (success) {
        newRelation.value = { parentTable: '', childTable: '', mandatory: false, hasForm: false };
    }
}

onMounted(() => {
    fetchTablesMetadataParents();
    fetchTablesMetadataChilds();
    fetchRelations();
});
</script>