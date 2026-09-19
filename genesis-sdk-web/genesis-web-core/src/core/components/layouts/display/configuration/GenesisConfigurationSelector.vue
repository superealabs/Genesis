<template>
    <div class="flex flex-col gap-4 w-full">
        
        <!-- ═══ PARTIE 1 : Le Panneau de Configuration (Générique) ═══ -->
        <GenesisConfigurationPanel
            :configurations="configurations"
            :selected-config-id="selectedConfigId"
            :filtered-configs="filteredConfigs"
            :search-query="searchQuery"
            :can-move-up="canMoveUp"
            :can-move-down="canMoveDown"
            @update:search-query="(val) => $emit('update:searchQuery', val)"
            @add="$emit('add')"
            @delete="(id) => $emit('delete', id)"
            @rename="(id, newName) => $emit('rename', id, newName)"
            @edit="(id) => $emit('edit', id)"
            @toggle-visibility="(id) => $emit('toggleVisibility', id)"
            @move-up="(id) => $emit('moveUp', id)"
            @move-down="(id) => $emit('moveDown', id)"
            @select-configuration="(id) => $emit('selectConfiguration', id)"
        />

        <!-- ═══ PARTIE 2 : Ligne d'actions Assign / Remove ═══ -->
        <div class="flex items-center justify-start gap-3 pt-3 border-t border-secondary">
            <GenesisButton
                variant="secondary"
                size="sm"
                :disabled="!selectedConfigId || selectedItems.length === 0"
                @click="handleAssign"
                title="Assigner les éléments sélectionnés à cette configuration"
            >
                Assign
            </GenesisButton>

            <GenesisButton
                variant="secondary"
                size="sm"
                :disabled="!selectedConfigId || selectedItems.length === 0"
                @click="handleRemove"
                title="Retirer les éléments sélectionnés de cette configuration"
            >
                Remove
            </GenesisButton>
        </div>

    </div>
</template>

<script setup lang="ts">
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import GenesisConfigurationPanel from './GenesisConfigurationPanel.vue';
import type { ConfigurationItem } from '@genesis-labs/web-core/core/composables/ux/useConfigurationManager';

const props = defineProps<{
    configurations: ConfigurationItem[];
    selectedConfigId: string | number | null;
    filteredConfigs: ConfigurationItem[];
    searchQuery: string;
    canMoveUp: boolean;
    canMoveDown: boolean;
    // NOUVEAU : La liste des éléments actuellement sélectionnés dans le parent (ex: noms de tables/vues)
    selectedItems: string[]; 
}>();

const emit = defineEmits<{
    'update:searchQuery': [value: string];
    add: [];
    delete: [id: string | number];
    rename: [id: string | number, newName: string];
    edit: [id: string | number];
    toggleVisibility: [id: string | number];
    moveUp: [id: string | number];
    moveDown: [id: string | number];
    selectConfiguration: [id: string | number];
    assign: [configId: string | number, items: string[]];
    remove: [configId: string | number, items: string[]];
}>();

// ═══ HANDLERS D'ASSOCIATION ═══

function handleAssign() {
    if (props.selectedConfigId !== null && props.selectedItems.length > 0) {
        emit('assign', props.selectedConfigId, props.selectedItems);
    }
}

function handleRemove() {
    if (props.selectedConfigId !== null && props.selectedItems.length > 0) {
        emit('remove', props.selectedConfigId, props.selectedItems);
    }
}
</script>