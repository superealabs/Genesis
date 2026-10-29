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
            @click="handleClick(item)"
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

                <div class="flex flex-col min-w-0">
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
import GenesisContainer from '@genesis-labs/web-core/core/components/layouts/GenesisContainer.vue';
import GenesisCheckboxSimple from '@genesis-labs/web-core/core/components/ui/inputs/GenesisCheckboxSimple.vue';
import IconData from '@genesis-labs/web-core/core/components/ui/icons/IconData.vue';
import IconShow from '@genesis-labs/web-core/core/components/ui/icons/IconShow.vue';

export interface EntityTableItem {
    tableName: string;
    isView: boolean;
}

const props = withDefaults(defineProps<{
    items: EntityTableItem[];
    /** true : lignes cliquables avec case à cocher. false : lecture seule. */
    isEditable?: boolean;
    /** Noms des entités sélectionnées (utilisé seulement si isEditable) */
    selectedItems?: string[];
    /** Texte affiché à droite de chaque ligne (ex. nom du profil). Pas de colonne si absent. */
    rightText?: (item: EntityTableItem) => string;
    isLoading?: boolean;
    loadingText?: string;
    emptyText?: string;
    minHeight?: number;
    maxHeight?: number;
    height?: number;
}>(), {
    isEditable: false,
    selectedItems: () => [],
    isLoading: false,
    emptyText: 'Aucune entité.'
});

const emit = defineEmits<{
    toggle: [item: EntityTableItem];
}>();

function handleClick(item: EntityTableItem) {
    if (props.isEditable) emit('toggle', item);
}
</script>