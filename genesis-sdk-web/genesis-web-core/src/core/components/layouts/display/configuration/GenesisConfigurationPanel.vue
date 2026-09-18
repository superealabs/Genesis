<template>
    <div class="flex flex-col w-full min-w-[300px] max-w-[400px] gap-4 relative" :style="resizeStyle">

        
        <!-- ═══ EN-TÊTE : Barre de recherche ═══ -->
        <div class="relative">
            <GenesisInput
                :modelValue="searchQuery"
                @update:modelValue="handleSearchUpdate"
                type="text"
                placeholder="Configuration name....."
                size="md"
                fill-width
            >
                <template #left>
                    <IconSearch :size="18" class="text-text-muted" />
                </template>
            </GenesisInput>
        </div>

        <!-- ═══ CONTENU : Deux colonnes ═══ -->
        <div class="flex flex-1 min-h-0 gap-4 overflow-hidden">
            
            <div class="flex-1 flex flex-col min-h-0 overflow-hidden border border-secondary rounded-lg bg-bg-dark/30 relative">
                <div class="flex-1 overflow-y-auto p-3 space-y-2 custom-scrollbar">
                    
                    <!-- Dans le template, mettre à jour l'écouteur @rename -->
                    <GenesisItemConfig
                        v-for="config in filteredConfigs"
                        :key="config.id"
                        :name="config.name"
                        :is-hidden="config.isHidden"
                        :is-selected="selectedConfigId === config.id"
                        :show-delete="false" 
                        @select="handleSelect(config.id)"
                        @rename="(newName) => $emit('rename', config.id, newName)" 
                        @edit="$emit('edit', config.id)"
                        @toggleVisibility="handleToggleVisibility(config.id)"
                    />

                    <div v-if="filteredConfigs.length === 0" class="flex flex-col items-center justify-center h-32 text-text-muted text-sm">
                        <IconSearch :size="24" class="mb-2 opacity-50" />
                        <span>Aucune configuration trouvée.</span>
                    </div>
                </div>

                <!-- Handle déplacé ICI, à l'intérieur de la colonne gauche -->
                <div 
                    class="absolute bottom-0 left-0 right-0 py-2 h-3 cursor-ns-resize flex items-center justify-center hover:bg-accent/20 transition-colors z-20 rounded-b-lg"
                    @mousedown="startResizeBottom"
                    title="Redimensionner verticalement"
                >
                    <IconDragY class="text-text-muted opacity-50 hover:opacity-100" :size="20" />
                </div>
            </div>

            <!-- COLONNE DROITE : Boutons de configuration (Exclusivement GenesisButtonIcon) -->
            <div class="flex flex-col gap-2 flex-shrink-0 justify-between pt-1">
                
                <!-- Groupe 1 : Gestion (Ajout / Suppression) -->
                <div class="flex flex-col gap-2">
                    <GenesisButtonIcon 
                        variant="secondary" 
                        size="lg" 
                        title="Ajouter une configuration"
                        @click="handleAdd"
                    >
                        <IconPlus />
                    </GenesisButtonIcon>

                    <GenesisButtonIcon 
                        variant="secondary" 
                        size="lg" 
                        title="Supprimer la configuration sélectionnée"
                        :disabled="!selectedConfigId"
                        class="disabled:opacity-50 disabled:cursor-not-allowed"
                        @click="handleDelete"
                    >
                        <IconTrashAlt />
                    </GenesisButtonIcon>
                </div>

                <!-- Séparateur -->


                <div class="flex flex-col gap-2">
                    <!-- Groupe 2 : Navigation (Réordonnancement) -->
                    <GenesisButtonIcon 
                        variant="secondary" 
                        size="lg" 
                        title="Monter la configuration"
                        :disabled="!canMoveUp"
                        class="disabled:opacity-50 disabled:cursor-not-allowed"
                        @click="handleMoveUp"
                    >
                        <IconChevronUp />
                    </GenesisButtonIcon>

                    <GenesisButtonIcon 
                        variant="secondary" 
                        size="lg" 
                        title="Descendre la configuration"
                        :disabled="!canMoveDown"
                        class="disabled:opacity-50 disabled:cursor-not-allowed"
                        @click="handleMoveDown"
                    >
                        <IconChevronDown />
                    </GenesisButtonIcon>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import IconPlus from '@genesis-labs/web-core/core/components/ui/icons/IconPlus.vue';
import IconTrashAlt from '@genesis-labs/web-core/core/components/ui/icons/IconTrashAlt.vue';
import IconChevronUp from '@genesis-labs/web-core/core/components/ui/icons/IconChevronUp.vue';
import IconChevronDown from '@genesis-labs/web-core/core/components/ui/icons/IconChevronDown.vue';
import GenesisItemConfig from './GenesisItemConfig.vue';
import type { ConfigurationItem } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';

import { useResizable } from '@genesis-labs/web-core/core/composables/ux/useResizable';
import IconDragY from '@genesis-labs/web-core/core/components/ui/icons/IconDragY.vue';


const props = defineProps<{
    configurations: ConfigurationItem[];
    selectedConfigId: string | number | null;
    filteredConfigs: ConfigurationItem[];
    searchQuery: string;
    canMoveUp: boolean;
    canMoveDown: boolean;
}>();

// Dans le script setup, mettre à jour defineEmits
const emit = defineEmits<{
    'update:searchQuery': [value: string];
    add: [];
    delete: [id: string | number];
    rename: [id: string | number, newName: string]; // ✅ MODIFIÉ
    edit: [id: string | number];
    toggleVisibility: [id: string | number];
    moveUp: [id: string | number];
    moveDown: [id: string | number];
    selectConfiguration: [id: string | number];
}>();

// ═══ HANDLERS POUR RELIER LE TEMPLATE AUX EMITS ═══

function handleSelect(id: string | number) {
    emit('selectConfiguration', id);
}

function handleToggleVisibility(id: string | number) {
    emit('toggleVisibility', id);
}

function handleAdd() {
    emit('add');
}

function handleDelete() {
    if (props.selectedConfigId !== null) {
        emit('delete', props.selectedConfigId);
    }
}

function handleMoveUp() {
    if (props.selectedConfigId !== null) {
        emit('moveUp', props.selectedConfigId);
    }
}

function handleMoveDown() {
    if (props.selectedConfigId !== null) {
        emit('moveDown', props.selectedConfigId);
    }
}

function handleSearchUpdate(value: string | number | boolean) {
    emit('update:searchQuery', String(value));
}

const { resizeStyle, startResizeBottom } = useResizable({
    minHeight: 250, // Hauteur minimale pour garder le panneau utilisable
    maxHeight: () => window.innerHeight * 0.85, // Hauteur max : 85% de la fenêtre
    resizableX: ref(false), // Désactive le redimensionnement horizontal
    resizableY: ref(true)   // Active le redimensionnement vertical
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
    background-color: var(--color-secondary, #cbd5e1);
    border-radius: 3px;
}
</style>