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

  const filteredConfigs = computed(() => {
    if (!searchQuery.value.trim()) return configurations.value;
    const query = searchQuery.value.toLowerCase();
    return configurations.value.filter(c => c.name.toLowerCase().includes(query));
  });

  /**
   * Vérifie si on peut monter la sélection dans la liste FILTRÉE.
   */
  const canMoveUp = computed(() => {
    if (!selectedConfigId.value) return false;
    const index = filteredConfigs.value.findIndex(c => c.id === selectedConfigId.value);
    return index > 0;
  });

  /**
   * Vérifie si on peut descendre la sélection dans la liste FILTRÉE.
   */
  const canMoveDown = computed(() => {
    if (!selectedConfigId.value) return false;
    const index = filteredConfigs.value.findIndex(c => c.id === selectedConfigId.value);
    return index < filteredConfigs.value.length - 1;
  });

  // --- 3. Actions ---

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

  function deleteConfiguration(id: string | number): void {
    const index = configurations.value.findIndex(c => c.id === id);
    if (index !== -1) {
      const wasSelected = selectedConfigId.value === id;
      
      configurations.value.splice(index, 1);
      
      if (wasSelected) {
        if (configurations.value.length === 0) {
          selectedConfigId.value = null;
        } else {
          // Sélectionne l'élément précédent, ou le premier si on était au début
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
   * Déplace la sélection vers le haut dans la liste FILTRÉE.
   */
  function moveUp(): void {
    if (!selectedConfigId.value) return;
    const index = filteredConfigs.value.findIndex(c => c.id === selectedConfigId.value);
    if (index > 0) {
      selectedConfigId.value = filteredConfigs.value[index - 1].id;
    }
  }

  /**
   * Déplace la sélection vers le bas dans la liste FILTRÉE.
   */
  function moveDown(): void {
    if (!selectedConfigId.value) return;
    const index = filteredConfigs.value.findIndex(c => c.id === selectedConfigId.value);
    if (index < filteredConfigs.value.length - 1) {
      selectedConfigId.value = filteredConfigs.value[index + 1].id;
    }
  }

  /**
   * Gère la sélection ET la désélection (toggle).
   * Si l'ID cliqué est déjà sélectionné, on le désélectionne (null).
   */
  function selectConfiguration(id: string | number | null): void {
    if (selectedConfigId.value === id) {
      selectedConfigId.value = null; // Désélection
    } else {
      selectedConfigId.value = id;   // Sélection
    }
  }

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

function removeFromConfig(id: string | number, items: string[]): void {
  if (singleConfiguration) {
    // Mode exclusif : on cherche dans TOUTES les configurations
    // car l'item pourrait être dans une config différente de celle sélectionnée
    configurations.value.forEach(config => {
      config.components = config.components.filter(c => !items.includes(c));
    });
  } else {
    // Mode libre : on retire uniquement de la configuration ciblée
    const config = configurations.value.find(c => c.id === id);
    if (config) {
      config.components = config.components.filter(c => !items.includes(c));
    }
  }
}

  // --- 4. Retour ---
  return {
    configurations,
    selectedConfigId,
    searchQuery,
    filteredConfigs,
    canMoveUp,
    canMoveDown,
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