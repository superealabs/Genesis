<template>
    <!-- ═══ MODE LIST ═══ -->
    <GenesisTableLine
        v-if="display === 'list'"
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

    <!-- ═══ MODE GRID ═══ -->
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
        @click="$emit('click', $event)"
        @info="$emit('info')"
        @close="$emit('close')"
    >
        <template #logo><slot name="logo" /></template>
        <template #header><slot name="header" /></template>
        <template #complementary><slot name="complementary" /></template>
    </GenesisCard>
</template>

<script setup lang="ts">
import { computed, inject, useSlots } from 'vue';
import { GENESIS_LIST_CONTEXT, type GenesisListContext } from './GenesisItem.types';
import GenesisTableLine from './GenesisTableLine.vue';
import GenesisCard from './GenesisCard.vue';

const props = withDefaults(defineProps<{
    label?: string;
    sublabel?: string;
    selected?: boolean;
    badge?: string | null;
    showInfoButton?: boolean;
    showComplementary?: boolean;
    showLogo?: boolean;
}>(), {
    selected: false,
    badge: null,
    showInfoButton: false,
    showComplementary: true,
    showLogo: true,
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

const initials = computed(() => {
    if (!props.label) return '';
    return props.label.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 3);
});

// ═══ SOURCE DE VÉRITÉ UNIQUE POUR LE CSS (DRY) ═══

const containerClasses = computed(() => {
    if (props.selected) {
        return display.value === 'list' 
            ? 'bg-accent/10' 
            : 'bg-accent/10 border-accent';
    }
    return display.value === 'list' 
        ? 'bg-bg-light hover:bg-bg-light/50' 
        : 'bg-bg-light hover:border-accent/50';
});

const badgeClasses = 'w-5 h-5 rounded-full bg-accent text-bg text-xs font-bold flex items-center justify-center shadow-sm';

const layoutClasses = computed(() => {
    const hasComp = props.showComplementary && hasComplementary.value;
    return hasComp
        ? 'flex flex-col items-center gap-4 p-4'
        : 'aspect-square flex flex-col items-center justify-center gap-4 p-4';
});

const logoClasses = computed(() => {
    return 'flex items-center justify-center w-10 h-10 rounded bg-secondary text-text-muted text-xs font-mono';
});

const textClasses = computed(() => {
    if (props.showLogo == true) {
        return 'flex flex-col text-center gap-2 w-full';        
    }
    return 'flex flex-col gap-2 w-full';
});

const labelClasses = 'text-xs font-semibold text-text leading-tight';
const sublabelClasses = 'text-[10px] text-text-muted truncate';

const complementaryClasses = computed(() => {
    return 'w-full pt-2 mt-1 border-t border-secondary/50 flex flex-col items-center gap-2';
});

const infoButtonClasses = 'absolute bottom-1 right-1 opacity-60 hover:opacity-100';
</script>