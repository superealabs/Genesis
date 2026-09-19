import { ref, computed } from 'vue';

export interface ConfigurationItem {
    id: string | number;
    name: string;
    isHidden: boolean;
    components: string[];
}

export interface UseConfigurationManagerOptions {
    singleConfiguration?: boolean;
}


export function useConfigurationManager(
    initialConfigs: ConfigurationItem[] = [],
    options: UseConfigurationManagerOptions = { singleConfiguration: true }
) {
    const configurations = ref<ConfigurationItem[]>(initialConfigs);
    const selectedConfigId = ref<string | number | null>(null);
    const searchQuery = ref('');
    const { singleConfiguration = true } = options;

    // Computed pour filtrer les configurations par recherche
    const filteredConfigs = computed(() => {
        if (!searchQuery.value.trim()) return configurations.value;
        const query = searchQuery.value.toLowerCase();
        return configurations.value.filter(c => c.name.toLowerCase().includes(query));
    });
    
    // Génère un nom unique pour une nouvelle configuration
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

    // Ajoute une nouvelle configuration
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

    //  MODIFIÉ : Supprime une configuration et sélectionne automatiquement la plus proche du haut (index 0)
    function deleteConfiguration(id: string | number): void {
        const index = configurations.value.findIndex(c => c.id === id);
        if (index !== -1) {
            const wasSelected = selectedConfigId.value === id;
            
            // 1. On supprime l'élément
            configurations.value.splice(index, 1);
            
            // 2. Si c'était l'élément sélectionné, on en choisit un nouveau
            if (wasSelected) {
                if (configurations.value.length === 0) {
                    selectedConfigId.value = null; // Plus rien à sélectionner
                } else {
                    // On privilégie l'élément du dessus (index - 1). 
                    // Si on a supprimé le premier (index 0), on prend le nouveau premier (index 0).
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

    //  MODIFIÉ : Change la sélection vers l'élément du dessus (ne réorganise plus le tableau)
    function moveUp(): void {
        if (!selectedConfigId.value) return;
        const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
        if (index > 0) {
            selectedConfigId.value = configurations.value[index - 1].id;
        }
    }

    //  MODIFIÉ : Change la sélection vers l'élément du dessous (ne réorganise plus le tableau)
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

    //  MODIFIÉ : Vérifie s'il y a un élément au-dessus à sélectionner
    const canMoveUp = computed(() => {
        if (!selectedConfigId.value) return false;
        const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
        return index > 0;
    });

    //  MODIFIÉ : Vérifie s'il y a un élément en dessous à sélectionner
    const canMoveDown = computed(() => {
        if (!selectedConfigId.value) return false;
        const index = configurations.value.findIndex(c => c.id === selectedConfigId.value);
        return index < configurations.value.length - 1;
    });


    function assignToConfig(id: string | number, items: string[]): void {
        const targetConfig = configurations.value.find(c => c.id === id);
        if (!targetConfig) return;

        items.forEach(item => {
            // 1. Si la contrainte est active, on retire l'élément de TOUTES les configurations existantes
            if (singleConfiguration) {
                configurations.value.forEach(config => {
                    const index = config.components.indexOf(item);
                    if (index !== -1) {
                        config.components.splice(index, 1);
                    }
                });
            }
            
            // 2. Ensuite, on l'ajoute à la configuration cible (si pas déjà présent)
            if (!targetConfig.components.includes(item)) {
                targetConfig.components.push(item);
            }
        });
    }

    function removeFromConfig(id: string | number, items: string[]): void {
        const config = configurations.value.find(c => c.id === id);
        if (config) {
            config.components = config.components.filter(c => !items.includes(c));
        }
    }

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