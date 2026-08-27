<template>
    <div class="flex flex-col gap-6 p-4 max-w-4xl mx-auto">
        
        <!-- ═══ Section 1 : Types de composants à générer ═══ -->
        <div class="space-y-3">
            <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                Composants à générer
                <span class="text-accent text-sm font-normal">*</span>
            </h3>
            <p class="text-sm text-text-muted">Sélectionnez les couches de l'architecture que vous souhaitez créer.</p>
            
            <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <button
                    v-for="comp in AVAILABLE_COMPONENTS"
                    :key="comp.value"
                    type="button"
                    class="relative flex items-center justify-center gap-2 px-4 py-3 rounded-md border transition-all duration-200 font-medium"
                    :class="isSelectedComponent(comp.value) 
                        ? 'border-accent bg-accent/10 text-accent' 
                        : 'border-secondary bg-bg-light text-text hover:border-accent/50 hover:bg-secondary'"
                    @click="toggleComponent(comp.value)"
                >
                    <!-- Icône de check conditionnelle -->
                    <svg v-if="isSelectedComponent(comp.value)" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="20 6 9 17 4 12"></polyline>
                    </svg>
                    <span>{{ comp.label }}</span>
                </button>
            </div>
        </div>

        <div class="border-t border-secondary"></div>

        <!-- ═══ Section 2 : Tables et Vues ═══ -->
        <div class="space-y-3">
            <div class="flex items-center justify-between">
                <h3 class="text-lg font-semibold text-text flex items-center gap-2">
                    Tables et Vues
                    <span class="text-accent text-sm font-normal">*</span>
                </h3>
                
                <!-- Bouton "Tout sélectionner" pour les tables -->
                <button 
                    type="button"
                    class="text-xs text-accent hover:text-accent/80 font-medium transition-colors"
                    @click="toggleAllTables"
                >
                    {{ areAllTablesSelected ? 'Tout désélectionner' : 'Tout sélectionner' }}
                </button>
            </div>
            
            <p class="text-sm text-text-muted">Choisissez les entités de votre base de données à inclure dans la génération.</p>

            <!-- Liste des éléments -->
            <div class="border border-secondary rounded-md overflow-hidden bg-bg-light">
                <div 
                    v-for="item in combinedItems" 
                    :key="item.tableName"
                    class="flex items-center justify-between p-3 border-b border-secondary last:border-b-0 hover:bg-secondary/30 transition-colors cursor-pointer"
                    @click="toggleItem(item)"
                >
                    <div class="flex items-center gap-3 flex-1 min-w-0">
                        <!-- Checkbox custom stylisée -->
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
                            <span class="text-xs text-text-muted truncate">Schema nature : {{ item.isView ? 'Vue' : 'Table' }}</span>
                        </div>
                    </div>
                </div>

                <!-- État vide (fallback) -->
                <div v-if="combinedItems.length === 0" class="p-6 text-center text-text-muted text-sm">
                    Aucune table ou vue trouvée pour cette configuration.
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useGenerator } from '../composables/useGenerator';
import { 
    AVAILABLE_COMPONENTS, 
    type ComponentType,
    // type DatabaseConfig,
    type TableMetadataDto 
} from '../types/generator.types';

const { stepperData, toggleTable, toggleView, toggleComponent } = useGenerator();
const MOCK_TABLES: TableMetadataDto[] = [];

// Accès réactif à la configuration de sélection
const tableSelection = computed(() => stepperData.value.tableSelection);

// Combinaison des tables et des vues en une seule liste pour l'affichage
const combinedItems = computed<(TableMetadataDto & { type: 'table' | 'view' })[]>(() => {
    return [
        ...MOCK_TABLES.map(t => ({ ...t, type: 'table' as const })),
        ...MOCK_TABLES.map(v => ({ ...v, type: 'view' as const }))
    ];
});

// ═══ Logique de sélection des composants ═══
const isSelectedComponent = (comp: ComponentType) => {
    return tableSelection.value.selectedComponents.includes(comp);
};

// ═══ Logique de sélection des tables/vues ═══
const isItemSelected = (item: TableMetadataDto) => {
    if (item.isView) {
        return tableSelection.value.selectedViews.includes(item.tableName);
    }
    return tableSelection.value.selectedTables.includes(item.tableName);
};

const toggleItem = (item: TableMetadataDto) => {
    if (item.isView) {
        toggleView(item.tableName);
    } else {
        toggleTable(item.tableName);
    }
};

// ═══ Fonction "Tout sélectionner" pour les tables ═══
const areAllTablesSelected = computed(() => {
    if (MOCK_TABLES.length === 0) return false;
    return MOCK_TABLES.every(t => tableSelection.value.selectedTables.includes(t.tableName));
});

const toggleAllTables = () => {
    if (areAllTablesSelected.value) {
        // Désélectionner tout
        MOCK_TABLES.forEach(t => {
            const idx = tableSelection.value.selectedTables.indexOf(t.tableName);
            if (idx !== -1) tableSelection.value.selectedTables.splice(idx, 1);
        });
    } else {
        // Sélectionner tout
        MOCK_TABLES.forEach(t => {
            if (!tableSelection.value.selectedTables.includes(t.tableName)) {
                tableSelection.value.selectedTables.push(t.tableName);
            }
        });
    }
};
</script>