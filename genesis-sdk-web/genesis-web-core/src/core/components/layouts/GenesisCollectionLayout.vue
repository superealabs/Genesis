<template>
    <div class="flex flex-col gap-16 w-full">
        <div class="flex flex-col w-full">
            <Carrousel
                v-if="showCarousel"
                :slides="carouselSlides"
                slide-height="300px"
                slot-height="100px"
                :auto-play="true"
                class="rounded-t-lg overflow-hidden shadow-sm flex-shrink-0"
            >
                <template #bottom>
                    <div class="flex gap-4">
                        <div class="flex items-center gap-4 px-8 pb-4 pt-2">
                            <GenesisBackButton v-if="showBackButton" @click="$emit('back')" class="shrink-0" />
                            <h2 class="font-semibold font-heading text-4xl text-white drop-shadow-md truncate">
                                <slot name="title">{{ title }}</slot>
                            </h2>
                        </div>

                        <div class="relative flex items-center w-full">
                            <div 
                                class="flex items-center gap-2 flex-1 min-w-0 pl-24 pr-6 py-4 bg-bg-dark"
                                style="clip-path: polygon(10% 0, 101% 0, 100% 101%, 0 101%);"
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

                            <div class="flex gap-2 items-center flex-shrink-0 pr-6 bg-bg-dark h-full">
                                <!-- ═══ BOUTON FILTRE (Émet un événement) ═══ -->
                                <GenesisButtonIcon
                                    v-if="showFilter"
                                    variant="secondary"
                                    size="xl"
                                    :hide-chevron="true"
                                    @click="$emit('openFilter')"
                                    class="shrink-0"
                                    title="Ouvrir les filtres"
                                >
                                    <IconFilter />
                                </GenesisButtonIcon>

                                <!-- ═══ DROPDOWN TRI ═══ -->
                                <GenesisDropdown
                                    v-if="showSort"
                                    dropdownSize="lg"
                                    :closeOnSelect="false"
                                    triggerVariant="secondary"
                                    trigger-size="xl"
                                    :hide-chevron="true"
                                >
                                    <template #triggerIcon>
                                        <IconSort />
                                    </template>
                                    <template #default>
                                        <slot name="sort-content"></slot>
                                    </template>
                                </GenesisDropdown>

                                <!-- ═══ CONTRÔLES D'AFFICHAGE ═══ -->
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

        <div class="flex-1 min-h-0">
            <slot />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import GenesisBackButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisBackButton.vue';
import GenesisSegmentedControl from '@genesis-labs/web-core/core/components/ui/actions/GenesisSegmentedControl.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import IconCursor from '@genesis-labs/web-core/core/components/ui/icons/IconCursor.vue';
import IconGitCompare from '@genesis-labs/web-core/core/components/ui/icons/IconGitCompare.vue';
import IconFilter from '@genesis-labs/web-core/core/components/ui/icons/IconFilter.vue';
import IconSort from '@genesis-labs/web-core/core/components/ui/icons/IconSort.vue';
import IconSearch from '@genesis-labs/web-core/core/components/ui/icons/IconSearch.vue';
import LayoutSwitcherAlt from '../ui/dropdown/LayoutSwitcherAlt.vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import Carrousel, { type CarouselSlide } from '@genesis-labs/web-core/core/components/ui/carrousel/Carrousel.vue';

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
    showSort?: boolean;
    showCarousel?: boolean;
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
    'back': [];
    'openFilter': []; // ✅ Réintégré pour ouvrir le popup
}>();

const carouselSlides = ref<CarouselSlide[]>([
    { color: '#3B82F6', label: 'Slide 1 - Bleu' },
    { color: '#EF4444', label: 'Slide 2 - Rouge' },
    { color: '#10B981', label: 'Slide 3 - Vert' },
    { color: '#F59E0B', label: 'Slide 4 - Orange' },
    { color: '#8B5CF6', label: 'Slide 5 - Violet' },
]);

const internalDisplayMode = computed({
    get: () => props.displayMode,
    set: (value) => emit('update:displayMode', value)
});

const internalMode = computed({
    get: () => props.mode,
    set: (value) => emit('update:mode', value as CollectionMode)
});
</script>