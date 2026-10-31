<template>
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

          <!-- 👇 Switch de mode : ParentFirst (false) / ChildFirst (true) -->
          <div class="flex items-center gap-3 pb-2">
            <GenesisSwitch
              v-model="isChildFirstMode"
              :label="isChildFirstMode 
                ? 'Recherche par Enfant (Child → Parent)' 
                : 'Recherche par Parent (Parent → Child)'"
              description="Détermine quel champ doit être renseigné en premier."
              size="md"
            />
          </div>

          <div class="flex flex-wrap items-end gap-3">
            
            <!-- ÉTAPE 1 : Table Parent -->
            <GenesisTutorialItem id="relation-parent-input">
              <GenesisInput
                v-model="newRelation.parentTable"
                type="select"
                :options="parentTableOptions"
                label="Table Parent (Mère)"
                :placeholder="parentPlaceholder"
                :disabled="parentInputDisabled"
                size="lg"
                fill-width
                class="flex-1 min-w-[200px]"
              />
            </GenesisTutorialItem>

            <!-- ÉTAPE 2 : Table Enfant -->
            <GenesisTutorialItem id="relation-child-input">
              <GenesisInput
                v-model="newRelation.childTable"
                type="select"
                :options="childTableOptions"
                label="Table Child (Fille)"
                :placeholder="childPlaceholder"
                :disabled="childInputDisabled"
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
          
          <!-- ÉTAPE 3 : Tableau des relations -->
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
import { ref, computed, watch, nextTick } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// UI Components
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue';  // 👈 nouveau
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

import RelationTableView from '../relation/RelationTableView.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';

const size = window.innerHeight * 0.4;

// ============================================================================
// 1. COMPOSABLES & TUTORIEL
// ============================================================================
const { 
  tablesParents,        // 👈 getter du store (nom exact)
  tablesChilds,         // 👈 getter du store (nom exact)
  getRelations, 
  addRelation,
  removeRelation,
  fetchTablesMetadataParents,
  fetchTablesMetadataChilds,
} = useGenerator();

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

/**
 * Mode de recherche :
 * - false = ParentFirst (on choisit d'abord le parent, puis l'enfant)
 * - true  = ChildFirst  (on choisit d'abord l'enfant, puis le parent)
 */
const isChildFirstMode = ref(false);

/**
 * Flag anti-concurrence pour le switch.
 * Empêche les watchers de parentTable/childTable de se déclencher pendant
 * le reset du switch (évite les doubles appels API).
 */
const isSwitching = ref(false);

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
  tablesParents.value.map(t => ({ 
    label: `${t.className}${t.isView ? ' (Vue)' : ''}`, 
    value: t.className 
  }))
);

const childTableOptions = computed(() => 
  tablesChilds.value.map(t => ({ 
    label: `${t.className}${t.isView ? ' (Vue)' : ''}`, 
    value: t.className 
  }))
);

// En ChildFirst, le parent est désactivé tant que l'enfant n'est pas choisi
const parentInputDisabled = computed(() => 
  isChildFirstMode.value && !newRelation.value.childTable
);

// En ParentFirst, l'enfant est désactivé tant que le parent n'est pas choisi
const childInputDisabled = computed(() => 
  !isChildFirstMode.value && !newRelation.value.parentTable
);

const parentPlaceholder = computed(() => 
  parentInputDisabled.value 
    ? 'Sélectionnez d\'abord une table enfant' 
    : 'Sélectionner une table...'
);

const childPlaceholder = computed(() => 
  childInputDisabled.value 
    ? 'Sélectionnez d\'abord une table parent' 
    : 'Sélectionner une table...'
);

// ============================================================================
// 4. ACTIONS DE CHARGEMENT
// ============================================================================
async function loadAllParents() {
  try {
    await fetchTablesMetadataParents();  // sans paramètre = tous
  } catch (error) {
    console.error('[RelationConfigView] Échec chargement parents:', error);
  }
}

async function loadAllChilds() {
  try {
    await fetchTablesMetadataChilds();  // sans paramètre = tous
  } catch (error) {
    console.error('[RelationConfigView] Échec chargement enfants:', error);
  }
}

async function loadChildsForParent(parentTableName: string) {
  try {
    await fetchTablesMetadataChilds(parentTableName);
  } catch (error) {
    console.error('[RelationConfigView] Échec chargement enfants pour', parentTableName, error);
  }
}

async function loadParentsForChild(childTableName: string) {
  try {
    await fetchTablesMetadataParents(childTableName);
  } catch (error) {
    console.error('[RelationConfigView] Échec chargement parents pour', childTableName, error);
  }
}

// ============================================================================
// 5. WATCHERS
// ============================================================================

// En ParentFirst : quand le parent change, on recharge les enfants
watch(() => newRelation.value.parentTable, async (newParent, oldParent) => {
  if (isSwitching.value) return;         // ignore pendant le switch
  if (isChildFirstMode.value) return;    // ignore en ChildFirst
  if (newParent === oldParent) return;

  // Reset l'enfant (il n'appartient plus au même parent)
  newRelation.value.childTable = '';

  if (!newParent) {
    await loadAllChilds();  // parent vidé → tous les enfants
    return;
  }

  await loadChildsForParent(newParent);
});

// En ChildFirst : quand l'enfant change, on recharge les parents
watch(() => newRelation.value.childTable, async (newChild, oldChild) => {
  if (isSwitching.value) return;         // ignore pendant le switch
  if (!isChildFirstMode.value) return;   // ignore en ParentFirst
  if (newChild === oldChild) return;

  // Reset le parent
  newRelation.value.parentTable = '';

  if (!newChild) {
    await loadAllParents();
    return;
  }

  await loadParentsForChild(newChild);
});

// Switch de mode → reset complet + recharge
watch(isChildFirstMode, async (isChildFirst) => {
  isSwitching.value = true;

  // Reset les sélections (on change de logique de recherche)
  newRelation.value.parentTable = '';
  newRelation.value.childTable = '';

  // Recharger les options selon le mode
  if (isChildFirst) {
    await loadAllChilds();
  } else {
    await loadAllParents();
  }

  // Attendre que les watchers sur parentTable/childTable aient fini de sortir tôt
  await nextTick();
  isSwitching.value = false;
});

// ============================================================================
// 6. ACTIONS UTILISATEUR
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
    // Reset complet (les watchers vont recharger les options selon le mode)
    newRelation.value = { 
      parentTable: '', 
      childTable: '', 
      mandatory: false, 
      hasForm: false 
    };
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
// 7. LIFECYCLE
// ============================================================================
// Note : le chargement initial (parents, childs, relations) est fait
// dans `relations.onEnter` (wizard-step-config.ts). Pas besoin de le refaire ici.
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>