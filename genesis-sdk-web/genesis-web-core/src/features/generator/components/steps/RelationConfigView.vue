<template>
  <main class="flex gap-4 h-full overflow-hidden">
    
    <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
    <div class="flex-1 min-h-0 overflow-y-auto p-6 space-y-6 custom-scrollbar">
      
      <!-- 1. ZONE D'AJOUT DE RELATION -->
      <div class="flex flex-col gap-3">
        <h3 class="text-sm font-semibold text-text">Ajouter une relation</h3>
        <div class="flex flex-wrap items-end gap-3">
          <GenesisInput
            v-model="newRelation.parentTable"
            type="select"
            :options="parentTableOptions"
            label="Table Parent (Mère)"
            placeholder="Sélectionner une table..."
            size="lg"
            fill-width
            class="flex-1 min-w-[200px]"
          />
          <GenesisInput
            v-model="newRelation.childTable"
            type="select"
            :options="childTableOptions"
            label="Table Child (Fille)"
            placeholder="Sélectionner une table..."
            size="lg"
            fill-width
            class="flex-1 min-w-[200px]"
          />
          <GenesisButton 
            variant="primary" 
            size="lg" 
            :disabled="!canAddRelation" 
            @click="handleAddRelation"
          >
            <template #leftIcon><IconPlus /></template>
            Ajouter
          </GenesisButton>
        </div>
      </div>

      <!-- 2. LISTE DES RELATIONS CONFIGURÉES (Nouveau composant) -->
      <div class="flex flex-col gap-2">
        <RelationTableView 
          :relations="getRelations" 
          :is-editable="true"
          :max-height="size"
          @remove="handleRemoveRelation"
          @update-relation="handleUpdateRelation"
        />
      </div>

    </div>

    <!-- PARTIE DROITE : CARROUSEL TUTORIEL (2/5 de la largeur) -->
    <div class="w-2/5 h-full shrink-0">
      <CarrouselPanel :slides="panelSlides" />
    </div>

  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

// UI Components
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ✅ NOUVEAU COMPOSANT
import RelationTableView from '../relation/RelationTableView.vue';

const size = window.innerHeight * 0.4;

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
// 2. ÉTAT LOCAL & CONSTANTES
// ============================================================================
const newRelation = ref<Partial<RelationParameter>>({ 
  parentTable: '', 
  childTable: '', 
  mandatory: false, 
  hasForm: false 
});

const panelSlides: CarouselSlide[] = [
  { color: '#3B82F6', label: 'Étape 1 : Sélection des entités' },
  { color: '#EF4444', label: 'Étape 2 : Configuration des relations' },
  { color: '#10B981', label: 'Étape 3 : Options (Form, Mandatory)' },
];

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
const canAddRelation = computed(() => 
  !!newRelation.value.parentTable && !!newRelation.value.childTable
);

const parentTableOptions = computed(() => 
  getTablesParents.value.map(t => ({ 
    label: `${t.className}${t.isView ? ' (Vue)' : ''}`, 
    value: t.className 
  }))
);

const childTableOptions = computed(() => 
  getTablesChilds.value.map(t => ({ 
    label: `${t.className}${t.isView ? ' (Vue)' : ''}`, 
    value: t.className 
  }))
);

// ============================================================================
// 4. ACTIONS
// ============================================================================
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

// ✅ Gère la suppression depuis le tableau
function handleRemoveRelation(relationToRemove: RelationParameter) {
  const index = getRelations.value.findIndex(r => 
    r.parentTable === relationToRemove.parentTable && 
    r.childTable === relationToRemove.childTable
  );
  if (index !== -1) {
    removeRelation(index);
  }
}

// ✅ Gère la mise à jour des switches depuis le tableau
function handleUpdateRelation(relation: RelationParameter, field: 'hasForm' | 'mandatory', value: boolean) {
  // Note: Si ton store Pinia est en mode strict, tu devras peut-être appeler une action 
  // 'updateRelation' dédiée. Sinon, la mutation réactive directe fonctionne.
  // Exemple avec une action hypothétique : updateRelation(index, field, value);
  console.log(`Mise à jour de ${field} pour ${relation.parentTable} -> ${relation.childTable} : ${value}`);
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

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>