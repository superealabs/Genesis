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
            <!-- ✅ PASSAGE DE LA PROP SIZE -->
            <GenesisLogo v-if="logoSrc" :src="logoSrc" :alt="label" :size="size" />
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
                <!-- ✅ PASSAGE DE LA PROP SIZE -->
                <GenesisLogo v-if="logoSrc" :src="logoSrc" :alt="label" :size="size" />
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
    /** ✅ NOUVEAU : Taille sémantique du composant (impacte le logo et les espacements) */
    size?: 'sm' | 'md' | 'lg';
}>(), {
    selected: false,
    badge: null,
    showInfoButton: false,
    showComplementary: true,
    showLogo: true,
    mascotSize: 32,
    logoSrc: null,
    size: 'md', // ✅ Valeur par défaut
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

const mascotSrc = genieUrl;

const initials = computed(() => {
    if (!props.label) return '';
    return props.label.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 3);
});

const containerClasses = computed(() => {
    if (props.selected) {
        return 'bg-bg-light/5 border-2 border-primary';
        // return 'bg-bg-light border-b-8 border-bg';

    }
    if (display.value === 'grid' || display.value === 'list') {
        return 'bg-bg-light border-b-8 border-bg active:border active:border-transparent';
    }
    return 'bg-bg-light hover:bg-bg-light/50';
});

const badgeClasses = 'w-5 h-5 rounded-full bg-primary text-primary-text text-xs font-bold flex items-center justify-center';

const layoutClasses = computed(() => {
    const hasComp = props.showComplementary && hasComplementary.value;
    // ✅ AJUSTEMENT : En mode 'sm', on réduit légèrement le padding global
    const padding = props.size === 'sm' ? 'p-2' : 'p-4';
    return hasComp
        ? `flex flex-col items-center gap-4 ${padding}`
        : `aspect-square flex flex-col items-center justify-center gap-4 ${padding}`;
});

const logoClasses = computed(() => {
    // ✅ DYNAMISME : La taille minimale du conteneur du logo s'adapte à la prop 'size'
    const sizeMap = {
        sm: 'min-w-[32px] min-h-[32px]',
        md: 'min-w-[48px] min-h-[48px]',
        lg: 'min-w-[64px] min-h-[64px]'
    };
    const sizeClass = sizeMap[props.size] || sizeMap.md;
    
    return `flex items-center justify-center ${sizeClass} rounded text-text-muted text-xs font-mono flex-shrink-0`;
});

const textClasses = computed(() => {
    if (display.value === 'list') {
        return 'flex flex-col gap-1 w-full';
    }
    if (props.showLogo) {
        return 'flex flex-col text-center gap-2 w-full';
    }
    return 'flex flex-col gap-2 w-full';
});

// ✅ AJUSTEMENT : En mode 'sm', on peut réduire légèrement la taille de la police du label
const labelClasses = computed(() => {
    return props.size === 'sm' 
        ? 'text-[11px] font-semibold text-text leading-tight' 
        : 'text-xs font-semibold text-text leading-tight';
});

const sublabelClasses = 'text-[10px] text-text-muted truncate';

const complementaryClasses = computed(() => {
    if (display.value === 'list') {
        return 'flex items-center gap-2';
    }
    return 'w-full pt-2 mt-1 border-t border-secondary/50 flex flex-col items-center gap-2';
});

const infoButtonClasses = computed(() => {
    if (display.value === 'list') {
        return 'opacity-60 hover:opacity-100';
    }
    return 'absolute bottom-1 right-1 opacity-60 hover:opacity-100';
});
</script>