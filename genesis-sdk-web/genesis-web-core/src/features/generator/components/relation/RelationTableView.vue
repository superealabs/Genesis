<template>
  <div class="flex flex-col gap-4 w-full h-full">
    <!-- 1. En-tête et Filtres -->
    <header class="flex flex-col gap-4">
      <div class="flex flex-col gap-2">
        <span class="text-xs font-semibold uppercase tracking-widest text-muted">Relations</span>
        <h1 class="text-2xl font-bold leading-tight text-text break-words">
          {{ isEditable ? 'Gérer les liaisons' : 'Liaisons configurées' }}
        </h1>
        <p class="text-sm text-text-muted">
          {{ isEditable 
            ? 'Définissez les liaisons parent-enfant et leurs options de génération.' 
            : 'Aperçu des liaisons parent-enfant configurées entre les entités de votre base de données.' 
          }}
        </p>
      </div>

      <!-- Barre de recherche -->
      <div class="flex flex-wrap gap-3 items-end">
        <div class="flex-1 min-w-[250px]">
          <GenesisInput
            v-model="searchQuery"
            type="text"
            :placeholder="searchPlaceholder"
            size="lg"
            fill-width
          >
            <template #left>
              <IconSearch :size="12" class="text-text-muted" />
            </template>
          </GenesisInput>
        </div>
      </div>
    </header>

    <!-- 2. Tableau des Relations -->
    <section class="flex-1 min-h-0">
      <GenesisContainer
        :min-height="250"
        :max-height="maxHeight"
        :is-empty="filteredRelations.length === 0"
        empty-text="Aucune relation ne correspond à ces critères."
      >
        <table class="w-full text-left text-sm">
          <thead>
            <tr>
              <!-- ✅ Header Parent -->
              <th 
                class="sticky top-0 z-20 p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                :class="searchTarget === 'parent' ? 'text-secondary bg-bg-secondary' : 'text-muted bg-bg-dark hover:bg-bg-secondary'"
                @click="toggleSearchTarget('parent')"
              >
                <span>Table Parent (Mère)</span>
                <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                  <IconSearch v-if="searchTarget === 'parent'" :size="16" class="w-4 h-4" />
                </div>
              </th>

              <!-- ✅ Header Child -->
              <th 
                class="sticky top-0 z-20 p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                :class="searchTarget === 'child' ? 'text-secondary bg-bg-secondary' : 'text-muted bg-bg-dark hover:bg-bg-secondary'"
                @click="toggleSearchTarget('child')"
              >
                <span>Table Child (Fille)</span>
                <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                  <IconSearch v-if="searchTarget === 'child'" :size="16" class="w-4 h-4" />
                </div>
              </th>

              <!-- ✅ Header Formulaire -->
              <th 
                class="sticky top-0 z-20 p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                :class="{
                  'text-muted bg-bg-dark hover:bg-bg-secondary': filterForm === 'all',
                  'text-secondary bg-bg-secondary': filterForm === 'yes',
                  'text-red-600 bg-red-600/50 hover:bg-red-300': filterForm === 'no'
                }"
                @click="toggleFilterForm"
              >
                <span>Formulaire</span>
                <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                  <IconCheck v-if="filterForm === 'yes'" :size="16" class="w-4 h-4" />
                  <IconX v-else-if="filterForm === 'no'" :size="16" class="w-4 h-4" />
                </div>
              </th>

              <!-- ✅ Header Obligatoire -->
              <th 
                class="sticky top-0 z-20 p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                :class="{
                  'text-muted bg-bg-dark hover:bg-bg-secondary': filterMandatory === 'all',
                  'text-secondary bg-bg-secondary': filterMandatory === 'yes',
                  'text-red-600 bg-red-600/50 hover:bg-red-300': filterMandatory === 'no'
                }"
                @click="toggleFilterMandatory"
              >
                <span>Obligatoire</span>
                <div class="absolute left-1/2 -translate-x-1/2 bottom-1 w-4 h-4 flex items-center justify-center">
                  <IconCheck v-if="filterMandatory === 'yes'" :size="16" class="w-4 h-4" />
                  <IconX v-else-if="filterMandatory === 'no'" :size="16" class="w-4 h-4" />
                </div>
              </th>

              <!-- ✅ Header Actions -->
              <th v-if="isEditable" class="sticky top-0 z-20 p-4 text-center text-xs font-semibold uppercase tracking-wider text-muted w-16 bg-bg-dark">
                Actions
              </th>
            </tr>
          </thead>
          
          <tbody class="divide-y divide-bg-neutral-genesis">
            <tr 
              v-for="rel in filteredRelations" 
              :key="`${rel.parentTable}-${rel.childTable}`" 
              class="hover:bg-bg-neutral-genesis transition-colors"
            >
              <td class="p-4 font-medium text-text">
                {{ rel.parentTable }}
              </td>
              <td class="p-4 font-medium text-text">
                {{ rel.childTable }}
              </td>
              
              <!-- Colonne Formulaire -->
              <td class="p-4 text-center">
                <div class="flex justify-center items-center">
                  <template v-if="isEditable">
                    <GenesisSwitch v-model="rel.hasForm" size="sm" @update:modelValue="emitUpdate(rel, 'hasForm', $event)" />
                  </template>
                  <template v-else>
                    <IconCheck v-if="rel.hasForm" class="w-6 h-6 text-primary" />
                    <IconX v-else class="w-6 h-6 text-red-500" />
                  </template>
                </div>
              </td>
              
              <!-- Colonne Obligatoire -->
              <td class="p-4 text-center">
                <div class="flex justify-center items-center">
                  <template v-if="isEditable">
                    <GenesisSwitch v-model="rel.mandatory" size="sm" @update:modelValue="emitUpdate(rel, 'mandatory', $event)" />
                  </template>
                  <template v-else>
                    <IconCheck v-if="rel.mandatory" class="w-6 h-6 text-primary" />
                    <IconX v-else class="w-6 h-6 text-red-500" />
                  </template>
                </div>
              </td>

              <!-- Colonne Actions (Suppression) -->
              <td v-if="isEditable" class="p-4 text-center">
                <GenesisButtonIcon
                  variant="tertiary"
                  size="sm"
                  class="text-red-500 hover:text-red-600 hover:bg-red-500/10"
                  title="Supprimer cette relation"
                  @click="$emit('remove', rel)"
                >
                  <IconTrashAlt />
                </GenesisButtonIcon>
              </td>
            </tr>
          </tbody>
        </table>
      </GenesisContainer>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { RelationParameter } from '@genesis-labs/shared-types';

