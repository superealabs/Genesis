<template>
    <!-- ═══ MODE LIST ═══ -->
    <tr
        v-if="display === 'list'"
        class="table-row transition-colors group cursor-pointer"
        :class="{
            'bg-accent/10': selected,
            'bg-bg hover:bg-bg-light/50': !selected
        }"
        @click="$emit('click', $event)"
    >
        <!-- Parent fournit ses <td> via #default -->
        <slot />

        <!-- Colonne actions — injectée automatiquement en dernière position -->
        <td
            v-if="deletable || showInfoButton"
            class="p-3 text-center"
        >
            <div class="flex items-center justify-center gap-1">
                <GenesisButtonIcon
                    v-if="showInfoButton"
                    size="lg"
                    variant="tertiary"
                    :hover-himself="true"
                    @click.stop="$emit('info')"
                >
                    <IconHelpCircle />
                </GenesisButtonIcon>
                <GenesisButtonIcon
                    v-if="deletable"
                    size="lg"
                    variant="tertiary"
                    @click.stop="$emit('close')"
                >
                    <IconTrashAlt/>
                </GenesisButtonIcon>
            </div>
        </td>
    </tr>

    <!-- ═══ MODE GRID ═══ -->
    <div
        v-else
        class="relative rounded-lg cursor-pointer transition-all duration-200"
        :class="[
            layoutClasses,
            {
                'bg-accent/10 border-accent': selected,
                'bg-bg-light hover:border-accent/50': !selected
            }
        ]"
        @click="$emit('click', $event)"
    >
        <!-- Badge + close -->
        <div
            v-if="badge || deletable"
            class="absolute top-2 right-2 flex items-center gap-1 z-10"
        >
            <GenesisButtonIcon
                v-if="deletable"
                size="xs"
                variant="tertiary"
                class="opacity-60 hover:opacity-100"
                @click.stop="$emit('close')"
            >
                <IconTrashAlt/>
            </GenesisButtonIcon>
            <div
                v-if="badge"
                class="w-5 h-5 rounded-full bg-accent text-bg text-xs font-bold flex items-center justify-center shadow-sm"
            >
                {{ badge }}
            </div>
        </div>

        <!-- Logo -->
        <div v-if="showLogo" :class="logoClasses">
            <slot name="logo">{{ initials }}</slot>
        </div>

        <!-- Zone texte : #header custom ou label/sublabel -->
        <div :class="textClasses">
            <template v-if="hasHeaderSlot">
                <slot name="header" />
            </template>
            <template v-else>
                <span v-if="label" :class="labelClasses">{{ label }}</span>
                <span v-if="sublabel" :class="sublabelClasses">{{ sublabel }}</span>
            </template>
        </div>

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
import { computed, inject, useSlots } from 'vue';
import { GENESIS_LIST_CONTEXT, type GenesisListContext } from './GenesisItem.types';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../ui/icons/IconTrashAlt.vue';

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

const emit = defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();

const context  = inject<GenesisListContext>(GENESIS_LIST_CONTEXT);
const display  = computed(() => context?.display.value  ?? 'grid');
const deletable = computed(() => context?.deletable.value ?? false);

const slots = useSlots();
const hasComplementary = computed(() => !!slots.complementary);
const hasHeaderSlot    = computed(() => !!slots.header);

const initials = computed(() => {
    if (!props.label) return '';
    return props.label.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 3);
});

// ═══ Layout global ═══
const layoutClasses = computed(() => {
    const hasComp = props.showComplementary && hasComplementary.value;
    return hasComp
        ? 'flex flex-col items-center gap-4 p-4'
        : 'aspect-square flex flex-col items-center justify-center gap-4 p-4';
});

// ═══ Logo ═══
const logoClasses = computed(() => {
    return 'flex items-center justify-center w-10 h-10 rounded bg-secondary text-text-muted text-xs font-mono';
});

// ═══ Zone texte ═══
const textClasses = computed(() => {
    if (props.showLogo == true) {
        return 'flex flex-col text-center gap-2 w-full';        
    }
    return 'flex flex-col gap-2 w-full';
});

// ═══ Label ═══
const labelClasses = computed(() => {
    return 'text-xs font-semibold text-text leading-tight';
});

// ═══ Sublabel ═══
const sublabelClasses = computed(() => {
    return 'text-[10px] text-text-muted truncate';
});

// ═══ Contenu complémentaire ═══
const complementaryClasses = computed(() => {
    return 'w-full pt-2 mt-1 border-t border-secondary/50 flex flex-col items-center gap-2';
});

// ═══ Bouton info ═══
const infoButtonClasses = computed(() => {
    return 'absolute bottom-1 right-1 opacity-60 hover:opacity-100';
});
</script>