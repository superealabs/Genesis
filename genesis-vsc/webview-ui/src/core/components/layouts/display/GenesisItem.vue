<template>
    <div
        class="relative border border-secondary rounded-lg p-3 cursor-pointer transition-all duration-200"
        :class="[
            layoutClasses,
            {
                'bg-accent/10 border-accent': selected,
                'bg-bg-light hover:border-accent/50': !selected
            }
        ]"
        @click="$emit('click')"
    >
        <!-- Badge de sélection (top-right) -->
        <div
            v-if="slot"
            class="absolute top-1 right-1 w-5 h-5 rounded-full bg-accent text-bg text-xs font-bold
                   flex items-center justify-center shadow-sm"
        >
            {{ slot }}
        </div>

        <!-- Mode Card : layout vertical centré -->
        <template v-if="layoutMode === 'card'">
            <!-- Logo SVG ou placeholder -->
            <div class="flex items-center justify-center w-10 h-10 rounded bg-secondary text-text-muted text-xs font-mono">
                <slot name="logo">{{ initials }}</slot>
            </div>

            <!-- Nom + sous-titre -->
            <div class="flex flex-col items-center gap-0.5 text-center">
                <span class="text-xs font-semibold text-text leading-tight">{{ label }}</span>
                <span v-if="sublabel" class="text-text-muted truncate" style="font-size: 10px;">{{ sublabel }}</span>
            </div>
        </template>

        <!-- Mode List : layout horizontal -->
        <template v-else>
            <!-- Logo SVG ou placeholder -->
            <div class="flex items-center justify-center w-8 h-8 rounded bg-secondary text-text-muted text-xs font-mono flex-shrink-0">
                <slot name="logo">{{ initials }}</slot>
            </div>

            <!-- Nom + sous-titre -->
            <div class="flex flex-col gap-0.5 min-w-0 flex-1">
                <span class="text-sm font-semibold text-text leading-tight truncate">{{ label }}</span>
                <span v-if="sublabel" class="text-xs text-text-muted truncate">{{ sublabel }}</span>
            </div>

            <!-- Informations complémentaires (slot) -->
            <div v-if="$slots.complementary" class="flex items-center gap-2 flex-shrink-0">
                <slot name="complementary" />
            </div>
        </template>

        <!-- Bouton info (bottom-right en card, right en list) -->
        <GenesisButtonIcon
            v-if="showInfoButton"
            size="xs"
            :visibleBackground="false"
            :class="infoButtonClasses"
            @click.stop="$emit('info')"
            :hover-himself="true"
        >
            <IconHelpCircle color="var(--color-text-muted)" />
        </GenesisButtonIcon>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import GenesisButtonIcon from '@/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@/core/components/ui/icons/IconHelpCircle.vue';

const props = withDefaults(defineProps<{
    label: string;
    sublabel?: string;
    selected?: boolean;
    /**
     * Mode d'affichage : card (grille) ou list (ligne)
     * @default 'card'
     */
    layoutMode?: 'card' | 'list';
    /**
     * Badge de sélection affiché en top-right (ex: 'A', 'B', 'C', 'D')
     */
    slot?: string | null;
    /**
     * Affiche un bouton "?" pour ouvrir un panneau d'informations
     * @default false
     */
    showInfoButton?: boolean;
}>(), {
    selected: false,
    layoutMode: 'card',
    slot: null,
    showInfoButton: false
});

defineEmits<{
    click: [];
    info: [];
}>();

const initials = computed(() => {
    return props.label
        .split(' ')
        .map(word => word[0])
        .join('')
        .toUpperCase()
        .slice(0, 3);
});

// Classes de layout selon le mode
const layoutClasses = computed(() => {
    if (props.layoutMode === 'card') {
        return 'aspect-square flex flex-col items-center justify-center gap-2';
    }
    return 'aspect-auto flex flex-row items-center gap-3';
});

// Position du bouton info selon le mode
const infoButtonClasses = computed(() => {
    if (props.layoutMode === 'card') {
        return 'absolute bottom-1 right-1 opacity-60 hover:opacity-100';
    }
    return 'opacity-60 hover:opacity-100 flex-shrink-0';
});
</script>