// UI Components
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisSwitch from '@genesis-labs/web-core/core/components/ui/inputs/GenesisSwitch.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue';
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue';

// ============================================================================
// PROPS & EMITS
// ============================================================================
const props = withDefaults(defineProps<{
  relations: RelationParameter[];
  isEditable?: boolean;
  maxHeight?: number;
}>(), {
  isEditable: false,
  maxHeight: () => 600
});

const emit = defineEmits<{
  remove: [relation: RelationParameter];
  'update-relation': [relation: RelationParameter, field: 'hasForm' | 'mandatory', value: boolean];
}>();

// ============================================================================
// ÉTATS DE FILTRAGE
// ============================================================================
const searchQuery = ref('');
const filterForm = ref<'all' | 'yes' | 'no'>('all');
const filterMandatory = ref<'all' | 'yes' | 'no'>('all');

type SearchTarget = 'all' | 'parent' | 'child';
const searchTarget = ref<SearchTarget>('all');

const searchPlaceholder = computed(() => {
  if (searchTarget.value === 'parent') return 'Rechercher dans les tables parentes...';
  if (searchTarget.value === 'child') return 'Rechercher dans les tables filles...';
  return 'Rechercher une table (Parent ou Child)...';
});

const toggleFilterForm = () => {
  if (filterForm.value === 'all') filterForm.value = 'yes';
  else if (filterForm.value === 'yes') filterForm.value = 'no';
  else filterForm.value = 'all';
};

const toggleFilterMandatory = () => {
  if (filterMandatory.value === 'all') filterMandatory.value = 'yes';
  else if (filterMandatory.value === 'yes') filterMandatory.value = 'no';
  else filterMandatory.value = 'all';
};

const toggleSearchTarget = (target: SearchTarget) => {
  searchTarget.value = searchTarget.value === target ? 'all' : target;
};

// ============================================================================
// LOGIQUE DE FILTRAGE COMBINÉE
// ============================================================================
const filteredRelations = computed(() => {
  return props.relations.filter(rel => {
    const query = searchQuery.value.toLowerCase().trim();
    let matchesSearch = true;

    if (query) {
      if (searchTarget.value === 'parent') {
        matchesSearch = rel.parentTable.toLowerCase().includes(query);
      } else if (searchTarget.value === 'child') {
        matchesSearch = rel.childTable.toLowerCase().includes(query);
      } else {
        matchesSearch = rel.parentTable.toLowerCase().includes(query) || 
                        rel.childTable.toLowerCase().includes(query);
      }
    }

    if (!matchesSearch) return false;

    if (filterForm.value !== 'all' && rel.hasForm !== (filterForm.value === 'yes')) return false;
    if (filterMandatory.value !== 'all' && rel.mandatory !== (filterMandatory.value === 'yes')) return false;

    return true;
  });
});

// ============================================================================
// ACTIONS
// ============================================================================
function emitUpdate(rel: RelationParameter, field: 'hasForm' | 'mandatory', value: boolean) {
  emit('update-relation', rel, field, value);
}
</script>

<style scoped>
/* Hérité de l'ancien composant pour la scrollbar */
:deep(.custom-scrollbar)::-webkit-scrollbar { width: 6px; }
:deep(.custom-scrollbar)::-webkit-scrollbar-track { background: transparent; }
:deep(.custom-scrollbar)::-webkit-scrollbar-thumb { background: var(--color-neutral-light, #cbd5e1); border-radius: 4px; }
:deep(.custom-scrollbar)::-webkit-scrollbar-thumb:hover { background: var(--color-neutral, #94a3b8); }
</style>