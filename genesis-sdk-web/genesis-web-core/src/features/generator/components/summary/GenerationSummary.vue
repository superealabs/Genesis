<template>
  <!-- Image toujours à droite : rangée non repliable, colonne droite à largeur fixe (shrink-0) -->
  <main class="flex flex-row gap-4 h-full w-full p-8">

    <!-- ═══ GAUCHE : informations et listes (Scrollable indépendamment) ═══ -->
    <div class="flex-1 min-w-0 min-h-0 overflow-y-auto custom-scrollbar flex flex-col">
      <div class="my-auto flex flex-col gap-8 py-4">

        <!-- 1. En-tête et Filtres -->
        <header class="flex flex-col gap-4">
          <div class="flex flex-col gap-2">
            <span class="text-xs font-semibold uppercase tracking-widest text-muted">Tables et Vues</span>
            <h1 class="text-4xl font-bold leading-tight text-text break-words">
              Entités configurées
            </h1>
            <p class="text-sm text-text-muted">
              Aperçu des entités de base de données et de leur profil d'assignation.
            </p>
          </div>
        </header>

        <!-- 2. Liste des Tables et Vues + Récapitulatif des Profils -->
        <section class="flex gap-6 pr-2">
          
          <!-- Colonne gauche : Liste des entités -->
          <div class="w-2/3 flex flex-col gap-2">
            <div class="flex flex-row gap-3 max-w-2xl">
              <GenesisInput
                v-model="tableSearchQuery"
                type="text"
                placeholder="Rechercher une table ou une vue..."
                size="md"
                class="flex-1"
                :fill-width="true"
              >
                <template #left>
                  <IconSearch :size="16" class="text-text-muted" />
                </template>
              </GenesisInput>

              <GenesisSegmentedControl v-model="activeFilter" :options="filterOptions" size="md" />
            </div>
            
            <GenesisEntityTable
                :items="filteredCombinedItems"
                :right-text="(item) => getAssignedProfile(item.tableName, item.isView)"
                :min-height="300"
                :max-height="maxHeight()"
                empty-text="Aucune table ou vue ne correspond à cette recherche."
            />
          </div>

          <!-- Colonne droite : Récapitulatif des Profils et leurs composants -->
          <div class="w-1/3 flex flex-col gap-4">
            <h3 class="text-base font-semibold text-text flex items-center gap-2">
              Profils de Génération
            </h3>
            
            <!-- ✅ MODIFIÉ : selected-config-id lié à l'état local, et écoute de l'événement select-configuration -->
            <GenesisConfigurationPanel
              :configurations="summaryConfigs"
              :selected-config-id="selectedProfileId"
              :filtered-configs="summaryConfigs"
              :search-query="''"
              :can-move-up="false"
              :can-move-down="false"
              :readonly="true"
              width="w-full"
              @select-configuration="handleProfileSelect"
            />

            <!-- ✅ NOUVEAU : Affichage des composants du profil sélectionné (Lecture seule) -->
            <div v-if="selectedProfileId !== null" class="p-4 rounded-lg border border-bg-neutral-genesis flex flex-col gap-2">
              <div class="flex items-center justify-between">
                <h4 class="text-sm font-semibold text-text">
                  Composants pour : <span class="text-primary">{{ selectedProfileName }}</span>
                </h4>
              </div>
              
              <div class="flex flex-col flex-wrap gap-2">
                <span 
                  v-for="comp in selectedProfileComponents" 
                  :key="comp"
                  class="inline-flex items-center px-2.5 py-1 rounded-md text-xs font-medium"
                >
                  {{ getComponentLabel(comp) }}
                </span>
                <span v-if="selectedProfileComponents.length === 0" class="text-xs text-muted italic">
                  Aucun composant sélectionné
                </span>
              </div>
            </div>
            
            <!-- Message d'invitation si rien n'est sélectionné -->
            <div v-else class="p-4 rounded-lg border border-dashed border-bg-neutral-genesis text-center">
              <p class="text-xs text-muted">Sélectionnez un profil ci-dessus pour voir ses composants.</p>
            </div>

          </div>
        </section>

      </div>
    </div>

    <!-- ═══ DROITE : visuel (statique, ne rétrécit jamais sous sa largeur) ═══ -->
    <div 
      class="w-2/5 min-w-[280px] shrink-0 flex flex-col items-center justify-center rounded-lg overflow-hidden relative min-h-[300px]" 
      :class="generationIllustration ? '' : 'border border-secondary border-dashed bg-secondary/10'"
    >

      <img 
        v-if="generationIllustration"
        :src="generationIllustration" 
        alt="Generation" 
        class="absolute inset-0 w-full h-full object-cover"
      />
      <template v-else>
        <svg class="w-12 h-12 opacity-60" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4"/>
        </svg>
        <span class="text-sm font-medium">Aperçu des entités</span>
        <span class="text-xs text-muted text-center px-4">La représentation visuelle du schéma de base de données sera intégrée ici.</span>
      </template>

    </div>

  </main>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import type { TableMetadataDto } from '@genesis-labs/shared-types';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';

