<template>
    <div class="flex flex-col gap-16 w-full">
            <Carrousel
                v-if="showCarousel"
                :slides="carouselSlides"
                slide-height="300px"
                :auto-play="true"
                class="rounded-t-lg flex-shrink-0"
            >
                <template #bottom="{ isStuck}">
                    <div 
                        class="flex gap-4 rounded-b-lg" 
                        id="background-header"
                        :class="isStuck ? 'bg-white shadow-sm' : 'bg-transparent'"
                        >
                        <div class="flex items-center gap-4 px-8 pb-4 pt-2">
                            <GenesisBackButton v-if="showBackButton" @click="$emit('back')" class="shrink-0" />
                            <h2 
                                class="font-semibold font-heading text-4xl truncate transition-colors duration-300"
                                :class="isStuck ? 'text-text' : 'text-white'"
                            >
                                <slot name="title">{{ title }}</slot>
                            </h2>
                        </div>

                        <div 
                            class="relative flex items-stretch w-full"                        
                        >

                            <div 
                                class="bar-shape absolute inset-0 pointer-events-none" aria-hidden="true"
                                :class="isStuck ? 'bg-white' : 'bg-bg-dark'"
                                />

                            <div class="relative flex items-center gap-2 flex-1 min-w-0 pl-24 pr-6">
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

                            <div class="relative flex gap-2 items-center flex-shrink-0 pr-6">
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

        <div class="flex-1 min-h-0">
            <slot />
            <SimpleSelectionPopup
                v-if="showReplacePopup"
                :show="showReplacePopup"
                :mouseX="mouseX ?? 0"
                :mouseY="mouseY ?? 0"
                :options="replaceOptions ?? []"
                position="bottom-right"
                @select="$emit('select-replace', $event)"
                @close="$emit('close-replace')"
            />
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
import SimpleSelectionPopup from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { SelectionOption } from '@genesis-labs/web-core/core/components/layouts/Popup/SimpleSelectionPopup.vue';
import type { DisplayMode } from './display/items/GenesisItem.types.ts';

export type CollectionMode = 'selection' | 'compare';

interface Props {
    title?: string;
    searchValue?: string;
    searchPlaceholder?: string;
    displayMode: DisplayMode;
    mode?: CollectionMode;
    align?: 'left' | 'right';
    showBackButton?: boolean;
    showFilter?: boolean;
    showSort?: boolean;
    showCarousel?: boolean;

    replaceOptions?: SelectionOption[];
    showReplacePopup?: boolean;
    mouseX?: number | null;
    mouseY?: number | null;
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
    'update:displayMode': [value: DisplayMode];
    'update:mode': [value: CollectionMode];
    'back': [];
    'openFilter': [];

    'select-replace': [slotId: string | number];
    'close-replace': [];
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
<style scoped>
.bar-shape {
    /* Largeur du capuchon (la partie incurvée à gauche) */
    --cap: 96px;

    /* Tracé du capuchon : viewBox 96 x 80, étiré à la hauteur de la barre */
    --cap-shape: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 96 80' preserveAspectRatio='none'%3E%3Cpath d='M96 0H90Q80 0 72.9 7.1L0 80H96Z'/%3E%3C/svg%3E");

    -webkit-mask:
        var(--cap-shape) left top / var(--cap) 100% no-repeat,
        linear-gradient(#000, #000) right top / calc(100% - var(--cap) + 1px) 100% no-repeat;
    mask:
        var(--cap-shape) left top / var(--cap) 100% no-repeat,
        linear-gradient(#000, #000) right top / calc(100% - var(--cap) + 1px) 100% no-repeat;
}
</style>