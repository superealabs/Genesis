// genesis-sdk-web/genesis-web-core/src/features/repository/store/useRepository.store.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import type { RepositoryProviderMetadata, RepositoryConfig } from '@genesis-labs/shared-types';
import { DisplayMode } from '@genesis-labs/web-core/core/components/layouts/display/items/GenesisItem.types';

export const useRepositoryStore = defineStore('repository', () => {

  // ═══════════════════════════════════════════════════════════
  // 1. ÉTAT (STATE) - Groupé par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Domaine : Données métier
  const providers = ref<RepositoryProviderMetadata[]>([]);
  const selectedProviderId = ref<string | null>(null); // ID du provider sélectionné (ex: 'github')
  
  // Domaine : Configuration (sera remplie lors de l'étape de config)
  const repositoryConfig = ref<RepositoryConfig | null>(null);

  // Domaine : Interface Utilisateur (UI)
  const searchQuery = ref('');
  const displayMode = ref<DisplayMode>('grid');
  const isLoading = ref(false);
  const detailProvider = ref<RepositoryProviderMetadata | null>(null);


  // ═══════════════════════════════════════════════════════════
  // 2. GETTERS (COMPUTED)
  // ═══════════════════════════════════════════════════════════
  
  const filteredProviders = computed(() => {
    let result = providers.value;

    if (searchQuery.value.trim()) {
      const q = searchQuery.value.toLowerCase();
      result = result.filter(p =>
        p.name.toLowerCase().includes(q) ||
        p.id.toLowerCase().includes(q)
      );
    }

    return result;
  });

  const selectedProvider = computed(() => 
    providers.value.find(p => p.id === selectedProviderId.value) || null
  );


  // ═══════════════════════════════════════════════════════════
  // 3. ACTIONS (MUTATIONS) - Groupées par domaine
  // ═══════════════════════════════════════════════════════════
  
  // Actions : Données métier
  function setProviders(data: RepositoryProviderMetadata[]) { providers.value = data; }
  function setSelectedProviderId(id: string | null) { selectedProviderId.value = id; }
  function setRepositoryConfig(config: RepositoryConfig | null) { repositoryConfig.value = config; }

  // Actions : Interface Utilisateur (UI)
  function setSearch(query: string) { searchQuery.value = query; }
  function setDisplayMode(mode: DisplayMode) { displayMode.value = mode; }
  function setLoading(state: boolean) { isLoading.value = state; }
  
  // Action : Réinitialisation (Propreté)
  function reset() {
    providers.value = [];
    selectedProviderId.value = null;
    repositoryConfig.value = null;
    searchQuery.value = '';
    displayMode.value = 'grid';
    isLoading.value = false;
    detailProvider.value = null;
  }


  // ═══════════════════════════════════════════════════════════
  // 4. RETURN - Ordre identique à la déclaration
  // ═══════════════════════════════════════════════════════════
  return {
    // État
    providers, 
    selectedProviderId, 
    repositoryConfig, 
    searchQuery, 
    displayMode, 
    isLoading, 
    detailProvider,
    
    // Getters
    filteredProviders, 
    selectedProvider,
    
    // Actions
    setProviders, 
    setSelectedProviderId, 
    setRepositoryConfig, 
    setSearch, 
    setDisplayMode, 
    setLoading, 
    reset
  };
});