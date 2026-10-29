<template>
  <!-- ✅ Passage en flex-col pour placer l'en-tête au-dessus du contenu -->
  <main class="flex flex-col h-full overflow-hidden">
    
    <!-- Conteneur des colonnes (Contenu + Carrousel) -->
    <div class="flex gap-8 flex-1 min-h-0 overflow-hidden px-6 pb-6">
      
      <!-- PARTIE GAUCHE : CONTENU PRINCIPAL (Scrollable) -->
      <div class="flex-1 min-h-0 overflow-y-auto space-y-6 custom-scrollbar pr-3">

        <div class="flex items-center justify-between pb-3 mb-4 flex-shrink-0">
          <h3 class="text-xl font-semibold text-text">
            Configuration des Relations
          </h3>
          <GenesisButtonIcon 
            variant="neutral" 
            size="sm" 
            @click="tutorial.restart('main')"
            title="Lancer le tutoriel"
          >
            <IconHelp />
          </GenesisButtonIcon>
        </div>
        
        <!-- 1. ZONE D'AJOUT DE RELATION -->
        <div class="flex flex-col gap-3">
          <h3 class="text-sm font-semibold text-text">Ajouter une relation</h3>
          <div class="flex flex-wrap items-end gap-3">
            
            <!-- ✅ ÉTAPE 1 : Table Parent -->
            <GenesisTutorialItem id="relation-parent-input">
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
            </GenesisTutorialItem>

            <!-- ✅ ÉTAPE 2 : Table Enfant -->
            <GenesisTutorialItem id="relation-child-input">
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
            </GenesisTutorialItem>

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

        <!-- 2. LISTE DES RELATIONS CONFIGURÉES -->
        <div class="flex flex-col gap-2">
          <h3 class="text-sm font-semibold text-text">Relations configurées</h3>
          
          <!-- ✅ ÉTAPE 3 : Tableau des relations -->
          <GenesisTutorialItem id="relation-table-view">
            <RelationTableView 
              :relations="getRelations" 
              :is-editable="true"
              :max-height="size"
              @remove="handleRemoveRelation"
              @update-relation="handleUpdateRelation"
            />
          </GenesisTutorialItem>
        </div>

      </div>

      <!-- PARTIE DROITE : CARROUSEL TUTORIEL (2/5 de la largeur) -->
      <div class="w-2/5 h-full shrink-0">
        <CarrouselPanel :slides="panelSlides" />
      </div>

    </div>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial'; // ✅ AJOUT

// UI Components
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue'; // ✅ AJOUT
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue'; // ✅ AJOUT
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// ✅ NOUVEAU COMPOSANT
import RelationTableView from '../relation/RelationTableView.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue'; // ✅ AJOUT

const size = window.innerHeight * 0.4;

// ============================================================================
// 1. COMPOSABLES & TUTORIEL
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

// ✅ DÉCLARATION DU TUTORIEL POUR CETTE VUE
const tutorial = useGenesisTutorial('relation-config', {
  'main': [
    { 
      id: 'relation-parent-input', 
      text: 'Sélectionnez la table parente (mère) qui contiendra la clé primaire de la relation.' 
    },
    { 
      id: 'relation-child-input', 
      text: 'Sélectionnez la table enfant (fille) qui contiendra la clé étrangère.' 
    },
    { 
      id: 'relation-table-view', 
      text: 'Visualisez, modifiez ou supprimez les relations que vous avez configurées dans ce tableau.' 
    }
  ]
});

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

function handleRemoveRelation(relationToRemove: RelationParameter) {
  const index = getRelations.value.findIndex(r => 
    r.parentTable === relationToRemove.parentTable && 
    r.childTable === relationToRemove.childTable
  );
  if (index !== -1) {
    removeRelation(index);
  }
}

function handleUpdateRelation(relation: RelationParameter, field: 'hasForm' | 'mandatory', value: boolean) {
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