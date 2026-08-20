<template>
    <div
        class="relative aspect-square border border-secondary rounded-lg p-3 cursor-pointer transition-all duration-200
               flex flex-col items-center justify-center gap-2"
        :class="{
            'bg-accent/10 border-accent': selected,
            'bg-bg-light hover:border-accent/50': !selected
        }"
        @click="$emit('click')"
    >
        <!-- Logo SVG ou placeholder -->
        <div class="flex items-center justify-center w-10 h-10 rounded bg-secondary text-text-muted text-xs font-mono">
            <slot name="logo">{{ initials }}</slot>
        </div>

        <!-- Nom + sous-titre -->
        <div class="flex flex-col items-center gap-0.5 text-center">
            <span class="text-xs font-semibold text-text leading-tight">{{ label }}</span>
            <span v-if="sublabel" class="text-text-muted truncate" style="font-size: 10px;">{{ sublabel }}</span>
        </div>

        <!-- Bouton info (bottom-right) -->
        <GenesisButtonIcon
            v-if="showInfoButton"
            size="xs"
            :visibleBackground="false"
            class="absolute bottom-1 right-1 opacity-60 hover:opacity-100"
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
     * Affiche un bouton "?" dans le coin inférieur droit
     * pour ouvrir un panneau d'informations supplémentaires.
     * @default false
     */
    showInfoButton?: boolean;
}>(), {
    selected: false,
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
</script>