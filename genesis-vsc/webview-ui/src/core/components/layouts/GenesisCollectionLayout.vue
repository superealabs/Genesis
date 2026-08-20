<template>
    <div class="flex flex-col gap-4 p-4">
        <!-- Back button (optionnel) -->
        <GenesisBackButton v-if="showBackButton" @click="$emit('back')" />

        <!-- Header -->
        <div class="flex items-center justify-between">
            <h2 class="font-semibold text-text font-heading text-6xl">
                <slot name="title">{{ title }}</slot>
            </h2>

            <!-- Toggle mode -->
            <LayoutSwitcher 
                v-model="internalDisplayMode" 
                :align="align" 
            />
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
                <span 
                    v-if="showFilter"
                    variant="secondary" 
                    size="sm" 
                    @click="$emit('openFilter')"
                >
                    Filtres
                </span>
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

interface Props {
    title?: string;
    searchValue?: string;
    searchPlaceholder?: string;
    displayMode: 'grid' | 'list';  // ← Requis maintenant
    align?: 'left' | 'right';
    showBackButton?: boolean;
    showFilter?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    title: 'Collection',
    searchPlaceholder: 'Rechercher...',
    displayMode: 'grid',  // ← Valeur par défaut
    align: 'right',
    showBackButton: false,
    showFilter: true
});

const emit = defineEmits<{
    'update:searchValue': [value: string];
    'update:displayMode': [value: 'grid' | 'list'];
    'openFilter': [];
    'back': [];
}>();

// Support v-model pour displayMode (plus simple maintenant)
const internalDisplayMode = computed({
    get: () => props.displayMode,
    set: (value) => emit('update:displayMode', value)
});
</script>