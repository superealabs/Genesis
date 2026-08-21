<template>
    <div :class="containerClasses" :style="gridStyle">
        <slot />
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(defineProps<{
    /**
     * Mode d'affichage : grid (cartes) ou list (lignes)
     * @default 'grid'
     */
    display?: 'grid' | 'list';
    /**
     * Largeur minimale des colonnes en mode grid
     * @default '120px'
     */
    minColWidth?: string;
}>(), {
    display: 'grid',
    minColWidth: '120px'
});

// Classes du container selon le mode
const containerClasses = computed(() => {
    if (props.display === 'grid') {
        return 'grid gap-3';
    }
    return 'flex flex-col gap-2';
});

// Style inline pour le grid (uniquement en mode grid)
const gridStyle = computed(() => {
    if (props.display === 'grid') {
        return `grid-template-columns: repeat(auto-fill, minmax(${props.minColWidth}, 1fr))`;
    }
    return undefined;
});
</script>