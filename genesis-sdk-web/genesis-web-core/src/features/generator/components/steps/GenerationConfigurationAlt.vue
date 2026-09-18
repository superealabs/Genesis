<template>
    <div class="flex gap-6 p-4 max-w-5xl mx-auto h-full">
        
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
                    @edit="handleEdit"
                    @toggle-visibility="configManager.toggleVisibility"
                    @move-up="configManager.moveUp"
                    @move-down="configManager.moveDown"
                    @select-configuration="configManager.selectConfiguration"
                    @assign="handleAssign"
                    @remove="handleRemove"
                />
            </div>

            <div class="border-t border-secondary"></div>

            <!-- ═══ SECTION 2 : Édition de la configuration sélectionnée ═══ -->
            <div v-if="activeConfig && !isEditing" class="p-4 bg-bg-light/50 rounded-lg border border-secondary">
                <div class="flex items-center justify-between">
                    <div>
                        <h4 class="text-sm font-semibold text-text">
                            Configuration active : <span class="text-accent">{{ activeConfig.name }}</span>
                        </h4>
                        <p class="text-xs text-text-muted mt-1">
                            Composants : {{ activeConfig.components.map(getComponentLabel).join(', ') || 'Aucun' }}
                        </p>
                    </div>
                    <GenesisButton variant="secondary" size="sm" @click="handleEdit(activeConfig.id)">
                        Modifier les composants
                    </GenesisButton>
                </div>
            </div>

            <div v-if="isEditing && activeConfig" class="p-4 bg-accent/5 rounded-lg border border-accent/30 space-y-3">
                <div class="flex items-center justify-between">
                    <h4 class="text-sm font-semibold text-accent">Modification : {{ activeConfig.name }}</h4>
                    <div class="flex gap-2">
                        <GenesisButton variant="tertiary" size="sm" @click="isEditing = false">Annuler</GenesisButton>
                        <GenesisButton variant="primary" size="sm" @click="saveComponents">Enregistrer</GenesisButton>
                    </div>
                </div>
                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                    <button
                        v-for="comp in AVAILABLE_COMPONENTS"
                        :key="comp.value"
                        type="button"
                        class="relative flex items-center justify-center gap-2 px-4 py-3 rounded-md border transition-all duration-200 font-medium"
                        :class="editingComponents.includes(comp.value) 
                            ? 'border-accent bg-accent/10 text-accent' 
                            : 'border-secondary bg-bg-light text-text hover:border-accent/50 hover:bg-secondary'"
                        @click="toggleEditingComponent(comp.value)"
                    >
                        <svg v-if="editingComponents.includes(comp.value)" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                            <polyline points="20 6 9 17 4 12"></polyline>
                        </svg>
                        <span>{{ comp.label }}</span>
                    </button>
                </div>
            </div>
        </section>

        <div class="border-t border-secondary"></div>

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

            <div class="border border-secondary rounded-md overflow-hidden bg-bg-light flex-1 min-h-0 flex flex-col">
                <div v-if="isLoading" class="p-6 text-center text-text-muted text-sm flex items-center justify-center gap-2">
                    <span class="animate-spin">⟳</span> Chargement des métadonnées...
                </div>

                <template v-else>
                    <div class="flex-1 overflow-y-auto custom-scrollbar">
                        <div 
                            v-for="item in combinedItems" 
                            :key="item.tableName"
                            class="flex items-center justify-between p-3 border-b border-secondary last:border-b-0 hover:bg-secondary/30 transition-colors cursor-pointer"
                            @click="toggleItem(item)"
                        >
                            <div class="flex items-center gap-3 flex-1 min-w-0">
                                <div 
                                    class="w-5 h-5 rounded border flex items-center justify-center flex-shrink-0 transition-colors"
                                    :class="isItemSelected(item) ? 'bg-accent border-accent' : 'border-secondary bg-bg'"
                                >
                                    <svg v-if="isItemSelected(item)" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                                        <polyline points="20 6 9 17 4 12"></polyline>
                                    </svg>
                                </div>
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
                    </div>
                    <div v-if="combinedItems.length === 0" class="p-6 text-center text-text-muted text-sm flex-shrink-0">
                        Aucune table ou vue trouvée.
                    </div>
                </template>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useGenerator } from '@genesis-labs/web-core/features/generator/composables/useGenerator';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisConfigurationSelector from '@genesis-labs/web-core/core/components/layouts/display/configuration/GenesisConfigurationSelector.vue';
import { useConfigurationManager } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';
import { AVAILABLE_COMPONENTS, type ComponentType, type TableMetadataDto } from '@genesis-labs/shared-types';

// ═══ 1. GESTION DES CONFIGURATIONS (Via Composable) ═══
const configManager = useConfigurationManager([
    { id: 1, name: 'Configuration par défaut', isHidden: false, components: ['model', 'dao', 'service', 'controller'] },
    { id: 2, name: 'Modèles uniquement', isHidden: false, components: ['model'] },
]);

const isEditing = ref(false);
const editingComponents = ref<ComponentType[]>([]);

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

function handleEdit(id: string | number) {
    configManager.selectConfiguration(id);
    const config = configManager.configurations.value.find(c => c.id === id);
    if (config) {
        editingComponents.value = [...(config.components as ComponentType[])];
        isEditing.value = true;
    }
}

function handleAssign(configId: string | number, items: string[]) {
    configManager.assignToConfig(configId, items);
}

function handleRemove(configId: string | number, items: string[]) {
    configManager.removeFromConfig(configId, items);
}

function toggleEditingComponent(comp: ComponentType) {
    const idx = editingComponents.value.indexOf(comp);
    idx === -1 ? editingComponents.value.push(comp) : editingComponents.value.splice(idx, 1);
}

function saveComponents() {
    if (configManager.selectedConfigId.value) {
        configManager.editConfiguration(configManager.selectedConfigId.value, editingComponents.value);
    }
    isEditing.value = false;
}

function getComponentLabel(value: ComponentType | string) {
    return AVAILABLE_COMPONENTS.find(c => c.value === value)?.label || value;
}

// MODIFIÉ : Retourne le nom de l'unique configuration, ou "Not assigned"
function getAssignedConfig(item: TableMetadataDto): string {
    const assigned = configManager.configurations.value
        .filter(c => c.components.includes(item.tableName))
        .map(c => c.name);
    
    // Grâce à singleConfiguration=true, ce tableau aura au maximum 1 élément
    return assigned.length > 0 ? assigned[0] : 'Not assigned';
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