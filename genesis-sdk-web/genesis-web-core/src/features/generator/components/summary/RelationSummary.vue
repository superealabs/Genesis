<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-12 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations et listes (Scrollable indépendamment) ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-8 py-4">

        <!-- 1. En-tête et Filtres -->
        <header class="flex flex-col gap-4">
          <div class="flex flex-col gap-2">
            <span class="text-xs font-semibold uppercase tracking-widest text-muted">Relations</span>
            <h1 class="text-4xl font-bold leading-tight text-text break-words">
              Liaisons configurées
            </h1>
            <p class="text-sm text-text-muted">
              Aperçu des liaisons parent-enfant configurées entre les entités de votre base de données.
            </p>
          </div>

          <!-- Barre de recherche (les filtres booléens sont maintenant dans les headers) -->
          <div class="flex flex-wrap gap-3 items-end">
            <div class="flex-1 min-w-[250px]">
              <GenesisInput
                v-model="searchQuery"
                type="text"
                :placeholder="searchPlaceholder"
                size="md"
                fill-width
              >
                <template #left>
                  <IconSearch :size="16" class="text-text-muted" />
                </template>
              </GenesisInput>
            </div>
          </div>
        </header>

        <!-- 2. Tableau des Relations (via GenesisContainer) -->
        <section>
          <GenesisContainer
            :min-height="300"
            :max-height="maxHeight()"
            :is-empty="filteredRelations.length === 0"
            empty-text="Aucune relation ne correspond à ces critères."
          >
            <table class="w-full text-left text-sm">
              <thead class="sticky top-0 z-10 bg-bg-secondary/20">
                <tr>
                  <!-- Header Parent cliquable -->
                  <th 
                    class="p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                    :class="searchTarget === 'parent' ? 'bg-bg-secondary text-secondary' : 'text-muted hover:text-text hover:bg-bg-secondary/50'"
                    @click="toggleSearchTarget('parent')"
                  >
                    Table Parent (Mère)
                    <IconSearch v-if="searchTarget === 'parent'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                  </th>

                  <!-- Header Child cliquable -->
                  <th 
                    class="p-4 text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group"
                    :class="searchTarget === 'child' ? 'bg-bg-secondary text-secondary' : 'text-muted hover:text-text hover:bg-bg-secondary/50'"
                    @click="toggleSearchTarget('child')"
                  >
                    Table Child (Fille)
                    <IconSearch v-if="searchTarget === 'child'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                  </th>

                  <!-- ✅ Header Formulaire cliquable (3 états) -->
                  <th 
                    class="p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group hover:bg-bg-secondary/50"
                    :class="{
                      'text-muted': filterForm === 'all',
                      'bg-bg-secondary text-secondary': filterForm === 'yes',
                      'bg-red-500/10 text-red-500': filterForm === 'no'
                    }"
                    @click="toggleFilterForm"
                  >
                    Formulaire
                    <IconCheck :size="iconSize" v-if="filterForm === 'yes'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                    <IconX :size="iconSize" v-if="filterForm === 'no'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                  </th>

                  <!-- ✅ Header Obligatoire cliquable (3 états) -->
                  <th 
                    class="p-4 text-center text-xs font-semibold uppercase tracking-wider cursor-pointer transition-colors relative group hover:bg-bg-secondary/50"
                    :class="{
                      'text-muted': filterMandatory === 'all',
                      'bg-bg-secondary text-secondary': filterMandatory === 'yes',
                      'bg-red-500/10 text-red-500': filterMandatory === 'no'
                    }"
                    @click="toggleFilterMandatory"
                  >
                    Obligatoire
                    <IconCheck :size="iconSize" v-if="filterMandatory === 'yes'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                    <IconX :size="iconSize" v-if="filterMandatory === 'no'" class="absolute top-3 right-3 w-3.5 h-3.5" />
                  </th>
                </tr>
              </thead>
              <tbody class="divide-y divide-bg-neutral-genesis">
                <tr 
                  v-for="(rel, index) in filteredRelations" 
                  :key="`${rel.parentTable}-${rel.childTable}-${index}`" 
                  class="hover:bg-bg-secondary/50 transition-colors"
                >
                  <td class="p-4 font-medium text-text">
                    {{ rel.parentTable }}
                  </td>
                  <td class="p-4 font-medium text-text">
                    {{ rel.childTable }}
                  </td>
                  
                  <td class="p-4 text-center">
                    <div class="flex justify-center items-center">
                      <IconCheck v-if="rel.hasForm" class="w-5 h-5 text-primary" />
                      <IconX v-else class="w-5 h-5 text-red-500" />
                    </div>
                  </td>
                  
                  <td class="p-4 text-center">
                    <div class="flex justify-center items-center">
                      <IconCheck v-if="rel.mandatory" class="w-5 h-5 text-primary" />
                      <IconX v-else class="w-5 h-5 text-red-500" />
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </GenesisContainer>
        </section>

      </div>
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center gap-2 text-muted border border-secondary border-dashed bg-secondary/10 rounded-lg">
      <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13.828 10.172a4 4 0 00-5.656 0l-4 4a4 4 0 105.656 5.656l1.102-1.101m-.758-4.899a4 4 0 005.656 0l4-4a4 4 0 00-5.656-5.656l-1.1 1.1"/>
      </svg>
      <span class="text-sm font-medium">Aperçu des relations</span>
      <span class="text-xs text-muted text-center px-4">La représentation visuelle du diagramme de relations sera intégrée ici.</span>
    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

// UI Components
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import IconCheck from '@genesis-labs/web-core/core/components/ui/icons/IconCheck.vue';

const { getRelations } = useGenerator();

const relations = computed(() => getRelations.value || []);
const maxHeight = () => window.innerHeight * 0.5;

// ============================================================================
// ÉTATS DE FILTRAGE
// ============================================================================
const searchQuery = ref('');
const filterForm = ref<'all' | 'yes' | 'no'>('all');
const filterMandatory = ref<'all' | 'yes' | 'no'>('all');

const iconSize = 32;

type SearchTarget = 'all' | 'parent' | 'child';
const searchTarget = ref<SearchTarget>('all');

// ✅ NOUVEAU : Placeholder dynamique
const searchPlaceholder = computed(() => {
  if (searchTarget.value === 'parent') return 'Rechercher dans les tables parentes...';
  if (searchTarget.value === 'child') return 'Rechercher dans les tables filles...';
  return 'Rechercher une table (Parent ou Child)...';
});

// ✅ NOUVEAU : Fonctions de cycle pour les filtres booléens (all -> yes -> no -> all)
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
  if (searchTarget.value === target) {
    searchTarget.value = 'all';
  } else {
    searchTarget.value = target;
  }
};

// ============================================================================
// LOGIQUE DE FILTRAGE COMBINÉE
// ============================================================================
const filteredRelations = computed(() => {
  return relations.value.filter(rel => {
    // 1. Filtre de recherche textuelle ciblée
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

    // 2. Filtre Formulaire
    if (filterForm.value !== 'all') {
      const isYes = filterForm.value === 'yes';
      if (rel.hasForm !== isYes) return false;
    }

    // 3. Filtre Obligatoire
    if (filterMandatory.value !== 'all') {
      const isYes = filterMandatory.value === 'yes';
      if (rel.mandatory !== isYes) return false;
    }

    return true;
  });
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { 
  width: 6px; 
}
.custom-scrollbar::-webkit-scrollbar-track { 
  background: transparent; 
}
.custom-scrollbar::-webkit-scrollbar-thumb { 
  background: var(--color-neutral-light); 
  border-radius: 4px; 
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover { 
  background: var(--color-neutral); 
}
</style>