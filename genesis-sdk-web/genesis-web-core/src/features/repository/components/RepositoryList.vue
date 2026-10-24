<template>
  <!-- État vide global -->
  <div v-if="providers.length === 0" class="p-6 text-center text-muted bg-bg-light rounded-lg">
    Aucun fournisseur de dépôt disponible.
  </div>

  <!-- ═══ SECTION DÉDIÉE : FOURNISSEURS RÉCENTS ═══ -->
  <GenesisDisclosure
    v-if="recentProviders.length > 0"
    title="Récents"
    :default-open="true"
    variant="secondary"
    class="mb-6"
  >
    <template #title>
      <span class="text-2xl font-semibold">Récemment</span>
      <span class="text-xs font-normal text-muted bg-bg-light px-2 py-0.5 rounded-full border border-secondary">
        {{ recentProviders.length }}
      </span>
    </template>

    <div class="section-body">
      <GenesisList :display="display" minColWidth="100px">
        <GenesisItem
          v-for="provider in recentProviders"
          :key="`recent-${provider.id}`"
          :label="provider.name"
          :selected="selectedId === provider.id"
          :badge="providerSlots?.get(provider.id) ?? null"
          :size="'sm'"
          @click="$emit('select', provider, $event)"
          @info="$emit('info', provider)"
        >
          <template #default>
            <td class="p-3 text-center">{{ provider.name }}</td>
          </template>
        </GenesisItem>
      </GenesisList>
    </div>
  </GenesisDisclosure>

  <!-- ═══ LISTE GROUPÉE GÉNÉRIQUE (TOUS LES FOURNISSEURS) ═══ -->
  <GenesisGroupedList
    v-if="providers.length > 0"
    :items="providers"
    :group-by="groupBy"
    :group-options="groupOptions"
    :group-labels="groupLabels"
    :display="display"
    minColWidth="200px"
    @update:groupBy="$emit('update:groupBy', $event)"
  >
    <template #default="{ items }">
        <GenesisItem
          v-for="provider in items"
          :key="provider.id"
          :label="provider.name"
          :selected="selectedId === provider.id"
          :badge="providerSlots?.get(provider.id) ?? null"
          :show-info-button="true"
          @click="$emit('select', provider, $event)"
          @info="$emit('info', provider)"
        >
          <template #default>
            <td class="p-3 text-center">{{ provider.name }}</td>
          </template>
        </GenesisItem>
    </template>
  </GenesisGroupedList>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { RepositoryProviderMetadata } from '@genesis-labs/shared-types';
import type { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';

import GenesisGroupedList from '@genesis-labs/web-core/core/components/layouts/display/GenesisGroupedList.vue';
import GenesisList from '@genesis-labs/web-core/core/components/layouts/display/GenesisList.vue';
import GenesisItem from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.vue';
import GenesisDisclosure from '@genesis-labs/web-core/core/components/layouts/GenesisDisclosure.vue';

// ✅ Import du composable de récence
import { useRecency } from '@genesis-labs/web-core/core/features/recent/composables/useRecency';

// ============================================================================
// 1. PROPS
// ============================================================================
const props = defineProps<{
  providers: RepositoryProviderMetadata[];
  selectedId?: string; // ✅ ID de type string pour les providers
  display: DisplayMode;
  providerSlots?: Map<string, string>; // ✅ Map avec clé string
  
  groupBy?: keyof RepositoryProviderMetadata | null;
  groupOptions?: { label: string; value: keyof RepositoryProviderMetadata | null }[];
  groupLabels?: Record<string | number, string>;
}>();

// ============================================================================
// 2. EMITS
// ============================================================================
defineEmits<{
  select: [provider: RepositoryProviderMetadata, event?: MouseEvent];
  info: [provider: RepositoryProviderMetadata];
  'update:groupBy': [value: keyof RepositoryProviderMetadata | null];
}>();

// ============================================================================
// 3. LOGIQUE DE RÉCENCE
// ============================================================================
// const { recentFrontends: recentProviderItems } = useRecency(); 
// Note: On utilise recentFrontends ou on adapte useRecency pour avoir recentProviders. 
// Pour l'instant, supposons que le store de récence gère une catégorie 'repositories' 
// ou qu'on mappe depuis une catégorie générique. Adaptons le computed pour être sûr :

const { recencyData } = useRecency();

const recentProviders = computed<RepositoryProviderMetadata[]>(() => {
  // On suppose que les providers récents sont stockés sous une clé 'repositories' ou 'frameworks' par héritage.
  // Ajuste 'repositories' selon la clé exacte que tu utiliseras dans le store de récence.
  const recentItems = recencyData.value.repositories || []; 
  
  return recentItems
    .map(recent => props.providers.find(p => p.id === recent.id))
    .filter((p): p is RepositoryProviderMetadata => p !== undefined);
});
</script>

<style scoped>
.section-body {
  padding-top: 1rem; 
}
</style>