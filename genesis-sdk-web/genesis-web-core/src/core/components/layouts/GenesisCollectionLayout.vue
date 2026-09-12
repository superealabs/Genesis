<template>
    <div class="flex flex-col gap-16 w-full">
        
        <!-- ═══════════════════════════════════════════════════════════ -->
        <!-- PARTIE 1 : EN-TÊTE (Carrousel + Contrôles sur une ligne) -->
        <!-- ═══════════════════════════════════════════════════════════ -->
        
        <div class="flex flex-col w-full">
        <Carrousel
            v-if="showCarousel"
            :slides="carouselSlides"
            slide-height="300px"
            slot-height="100px"
            :auto-play="true"
            class="rounded-t-lg overflow-hidden shadow-sm flex-shrink-0"
        >
                <!-- ✅ Titre ancré dans le carrousel, couleur contrastée automatique -->
                <template #bottom>
                    <div class="flex gap-4">
                        <div class="flex items-center gap-4 px-8 pb-4 pt-2">
                            <GenesisBackButton v-if="showBackButton" @click="$emit('back')" class="shrink-0" />
                            <h2 class="font-semibold font-heading text-4xl text-white drop-shadow-md truncate">
                                <slot name="title">{{ title }}</slot>
                            </h2>
                        </div>

                        <!-- Barre de contrôles -->
                        <div class="relative flex items-center w-full">

                            <!-- ✅ Polygon sur le bloc de droite (inversé) -->
                            <div 
                                class="flex items-center gap-2 flex-1 min-w-0 pl-16 pr-6 py-3 bg-bg-dark"
                                style="clip-path: polygon(3% 0, 100% 0, 100% 101%, 0 101%);"
                            >
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
                                
                            </div>
                            <div class="flex gap-4 items-center flex-shrink-0 pr-6 bg-bg-dark h-full">
                                <slot name="filter">
                                    <GenesisButtonIcon
                                        v-if="showFilter"
                                        variant="secondary"
                                        size="md"
                                        @click="$emit('openFilter')"
                                        class="shrink-0"
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
                                    >
                                        <IconSort />
                                    </GenesisButtonIcon>
                                </slot>

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
                </template>
            </Carrousel>
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