<template>
    <div class="flex flex-col gap-6 w-full">
        
        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- PARTIE 1 : EN-TÊTE (Carrousel + Contrôles sur une ligne) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
        
        <!-- 1.1 Carrousel (Bannière visuelle en haut) -->
        <div class="flex flex-col w-full">
            <Carrousel 
                v-if="showCarousel" 
                :slides="carouselSlides" 
                height="300px" 
                :auto-play="true"
                class="rounded-t-lg rounded-br-lg overflow-hidden shadow-sm"
            />

            <!-- 1.2 Barre de contrôles (Single Line Layout) -->
            <div class="relative flex items-center gap-4 w-full bg-red-300">
                
                <!-- ═══ GROUPE 1 : Back + Title ═══ -->
                <!-- 1. self-stretch force ce bloc à prendre toute la hauteur du parent (outrepasse items-center) -->
                <div class="relative flex items-center flex-[1.2] min-w-0 self-stretch pr-4 pt-3 pb-2">
                    
                    <!-- 2. LE FOND TRAPÈZE : 
                        - inset-0 le force à prendre 100% de la largeur et de la hauteur de son parent (le self-stretch)
                        - clip-path est appliqué ICI, pas sur le conteneur de texte -->
                    <div 
                        class="absolute inset-0 bg-amber-700 z-0 transition-colors duration-500 rounded-b-lg"
                        style="clip-path: polygon(0 0, 100% 0, 85% 100%, 0 100%);"
                    ></div>

                    <!-- 3. LE CONTENU : 
                        - relative et z-10 pour flotter au-dessus du fond trapèze -->
                    <div class="relative z-10 flex items-center gap-3 min-w-0 w-full pl-8">
                        <GenesisBackButton v-if="showBackButton" @click="$emit('back')" class="shrink-0" />
                        
                        <h2 class="font-semibold text-text font-heading text-3xl md:text-3xl truncate shrink-0" :title="title">
                            <slot name="title">{{ title }}</slot>
                        </h2>
                    </div>
                </div>

                <!-- ═══ GROUPE 2 : Recherche + Filtre + Tri ═══ -->
                <div class="relative z-10 flex items-center gap-2 flex-1 min-w-0">
                    <GenesisInput
                        :modelValue="searchValue"
                        @update:modelValue="$emit('update:searchValue', $event as string)"
                        type="text"
                        :placeholder="searchPlaceholder"
                        variant="primary"
                        shape="rectangle"
                        size="lg" 
                        fill-width
                        class="min-w-0"
                    >
                        <template #left>
                            <IconSearch :size="18" class="text-text-muted" />
                        </template>
                    </GenesisInput>
                    
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

                <!-- ═══ GROUPE 3 : Segmented Control + Layout Switcher ═══ -->
                <div class="relative z-10 flex items-center justify-end gap-2 flex-1 shrink-0 pr-8">
                    <GenesisSegmentedControl
                        v-model="internalMode"
                        :options="[
                            { label: 'Selection', value: 'selection', icon: IconCursor },
                            { label: 'Compare', value: 'compare', icon: IconGitCompare }
                        ]"
                        size="md" 
                    />
                    
                    <LayoutSwitcherAlt v-model="internalDisplayMode" />
                </div>

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