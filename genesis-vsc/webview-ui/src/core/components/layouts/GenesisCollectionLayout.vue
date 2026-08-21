<template>
    <div class="flex flex-col gap-4 p-4">
        <!-- Back button (optionnel) -->
        <GenesisBackButton v-if="showBackButton" @click="$emit('back')" />

        <!-- Header -->
        <div class="flex items-center justify-between gap-4">
            <h2 class="font-semibold text-text font-heading text-6xl shrink-0">
                <slot name="title">{{ title }}</slot>
            </h2>

            <!-- Contrôles à droite : Mode + Layout -->
            <div class="flex items-center gap-2">
                <GenesisSegmentedControl
                    v-model="internalMode"
                    :options="[
                        { label: 'Selection', value: 'selection', icon: IconCursor },
                        { label: 'Compare', value: 'compare', icon: IconGitCompare }
                    ]"
                    size="sm"
                />
                
                <LayoutSwitcher 
                    v-model="internalDisplayMode" 
                    :align="align" 
                />
            </div>
        </div>

        <!-- Barre de recherche + filtre -->
        <div class="flex items-center gap-2">
            <input
                :value="searchValue"
                @input="$emit('update:searchValue', ($event.target as HTMLInputElement).value)"
                type="text"
                :placeholder="searchPlaceholder"
                class="flex-1 bg-bg-light text-text border border-secondary rounded px-3 py-1.5 text-xs
                       focus:outline-none focus:border-accent focus:ring-1 focus:ring-accent
                       placeholder:text-text-muted"
            />
            
            <!-- Slot pour filtre custom -->
            <slot name="filter">
                <GenesisButtonIcon
                    v-if="showFilter"
                    variant="secondary"
                    size="md"
                    @click="$emit('openFilter')"
                >
                    <IconFilter />
                </GenesisButtonIcon>
            </slot>
        </div>

        <!-- Contenu (Grid ou List) -->
        <slot />
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GenesisBackButton from '@/core/components/ui/actions/GenesisBackButton.vue';
import LayoutSwitcher from '@/core/components/ui/dropdown/LayoutSwitcher.vue';
import GenesisSegmentedControl from '@/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconCursor from '@/core/components/ui/icons/IconCursor.vue';
import IconGitCompare from '@/core/components/ui/icons/IconGitCompare.vue';
import IconFilter from '@/core/components/ui/icons/IconFilter.vue';

export type CollectionMode = 'selection' | 'compare';

interface Props {
    title?: string;
    searchValue?: string;
    searchPlaceholder?: string;
    displayMode: 'grid' | 'list';
    mode?: CollectionMode;              // ← AJOUTÉ
    align?: 'left' | 'right';
    showBackButton?: boolean;
    showFilter?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    title: 'Collection',
    searchPlaceholder: 'Rechercher...',
    displayMode: 'grid',
    mode: 'selection',                  // ← AJOUTÉ
    align: 'right',
    showBackButton: false,
    showFilter: true
});

const emit = defineEmits<{
    'update:searchValue': [value: string];
    'update:displayMode': [value: 'grid' | 'list'];
    'update:mode': [value: CollectionMode];  // ← AJOUTÉ
    'openFilter': [];
    'back': [];
}>();

// Support v-model pour displayMode
const internalDisplayMode = computed({
    get: () => props.displayMode,
    set: (value) => emit('update:displayMode', value)
});

// Support v-model pour mode
const internalMode = computed({
    get: () => props.mode,
    set: (value) => emit('update:mode', value as CollectionMode)
});
</script>