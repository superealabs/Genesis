<template>
    <div class="flex flex-col gap-6 p-4 w-full">
        
        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- PARTIE 1 : EN-TÊTE (Carrousel + Contrôles sur une ligne) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
        
        <!-- 1.1 Carrousel (Bannière visuelle en haut) -->
        <Carrousel 
            v-if="showCarousel" 
            :slides="carouselSlides" 
            height="200px" 
            :auto-play="true"
            class="rounded-lg overflow-hidden shadow-sm"
        />

        <!-- 1.2 Barre de contrôles (Single Line Layout) -->
        <div class="flex items-center gap-4 w-full">

            <!-- GROUPE 1 : Back + Title (≈ 35-40%) -->
            <div class="flex items-center gap-3 flex-[1.2] min-w-0">
                <GenesisBackButton v-if="showBackButton" @click="$emit('back')" class="shrink-0" />
                
                <!-- Note : text-2xl/md:text-3xl remplace text-6xl pour tenir sur une seule ligne -->
                <h2 class="font-semibold text-text font-heading text-2xl md:text-3xl truncate shrink-0" :title="title">
                    <slot name="title">{{ title }}</slot>
                </h2>
            </div>

            <!-- GROUPE 2 : Recherche + Filtre + Tri (≈ 30-35%) -->
            <div class="flex items-center gap-2 flex-1 min-w-0">
                <GenesisInput
                    :modelValue="searchValue"
                    @update:modelValue="$emit('update:searchValue', $event as string)"
                    type="text"
                    :placeholder="searchPlaceholder"
                    variant="primary"
                    shape="rectangle"
                    size="md" 
                    fill-width
                    class="min-w-0"
                >
                    <template #left>
                        <IconSearch :size="18" class="text-text-muted" />
                    </template>
                </GenesisInput>
                
                <!-- Slot pour filtre custom -->
                <slot name="filter">
                    <GenesisButtonIcon
                        v-if="showFilter"
                        variant="secondary"
                        size="md"
                        @click="$emit('openFilter')"
                        class="shrink-0"
                        title="Filtrer"
                    >
                        <IconFilter />
                    </GenesisButtonIcon>
                </slot>

                <!-- Slot pour tri custom -->
                <slot name="sort">
                    <GenesisButtonIcon
                        v-if="showSort"
                        variant="secondary"
                        size="md"
                        @click="$emit('openSort')"
                        class="shrink-0"
                        title="Trier"
                    >
                        <IconSort />
                    </GenesisButtonIcon>
                </slot>
            </div>

            <!-- GROUPE 3 : Segmented Control + Layout Switcher (≈ 30-35%) -->
            <div class="flex items-center justify-end gap-2 flex-1 shrink-0">
                <GenesisSegmentedControl
                    v-model="internalMode"
                    :options="[
                        { label: 'Selection', value: 'selection', icon: IconCursor },
                        { label: 'Compare', value: 'compare', icon: IconGitCompare }
                    ]"
                    size="sm" 
                />
                
                <LayoutSwitcherAlt v-model="internalDisplayMode" />
            </div>

        </div>

        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- PARTIE 2 : CONTENU (Grid ou List) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
        <div class="flex-1 min-h-0">
            <slot />
        </div>

    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import GenesisBackButton from '@/core/components/ui/actions/GenesisBackButton.vue';
import GenesisSegmentedControl from '@/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconCursor from '@/core/components/ui/icons/IconCursor.vue';
import IconGitCompare from '@/core/components/ui/icons/IconGitCompare.vue';
import IconFilter from '@/core/components/ui/icons/IconFilter.vue';
import IconSort from '@/core/components/ui/icons/IconSort.vue'; // ✅ Ajouté
import IconSearch from '@/core/components/ui/icons/IconSearch.vue';
import LayoutSwitcherAlt from '../ui/dropdown/LayoutSwitcherAlt.vue';
import GenesisInput from '@/core/components/ui/inputs/GenesisInput.vue';

// ✅ Import du Carrousel
import Carrousel, { type CarouselSlide } from '@/core/components/ui/carrousel/Carrousel.vue';

export type CollectionMode = 'selection' | 'compare';

interface Props {
    title?: string;
    searchValue?: string;
    searchPlaceholder?: string;
    displayMode: 'grid' | 'list';
    mode?: CollectionMode;
    align?: 'left' | 'right';
    showBackButton?: boolean;
    showFilter?: boolean;
    showSort?: boolean;       // ✅ Ajouté
    showCarousel?: boolean;   // ✅ Ajouté
}

const props = withDefaults(defineProps<Props>(), {
    title: 'Collection',
    searchPlaceholder: 'Rechercher...',
    displayMode: 'grid',
    mode: 'selection',
    align: 'right',
    showBackButton: false,
    showFilter: true,
    showSort: false,
    showCarousel: true
});

const emit = defineEmits<{
    'update:searchValue': [value: string];
    'update:displayMode': [value: 'grid' | 'list'];
    'update:mode': [value: CollectionMode];
    'openFilter': [];
    'openSort': [];           // ✅ Ajouté
    'back': [];
}>();

// ✅ Données de démo pour le carrousel
const carouselSlides = ref<CarouselSlide[]>([
    { color: '#3B82F6', label: 'Slide 1 - Bleu' },
    { color: '#EF4444', label: 'Slide 2 - Rouge' },
    { color: '#10B981', label: 'Slide 3 - Vert' },
    { color: '#F59E0B', label: 'Slide 4 - Orange' },
    { color: '#8B5CF6', label: 'Slide 5 - Violet' },
]);

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