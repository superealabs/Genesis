<template>
  <div class="flex gap-8 flex-1 min-h-0 overflow-hidden px-6 pb-6">
    <div class="flex-1 min-h-0 overflow-y-auto space-y-6 custom-scrollbar pr-3">
      <!-- Bouton tuto -->
      <div class="flex justify-end">
        <GenesisButtonIcon variant="neutral" size="sm" @click="tutorial.restart('main')" title="Lancer le tutoriel">
          <IconHelp />
        </GenesisButtonIcon>
      </div>

        <div class="flex flex-col gap-3">
          <h3 class="text-sm font-semibold text-text">Ajouter une relation</h3>

          <div class="flex flex-wrap items-end gap-3">
            
            <!-- ÉTAPE 1 : Table Parent -->
            <GenesisTutorialItem id="relation-parent-input">
              <GenesisInput
                v-model="parentTable"
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

            <!-- Bouton de bascule de mode (ParentFirst ↔ ChildFirst) -->
            <GenesisButtonIcon
              variant="secondary"
              size="lg"
              @click="toggleSearchMode"
            >
              <IconChevronRight 
                class="transition-transform duration-300 ease-in-out"
                :class="isChildFirstMode ? '-rotate-180' : 'rotate-0'"
              />
            </GenesisButtonIcon>

            <!-- ÉTAPE 2 : Table Enfant -->
            <GenesisTutorialItem id="relation-child-input">
              <GenesisInput
                v-model="childTable"
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
    <div 
      class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center rounded-lg overflow-hidden relative min-h-[300px]" 
      :class="relationIllustration ? '' : 'border border-secondary border-dashed bg-secondary/10'"
    >
      <img 
        v-if="relationIllustration"
        :src="relationIllustration" 
        alt="Relation" 
        class="absolute inset-0 w-full h-full object-cover"
      />
      <template v-else>
        <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
        </svg>
        <span class="text-sm font-medium">Aperçu du projet</span>
      </template>
    </div>
  </div>
</template>
<script setup lang="ts">
import { computed } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// Composables
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import { useRelationBuilder } from './composables/useRelationBuilder.ts';
import { useGenesisTutorial } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';

// UI Components
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconHelp from '@genesis-labs/web-core/core/components/ui/icons/IconHelp.vue';
// import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
// import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

import RelationTableView from '../relation/RelationTableView.vue';
import GenesisTutorialItem from '@genesis-labs/web-core/core/features/tutorial/components/GenesisTutorialItem.vue';
import IconChevronRight from '@genesis-labs/web-core/core/components/ui/icons/IconChevronRight.vue';
import relationIllustration from '../../assets/relation-summary.png'

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
} = useGenerator();

// État PARTAGÉ avec RelationConfigAdvancedView (sens, ancre, cibles + chargements API)
const {
  isChildFirst: isChildFirstMode,
  anchor,
  targets,
  setDirection,
  setAnchor,
  setTargets,
  resetSelection,
} = useRelationBuilder();

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
// 2. ÉTAT & CONSTANTES
// ============================================================================
// const panelSlides: CarouselSlide[] = [
//   { color: '#3B82F6', label: 'Étape 1 : Sélection des entités' },
//   { color: '#EF4444', label: 'Étape 2 : Configuration des relations' },
//   { color: '#10B981', label: 'Étape 3 : Options (Form, Mandatory)' },
// ];

// ============================================================================
// 3. COMPUTEDS
// ============================================================================
/**
 * Parent / enfant sélectionnés, dérivés de l'état partagé :
 * ancre = parent (ParentFirst) ou enfant (ChildFirst) ; cible = l'autre côté (1 seule en Simple).
 */
const parentTable = computed({
  get: () => (isChildFirstMode.value ? (targets.value[0] ?? '') : anchor.value),
  set: (v: string) => (isChildFirstMode.value ? setTargets(v ? [v] : []) : setAnchor(v ?? '')),
});

const childTable = computed({
  get: () => (isChildFirstMode.value ? anchor.value : (targets.value[0] ?? '')),
  set: (v: string) => (isChildFirstMode.value ? setAnchor(v ?? '') : setTargets(v ? [v] : [])),
});

const canAddRelation = computed(() => !!parentTable.value && !!childTable.value);

const parentTableOptions = computed(() => 
  tablesParents.value.map(t => ({ 
    label: `${t.tableName}${t.isView ? ' (Vue)' : ''}`, 
    value: t.tableName
  }))
);

/**
 * Bascule entre ParentFirst et ChildFirst.
 * Le reset + rechargement sont gérés par `useRelationBuilder`.
 */
function toggleSearchMode() {
  setDirection(!isChildFirstMode.value);
}

const childTableOptions = computed(() => 
  tablesChilds.value.map(t => ({ 
    label: `${t.tableName}${t.isView ? ' (Vue)' : ''}`, 
    value: t.tableName 
  }))
);

// En ChildFirst, le parent est désactivé tant que l'enfant n'est pas choisi
const parentInputDisabled = computed(() => 
  isChildFirstMode.value && !childTable.value
);

// En ParentFirst, l'enfant est désactivé tant que le parent n'est pas choisi
const childInputDisabled = computed(() => 
  !isChildFirstMode.value && !parentTable.value
);

const parentPlaceholder = computed(() => 
  parentInputDisabled.value 
    ? 'Select Child First' 
    : 'Select Table'
);

const childPlaceholder = computed(() => 
  childInputDisabled.value 
    ? 'Select Parent First' 
    : 'Select Table'
);

// ============================================================================
// 4. ACTIONS UTILISATEUR
// ============================================================================
function handleAddRelation() {
  if (!canAddRelation.value) return;

  const success = addRelation({
    parentTable: parentTable.value,
    childTable:  childTable.value,
    mandatory:   false,
    hasForm:     false
  });

  if (success) resetSelection();  // vide ancre + cibles et recharge les listes complètes
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
// Note : le chargement initial (parents, childs, relations) est fait
// dans `relations.onEnter` (wizard-step-config.ts). Pas besoin de le refaire ici.
</script>