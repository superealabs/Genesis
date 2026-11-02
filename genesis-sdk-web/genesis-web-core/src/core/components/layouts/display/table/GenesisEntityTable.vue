<template>
    <GenesisContainer
        :is-loading="isLoading"
        :loading-text="loadingText"
        :is-empty="items.length === 0"
        :empty-text="emptyText"
        :min-height="minHeight"
        :max-height="maxHeight"
        :height="height"
    >
        <div
            v-for="item in items"
            :key="item.tableName"
            class="flex items-center justify-between p-3 border-b border-bg-neutral-genesis last:border-b-0 transition-colors"
            :class="isEditable
                ? 'cursor-pointer hover:bg-bg-secondary hover:text-secondary'
                : 'cursor-default hover:bg-bg-secondary/50'"
            @click="(e) => handleRowClick(item, e)"
        >
            <div class="flex items-center gap-3 flex-1 min-w-0">
                <!-- Case à cocher : uniquement en mode éditable -->
                <GenesisCheckboxSimple
                    v-if="isEditable"
                    :model-value="selectedItems.includes(item.tableName)"
                />

                <!-- Icône à gauche : table ou vue -->
                <component
                    :is="item.isView ? IconShow : IconData"
                    :size="18"
                    class="flex-shrink-0 text-text-muted"
                    :class="item.isView ? 'text-secondary' : 'text-accent'"
                />

                <div class="flex flex-col min-w-0 select-none">
                    <span class="text-sm font-medium truncate">{{ item.tableName }}</span>
                    <span class="text-xs text-text-muted truncate">{{ item.isView ? 'Vue' : 'Table' }}</span>
                </div>
            </div>

            <!-- Texte de droite (texte simple, sans badge) -->
            <div
                v-if="rightText"
                class="flex-shrink-0 ml-4 text-right min-w-[120px] truncate text-xs font-medium text-primary"
                :title="rightText(item)"
            >
                {{ rightText(item) }}
            </div>
        </div>
    </GenesisContainer>
</template>

<script setup lang="ts">
import { computed, toRef } from 'vue';
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import IconData from '@genesis-labs/web-core/core/components/ui/icons/IconData.vue';
import IconShow from '@genesis-labs/web-core/core/components/ui/icons/IconShow.vue';

import { useMultiSelect, type SelectMode } from '@genesis-labs/web-core/core/composables/ux/useMultiSelect';

export interface EntityTableItem {
    tableName: string;
    isView: boolean;
}

const props = withDefaults(defineProps<{
    items: EntityTableItem[];
    isEditable?: boolean;
    selectedItems?: string[];
    rightText?: (item: EntityTableItem) => string;
    selectMode?: SelectMode;   // 👈 nouveau
    isLoading?: boolean;
    loadingText?: string;
    emptyText?: string;
    minHeight?: number;
    maxHeight?: number;
    height?: number;
}>(), {
    isEditable: false,
    selectedItems: () => [],
    selectMode: 'Select',      // 👈 défaut
    isLoading: false,
    emptyText: 'Aucune entité.'
});

const emit = defineEmits<{
    toggle: [item: EntityTableItem];
    'update:selectedItems': [value: string[]];   // 👈 nouveau
}>();

// Ref interne synchronisée avec la prop (v-model)
const selectedIdsRef = computed({
    get: () => props.selectedItems,
    set: (v) => emit('update:selectedItems', [...v]),
});

const { handleClick } = useMultiSelect<EntityTableItem>({
    items: toRef(props, 'items'),
    getId: (item) => item.tableName,
    selectedIds: selectedIdsRef,
    selectMode: toRef(props, 'selectMode'),
});

function handleRowClick(item: EntityTableItem, event: MouseEvent) {
    if (!props.isEditable) return;
    emit('toggle', item);   // compat : conserve l'émission de toggle
    handleClick(item, event);
}
</script>