// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useConfigurationManager.ts
import { ref, computed } from 'vue';

// ============================================================================
// TYPES & INTERFACES
// ============================================================================

export interface ConfigurationItem {
  id: string | number;
  name: string;
  isHidden: boolean;
  components: string[];
}

export interface UseConfigurationManagerOptions {
  singleConfiguration?: boolean;
}

// ============================================================================
// COMPOSABLE PRINCIPAL
// ============================================================================

export function useConfigurationManager(
  initialConfigs: ConfigurationItem[] = [],
  options: UseConfigurationManagerOptions = { singleConfiguration: true }
) {
  // --- 1. État Réactif ---
  const configurations = ref<ConfigurationItem[]>(initialConfigs);
  const selectedConfigId = ref<string | number | null>(null);
  const searchQuery = ref('');
  const { singleConfiguration = true } = options;

  // --- 2. Computeds (Données dérivées) ---

  /**
   * Filtre la liste des configurations en fonction de la requête de recherche.
   * Retourne la liste complète si la recherche est vide.
   */
  const filteredConfigs = computed(() => {
    if (!searchQuery.value.trim()) return configurations.value;
    const query = searchQuery.value.toLowerCase();
    return configurations.value.filter(c => c.name.toLowerCase().includes(query));
  });

  /**
   * Indique s'il est possible de déplacer la sélection vers l'élément précédent.
   */
  const canMoveUp = computed(() => {
    if (!selectedConfigId.value) return false;
    const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
    return index > 0;
  });

  /**
   * Indique s'il est possible de déplacer la sélection vers l'élément suivant.
   */
  const canMoveDown = computed(() => {
    if (!selectedConfigId.value) return false;
    const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
    return index < configurations.value.length - 1;
  });

  // --- 3. Actions ---

  /**
   * Génère un nom unique pour une nouvelle configuration en incrémentant un compteur
   * jusqu'à trouver un nom qui n'existe pas encore dans la liste.
   */
  function generateUniqueName(): string {
    const baseName = 'config';
    let counter = 1;
    let proposedName = `${baseName}${counter}`;
    
    while (configurations.value.some(c => c.name === proposedName)) {
      counter++;
      proposedName = `${baseName}${counter}`;
    }
    
    return proposedName;
  }

  /**
   * Ajoute une nouvelle configuration et la sélectionne automatiquement.
   */
  function addConfiguration(components: string[] = []): ConfigurationItem {
    const newConfig: ConfigurationItem = {
      id: Date.now(),
      name: generateUniqueName(),
      isHidden: false,
      components
    };
    
    configurations.value.push(newConfig);
    selectedConfigId.value = newConfig.id;
    
    return newConfig;
  }

  /**
   * Supprime une configuration. Si l'élément supprimé était sélectionné, 
   * la sélection est automatiquement transférée à l'élément le plus proche en haut de la liste.
   */
  function deleteConfiguration(id: string | number): void {
    const index = configurations.value.findIndex(c => c.id === id);
    if (index !== -1) {
      const wasSelected = selectedConfigId.value === id;
      
      configurations.value.splice(index, 1);
      
      if (wasSelected) {
        if (configurations.value.length === 0) {
          selectedConfigId.value = null;
        } else {
          const newIndex = index > 0 ? index - 1 : 0;
          selectedConfigId.value = configurations.value[newIndex].id;
        }
      }
    }
  }

  function renameConfiguration(id: string | number, newName: string): void {
    const trimmed = newName.trim();
    if (!trimmed) return;

    const config = configurations.value.find(c => c.id === id);
    if (config) {
      config.name = trimmed;
    }
  }

  function editConfiguration(id: string | number, components: string[]): void {
    const config = configurations.value.find(c => c.id === id);
    if (config) {
      config.components = components;
    }
  }

  function toggleVisibility(id: string | number): void {
    const config = configurations.value.find(c => c.id === id);
    if (config) {
      config.isHidden = !config.isHidden;
    }
  }

  /**
   * Déplace la sélection vers l'élément précédent dans la liste.
   * Note : Cette fonction modifie uniquement l'ID sélectionné, elle ne réorganise pas le tableau.
   */
  function moveUp(): void {
    if (!selectedConfigId.value) return;
    const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
    if (index > 0) {
      selectedConfigId.value = configurations.value[index - 1].id;
    }
  }

  /**
   * Déplace la sélection vers l'élément suivant dans la liste.
   * Note : Cette fonction modifie uniquement l'ID sélectionné, elle ne réorganise pas le tableau.
   */
  function moveDown(): void {
    if (!selectedConfigId.value) return;
    const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
    if (index < configurations.value.length - 1) {
      selectedConfigId.value = configurations.value[index + 1].id;
    }
  }

  function selectConfiguration(id: string | number | null): void {
    selectedConfigId.value = id;
  }

  /**
   * Assigne des éléments (ex: tables) à une configuration spécifique.
   * Si l'option singleConfiguration est active, l'élément est d'abord retiré de 
   * toutes les autres configurations pour garantir qu'il n'appartient qu'à une seule.
   */
  function assignToConfig(id: string | number, items: string[]): void {
    const targetConfig = configurations.value.find(c => c.id === id);
    if (!targetConfig) return;

    items.forEach(item => {
      if (singleConfiguration) {
        configurations.value.forEach(config => {
          const index = config.components.indexOf(item);
          if (index !== -1) {
            config.components.splice(index, 1);
          }
        });
      }
      
      if (!targetConfig.components.includes(item)) {
        targetConfig.components.push(item);
      }
    });
  }

  /**
   * Retire des éléments d'une configuration spécifique.
   */
  function removeFromConfig(id: string | number, items: string[]): void {
    const config = configurations.value.find(c => c.id === id);
    if (config) {
      config.components = config.components.filter(c => !items.includes(c));
    }
  }

  // --- 4. Retour ---
  return {
    // État
    configurations,
    selectedConfigId,
    searchQuery,
    
    // Computeds
    filteredConfigs,
    canMoveUp,
    canMoveDown,
    
    // Actions
    addConfiguration,
    deleteConfiguration,
    renameConfiguration,
    editConfiguration,
    toggleVisibility,
    moveUp,
    moveDown,
    selectConfiguration,
    assignToConfig,
    removeFromConfig
  };
}