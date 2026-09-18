<template>
    <div class="flex gap-6 p-4 max-w-5xl mx-auto h-full">

        <!-- ═══ SECTION 3 : Tables et Vues ═══ -->
        <div class="space-y-3 flex-1 min-h-0 flex flex-col">
            <div class="flex items-center justify-between flex-shrink-0">
                <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                    Tables et Vues à inclure
                    <span class="text-accent text-sm font-normal">*</span>
                </h3>
                <button 
                    v-if="tables.length > 0"
                    type="button"
                    class="text-xs text-accent hover:text-accent/80 font-medium transition-colors"
                    @click="toggleAllTables"
                >
                    {{ areAllTablesSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
                </button>
            </div>
            
            <p class="text-sm text-text-muted flex-shrink-0">Choisissez les entités de votre base de données à inclure.</p>

            <div class="relative flex-shrink-0">
                <GenesisInput
                    v-model="tableSearchQuery"
                    type="text"
                    placeholder="Rechercher une table ou une vue..."
                    size="sm"
                    fill-width
                >
                    <template #left>
                        <!-- Assure-toi d'importer IconSearch dans le script si ce n'est pas déjà fait -->
                        <IconSearch :size="16" class="text-text-muted" />
                    </template>
                </GenesisInput>
            </div>

            <div class="border border-secondary rounded-md overflow-hidden bg-bg-light flex-1 min-h-0 flex flex-col">
                <div v-if="isLoading" class="p-6 text-center text-text-muted text-sm flex items-center justify-center gap-2">
                    <span class="animate-spin">⟳</span> Chargement des métadonnées...
                </div>

                <template v-else>
                    <div class="flex-1 overflow-y-auto custom-scrollbar">
                        <div 
                            v-for="item in filteredCombinedItems" 
                            :key="item.tableName"
                            class="flex items-center justify-between p-3 border-b border-secondary last:border-b-0 hover:bg-secondary/30 transition-colors cursor-pointer"
                            @click="toggleItem(item)"
                        >
                            <div class="flex items-center gap-3 flex-1 min-w-0">
                                <GenesisCheckboxSimple :model-value="isItemSelected(item)" />
                                <div class="flex flex-col min-w-0">
                                    <span class="text-sm font-medium text-text truncate">{{ item.tableName }}</span>
                                    <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
                                </div>
                            </div>
                            
                            <div 
                                class="flex-shrink-0 text-xs text-text-muted ml-4 text-right min-w-[120px] truncate" 
                                :title="getAssignedConfig(item)"
                            >
                                {{ getAssignedConfig(item) }}
                            </div>
                        </div>


                        <div v-if="filteredCombinedItems.length === 0" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
                            {{ combinedItems.length === 0 ? 'Aucune table ou vue trouvée.' : 'Aucun résultat pour cette recherche.' }}
                        </div>
                    </div>
                    <div v-if="combinedItems.length === 0" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
                        Aucune table ou vue trouvée.
                    </div>
                </template>
            </div>
        </div>

        
        <!-- ═══ SECTION 1 : Panneau de Gestion des Configurations a ═══ -->
        <section class="flex flex-col gap-2">
            <div class="w-fit">
                <h3 class="text-lg font-semibold text-text mb-3 flex items-center gap-2">
                    Profils de Génération
                    <span class="text-accent text-sm font-normal">*</span>
                </h3>
                
                <!--  REMPLACÉ par GenesisConfigurationSelector -->
                <GenesisConfigurationSelector
                    :configurations="configManager.configurations.value"
                    :selected-config-id="configManager.selectedConfigId.value"
                    :filtered-configs="configManager.filteredConfigs.value"
                    :search-query="configManager.searchQuery.value"
                    :can-move-up="configManager.canMoveUp.value"
                    :can-move-down="configManager.canMoveDown.value"
                    :selected-items="selectedItemsInList" 
                    @update:search-query="(val) => configManager.searchQuery.value = val"
                    @add="() => configManager.addConfiguration(['model'])"
                    @delete="configManager.deleteConfiguration"
                    @rename="handleRename"
                    @toggle-visibility="configManager.toggleVisibility"
                    @move-up="configManager.moveUp"
                    @move-down="configManager.moveDown"
                    @select-configuration="configManager.selectConfiguration"
                    @assign="handleAssign"
                    @remove="handleRemove"
                />
            </div>

            <div class="border-t border-secondary"></div>

            <!-- ═══ SECTION 2 : Composants de la configuration sélectionnée ═══ -->
            <div v-if="activeConfig" class="p-4 bg-bg-light/50 rounded-lg border border-secondary space-y-3">
                <div class="flex items-center justify-between">
                    <h4 class="text-sm font-semibold text-text">
                        Composants pour : <span class="text-accent">{{ activeConfig.name }}</span>
                    </h4>
                </div>
                
                <div class="flex flex-col gap-2">
                    <GenesisCheckboxSimple
                        v-for="comp in AVAILABLE_COMPONENTS"
                        :key="comp.value"
                        :modelValue="activeConfig.components.includes(comp.value)"
                        :label="comp.label"
                        size="md"
                        @update:modelValue="(isChecked) => handleComponentToggle(comp.value, isChecked)"
                    />
                </div>
            </div>
        </section>

    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
// import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { AVAILABLE_COMPONENTS, type ComponentType, type TableMetadataDto } from '@genesis-labs/shared-types';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';

// ═══ 1. GESTION DES CONFIGURATIONS (Via Composable) ═══
const configManager = useConfigurationManager([
    { id: 1, name: 'Configuration par défaut', isHidden: false, components: ['model', 'dao', 'service', 'controller'] },
    { id: 2, name: 'Modèles uniquement', isHidden: false, components: ['model'] },
]);


const tableSearchQuery = ref('');

const activeConfig = computed(() => 
    configManager.configurations.value.find(c => c.id === configManager.selectedConfigId.value)
);

const selectedItemsInList = computed(() => [
    ...tableSelection.value.selectedTables,
    ...tableSelection.value.selectedViews
]);

// Remplace l'ancienne fonction handleRename par celle-ci :
function handleRename(id: string | number, newName: string) {
    configManager.renameConfiguration(id, newName);
}

const filteredCombinedItems = computed(() => {
    if (!tableSearchQuery.value.trim()) return combinedItems.value;
    const query = tableSearchQuery.value.toLowerCase();
    return combinedItems.value.filter(item => 
        item.tableName.toLowerCase().includes(query) // Système "contain"
    );
});

function handleAssign(configId: string | number, items: string[]) {
    configManager.assignToConfig(configId, items);
}

function handleRemove(configId: string | number, items: string[]) {
    configManager.removeFromConfig(configId, items);
}


// MODIFIÉ : Retourne le nom de l'unique configuration, ou "Not assigned"
function getAssignedConfig(item: TableMetadataDto): string {
    const assigned = configManager.configurations.value
        .filter(c => c.components.includes(item.tableName))
        .map(c => c.name);
    
    // Grâce à singleConfiguration=true, ce tableau aura au maximum 1 élément
    return assigned.length > 0 ? assigned[0] : 'Not assigned';
}

function handleComponentToggle(comp: ComponentType, isChecked: boolean) {
    if (!activeConfig.value) return;
    
    const currentComponents = [...activeConfig.value.components];
    const idx = currentComponents.indexOf(comp);
    
    if (isChecked && idx === -1) {
        currentComponents.push(comp);
    } else if (!isChecked && idx !== -1) {
        currentComponents.splice(idx, 1);
    } else {
        return; // Aucun changement nécessaire
    }
    
    // Mise à jour via le composable pour garantir la réactivité
    configManager.editConfiguration(activeConfig.value.id, currentComponents);
}

// ═══ 2. LOGIQUE TABLES/VUES (Store) ═══
const { stepperData, toggleTable, toggleView, tables, views, fetchTablesMetadata } = useGenerator();
const isLoading = ref(false);
const tableSelection = computed(() => stepperData.value.tableSelection);

const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => [
    ...tables.value.map(t => ({ ...t, type: 'table' as const })),
    ...views.value.map(v => ({ ...v, type: 'view' as const }))
]);

const isItemSelected = (item: TableMetadataDto) => {
    return item.isView 
        ? tableSelection.value.selectedViews.includes(item.tableName)
        : tableSelection.value.selectedTables.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto) => {
    item.isView ? toggleView(item.tableName) : toggleTable(item.tableName);
};

const areAllTablesSelected = computed(() => {
    if (tables.value.length === 0) return false;
    return tables.value.every(t => tableSelection.value.selectedTables.includes(t.tableName));
});

const toggleAllTables = () => {
    if (areAllTablesSelected.value) {
        tables.value.forEach(t => {
            const idx = tableSelection.value.selectedTables.indexOf(t.tableName);
            if (idx !== -1) tableSelection.value.selectedTables.splice(idx, 1);
        });
    } else {
        tables.value.forEach(t => {
            if (!tableSelection.value.selectedTables.includes(t.tableName)) {
                tableSelection.value.selectedTables.push(t.tableName);
            }
        });
    }
};

onMounted(async () => {
    isLoading.value = true;
    try {
        await fetchTablesMetadata();
    } catch (error) {
        console.error('[GenerationConfiguration] Erreur chargement tables:', error);
    } finally {
        isLoading.value = false;
    }
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar { width: 6px; }
.custom-scrollbar::-webkit-scrollbar-track { background: transparent; }
.custom-scrollbar::-webkit-scrollbar-thumb { background-color: var(--color-secondary, #cbd5e1); border-radius: 3px; }
</style>