<template>
    <div
        class="group relative rounded-lg cursor-pointer transition-all duration-200"
        :class="[layoutClasses, containerClasses]"
        @click="$emit('click', $event)"
    >
        <!-- Mascotte -->
        <GenesisMascot :src="mascotSrc" :size="mascotSize" :selected="selected" />

        <div
            v-if="hasHoverSlot || feature || (selected && feature)"
            class="absolute top-0 left-0 z-10 px-4 py-2 rounded text-md font-semibold
                opacity-0 pointer-events-none
                transition-all duration-200
                group-hover:opacity-100"
            :class="selected ? 'bg-primary text-primary-text' : 'bg-bg-secondary text-secondary'"
        >
            <slot v-if="hasHoverSlot" name="hover" />
            <template v-else>{{ feature }}</template>
        </div>

        <!-- Badge + close -->
        <div v-if="badge || deletable" class="absolute top-2 right-2 flex items-center gap-1 z-10">
            <GenesisButtonIcon
                v-if="deletable"
                size="xs"
                variant="tertiary"
                class="opacity-60 hover:opacity-100"
                @click.stop="$emit('close')"
            >
                <IconTrashAlt />
            </GenesisButtonIcon>
            <div v-if="badge" :class="badgeClasses">
                {{ badge }}
            </div>
        </div>

        <!-- Identité : logo + texte (double couche pour l'effet de sélection) -->
        <GenesisSelectableIdentity
            class="w-full"
            :layer-class="`flex flex-col items-center w-full ${hasText ? 'gap-4' : ''}`"
            :selected="selected"
        >
            <!-- Logo -->
            <div v-if="showLogo" :class="logoClasses">
                <slot name="logo">{{ initials }}</slot>
            </div>

            <!-- Zone texte : masquée si aucun label/sublabel/header n'est fourni -->
             <div v-if="hasText" :class="textClasses">
                <template v-if="hasHeaderSlot">
                    <slot name="header" />
                </template>
                <template v-else>
                    <span v-if="label" :class="labelClasses">{{ label }}</span>
                    <span v-if="sublabel" :class="sublabelClasses">{{ sublabel }}</span>
                </template>
            </div>
        </GenesisSelectableIdentity>

        <!-- Contenu complémentaire -->
        <div v-if="showComplementary && hasComplementary" :class="complementaryClasses">
            <slot name="complementary" />
        </div>

        <!-- Bouton info -->
        <GenesisButtonIcon
            v-if="showInfoButton"
            size="xs"
            variant="tertiary"
            :class="infoButtonClasses"
            :hover-himself="true"
            @click.stop="$emit('info')"
        >
            <IconHelpCircle />
        </GenesisButtonIcon>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'; // Ajout nécessaire pour le computed
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../../ui/icons/IconTrashAlt.vue';
import GenesisMascot from './GenesisMascot.vue';
import GenesisSelectableIdentity from './GenesisSelectableIdentity.vue';

const props = withDefaults(defineProps<{
    containerClasses: string;
    layoutClasses: string;
    badgeClasses: string;
    logoClasses: string;
    textClasses: string;
    labelClasses: string;
    sublabelClasses: string;
    complementaryClasses: string;
    infoButtonClasses: string;
    label?: string;
    sublabel?: string;
    badge?: string | null;
    showInfoButton: boolean;
    showComplementary: boolean;
    showLogo: boolean;
    deletable: boolean;
    initials: string;
    hasHeaderSlot: boolean;
    hasComplementary: boolean;
    mascotSrc: string;
    mascotSize?: number;
    selected?: boolean;
    feature?: string;
    hasHoverSlot?: boolean;
}>(), {
    selected: false,
    mascotSize: 32
});

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();

// ✅ Vérification simple : y a-t-il du texte à afficher ?
const hasText = computed(() => props.hasHeaderSlot || !!props.label || !!props.sublabel);
</script>