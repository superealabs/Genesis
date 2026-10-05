<template>
    <!-- ═══ MODE LIST (anciennement LINE : Liste Flex) ═══ -->
    <GenesisLine
        v-if="display === 'list'"
        :container-classes="containerClasses"
        :badge-classes="badgeClasses"
        :logo-classes="logoClasses"
        :label-classes="labelClasses"
        :sublabel-classes="sublabelClasses"
        :complementary-classes="complementaryClasses"
        :label="label"
        :sublabel="sublabel"
        :badge="badge"
        :show-info-button="showInfoButton"
        :show-complementary="showComplementary"
        :show-logo="showLogo"
        :deletable="deletable"
        :initials="initials"
        :has-header-slot="hasHeaderSlot"
        :has-complementary="hasComplementary"
        :mascot-src="mascotSrc"
        :mascot-size="mascotSize"
        :selected="selected"
        @click="$emit('click', $event)"
        @info="$emit('info')"
        @close="$emit('close')"
    >
        <slot name="logo">
            <GenesisLogo v-if="logoSrc" :src="logoSrc" :alt="label" />
        </slot>
        <template #header><slot name="header" /></template>
        <template #complementary><slot name="complementary" /></template>
    </GenesisLine>

    <!-- ═══ MODE TABLE (anciennement LIST : Tableau) ═══ -->
    <GenesisTableLine
        v-else-if="display === 'table'"
        :container-classes="containerClasses"
        :badge-classes="badgeClasses"
        :badge="badge"
        :show-info-button="showInfoButton"
        :deletable="deletable"
        @click="$emit('click', $event)"
        @info="$emit('info')"
        @close="$emit('close')"
    >
        <slot />
    </GenesisTableLine>

    <!-- ═══ MODE GRID (Carte) ═══ -->
    <GenesisCard
        v-else
        :container-classes="containerClasses"
        :layout-classes="layoutClasses"
        :badge-classes="badgeClasses"
        :logo-classes="logoClasses"
        :text-classes="textClasses"
        :label-classes="labelClasses"
        :sublabel-classes="sublabelClasses"
        :complementary-classes="complementaryClasses"
        :info-button-classes="infoButtonClasses"
        :label="label"
        :sublabel="sublabel"
        :badge="badge"
        :show-info-button="showInfoButton"
        :show-complementary="showComplementary"
        :show-logo="showLogo"
        :deletable="deletable"
        :initials="initials"
        :has-header-slot="hasHeaderSlot"
        :has-complementary="hasComplementary"
        :mascot-src="mascotSrc"
        :mascot-size="mascotSize"
        :selected="selected"
        @click="$emit('click', $event)"
        @info="$emit('info')"
        @close="$emit('close')"
    >
        <template #logo>
            <slot name="logo">
                <GenesisLogo v-if="logoSrc" :src="logoSrc" :alt="label" />
            </slot>
        </template>
        <template #header><slot name="header" /></template>
        <template #complementary><slot name="complementary" /></template>
    </GenesisCard>
</template>

<script setup lang="ts">
import { computed, inject, useSlots } from 'vue';
import { GENESIS_LIST_CONTEXT, type GenesisListContext } from './GenesisItem.types';
import GenesisLine from './GenesisLine.vue';
import GenesisTableLine from './GenesisTableLine.vue';
import GenesisCard from './GenesisCard.vue';
import genieUrl from '../../../../../assets/LOGO/Genesis/genie-normal.svg?url';
import GenesisLogo from '../../../ui/logo/GenesisLogo.vue';

const props = withDefaults(defineProps<{
    label?: string;
    sublabel?: string;
    selected?: boolean;
    badge?: string | null;
    showInfoButton?: boolean;
    showComplementary?: boolean;
    showLogo?: boolean;
    /** Hauteur de la mascotte en px (modes grid et list) */
    mascotSize?: number;
    logoSrc?: string | null;
}>(), {
    selected: false,
    badge: null,
    showInfoButton: false,
    showComplementary: true,
    showLogo: true,
    mascotSize: 32,
    logoSrc: null,
});

defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();

const context = inject<GenesisListContext>(GENESIS_LIST_CONTEXT);
const display = computed(() => context?.display.value ?? 'grid');
const deletable = computed(() => context?.deletable.value ?? false);

const slots = useSlots();
const hasComplementary = computed(() => !!slots.complementary);
const hasHeaderSlot = computed(() => !!slots.header);

// ═══ SOURCE UNIQUE DE LA MASCOTTE (DRY) ═══
const mascotSrc = genieUrl;

const initials = computed(() => {
    if (!props.label) return '';
    return props.label.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 3);
});

// ═══ SOURCE DE VÉRITÉ UNIQUE POUR LE CSS (DRY) ═══

const containerClasses = computed(() => {
    // Grid : la sélection est signalée par le remplissage animé du logo + texte + mascotte
    if (display.value === 'grid') {
        return 'bg-bg-light hover:border-primary/50';
    }
    // List : même principe que grid (pas de changement de fond à la sélection)
    if (display.value === 'list') {
        return 'bg-bg-light';
    }
    // Table : comportement inchangé (fond accent à la sélection)
    return props.selected ? 'bg-accent/10' : 'bg-bg-light hover:bg-bg-light/50';
});

const badgeClasses = 'w-5 h-5 rounded-full bg-accent text-bg text-xs font-bold flex items-center justify-center shadow-sm';

const layoutClasses = computed(() => {
    const hasComp = props.showComplementary && hasComplementary.value;
    return hasComp
        ? 'flex flex-col items-center gap-4 p-4'
        : 'aspect-square flex flex-col items-center justify-center gap-4 p-4';
});

const logoClasses = computed(() => {
    return 'flex items-center justify-center min-w-[48px] min-h-[48px] rounded text-text-muted text-xs font-mono flex-shrink-0';
});

const textClasses = computed(() => {
    // En mode 'list' (flex), le texte est aligné à gauche, pas centré
    if (display.value === 'list') {
        return 'flex flex-col gap-1 w-full';
    }
    if (props.showLogo) {
        return 'flex flex-col text-center gap-2 w-full';
    }
    return 'flex flex-col gap-2 w-full';
});

const labelClasses = 'text-xs font-semibold text-text leading-tight';
const sublabelClasses = 'text-[10px] text-text-muted truncate';

const complementaryClasses = computed(() => {
    // En mode 'list' (flex), on retire la bordure du haut pour un rendu plus épuré à droite
    if (display.value === 'list') {
        return 'flex items-center gap-2';
    }
    return 'w-full pt-2 mt-1 border-t border-secondary/50 flex flex-col items-center gap-2';
});

const infoButtonClasses = computed(() => {
    // En mode 'list' (flex), le bouton n'a pas besoin d'être absolute
    if (display.value === 'list') {
        return 'opacity-60 hover:opacity-100';
    }
    return 'absolute bottom-1 right-1 opacity-60 hover:opacity-100';
});
</script>