// UI Components
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisConfigurationPanel from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationPanel.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import GenesisEntityTable from '@genesis-labs/web-core/core/components/layouts/display/table/GenesisEntityTable.vue';

import generationIllustration from '../../assets/generation-illustration.png';

const { tables, views, stepperData } = useGenerator();

const maxHeight = () => window.innerHeight * 0.5;

const tableSearchQuery = ref('');
const activeFilter = ref<'all' | 'table' | 'view'>('all');

// ✅ État pour suivre le profil sélectionné dans le panneau de droite
const selectedProfileId = ref<string | number | null>(null);

const handleProfileSelect = (id: string | number) => {
  if (selectedProfileId.value === id) {
    selectedProfileId.value = null; // Désélectionne si on clique sur le profil déjà actif
  } else {
    selectedProfileId.value = id;   // Sélectionne le nouveau profil
  }
};

const filterOptions = [
  { label: 'Tous', value: 'all' },
  { label: 'Tables', value: 'table' },
  { label: 'Vues', value: 'view' }
];

// Type local enrichi pour inclure les composants
interface SummaryConfig {
  id: string | number;
  name: string;
  components: string[];
  isHidden: boolean;
}

// Construction de la liste uniquement à partir des entités assignées
const assignedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => {
  const params = stepperData.value.generationParameters;
  const result: (TableMetadataDto & { type: 'table' | 'view' })[] = [];
  const seen = new Set<string>();

  params.forEach(param => {
    param.selectedTables.forEach(tableName => {
      if (!seen.has(tableName)) {
        const meta = tables.value.find(t => t.tableName === tableName);
        if (meta) {
          result.push({ ...meta, type: 'table' });
          seen.add(tableName);
        }
      }
    });
    
    param.selectedViews.forEach(viewName => {
      if (!seen.has(viewName)) {
        const meta = views.value.find(v => v.tableName === viewName);
        if (meta) {
          result.push({ ...meta, type: 'view' });
          seen.add(viewName);
        }
      }
    });
  });

  return result;
});

// ✅ MODIFIÉ : Ajout de la règle de filtrage par profil sélectionné
const filteredCombinedItems = computed(() => {
  let items = assignedItems.value;

  // 🆕 RÈGLE : Si un profil est sélectionné, on ne garde que ses entités
  if (selectedProfileId.value !== null) {
    const profile = summaryConfigs.value.find(c => c.id === selectedProfileId.value);
    if (profile) {
      // On retrouve le paramètre de génération correspondant à ce profil
      const param = stepperData.value.generationParameters.find(p => p.profileName === profile.name);
      if (param) {
        items = items.filter(item => {
          return item.type === 'table' 
            ? param.selectedTables.includes(item.tableName)
            : param.selectedViews.includes(item.tableName);
        });
      }
    }
  }

  // 🔄 RÈGLES EXISTANTES : Filtres de type (Table/Vue)
  if (activeFilter.value === 'table') {
    items = items.filter(item => item.type === 'table');
  } else if (activeFilter.value === 'view') {
    items = items.filter(item => item.type === 'view');
  }
  
  // 🔄 RÈGLES EXISTANTES : Filtre par recherche textuelle
  if (tableSearchQuery.value.trim()) {
    const query = tableSearchQuery.value.toLowerCase();
    items = items.filter(item => item.tableName.toLowerCase().includes(query));
  }
  
  return items;
});

const getAssignedProfile = (tableName: string, isView: boolean): string => {
  const param = stepperData.value.generationParameters.find(p => 
    isView ? p.selectedViews.includes(tableName) : p.selectedTables.includes(tableName)
  );
  return param ? param.profileName : 'Non assigné';
};

// On inclut les composants dans l'objet de configuration
const summaryConfigs = computed<SummaryConfig[]>(() => 
  stepperData.value.generationParameters.map((p, index) => ({
    id: index,
    name: p.profileName,
    components: p.selectedComponents,
    isHidden: false
  }))
);

// Computed pour récupérer facilement les données du profil sélectionné
const selectedProfile = computed(() => 
  summaryConfigs.value.find(c => c.id === selectedProfileId.value)
);

const selectedProfileName = computed(() => selectedProfile.value?.name || '');
const selectedProfileComponents = computed(() => selectedProfile.value?.components || []);

// Utilitaire pour formater l'affichage des composants (ex: 'dao' -> 'DAO')
const getComponentLabel = (comp: string): string => {
  const labels: Record<string, string> = {
    model: 'Model',
    dao: 'DAO',
    service: 'Service',
    controller: 'Controller'
  };
  return labels[comp] || comp;
};
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