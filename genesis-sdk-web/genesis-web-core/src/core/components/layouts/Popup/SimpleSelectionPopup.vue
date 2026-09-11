<template>
    <BasePopup
        :show="show"
        :anchor="anchor"
        :mouseX="mouseX"
        :mouseY="mouseY"
        :position="position"    
        :offset="offset"
        @close="$emit('close')"
    >
        <div class="flex flex-col gap-1 p-1 min-w-[150px]">
            <button
                v-for="option in options"
                :key="String(option.id)"
                type="button"
                class="w-full text-left px-3 py-1 rounded-md transition-all duration-150
                       flex flex-col gap-0.5 cursor-pointer
                       focus:outline-none focus:ring-2 focus:ring-accent/50"
                :class="[
                    isSelected(option.id)
                        ? 'bg-accent/15 border border-accent'
                        : 'border border-transparent hover:bg-[var(--color-hover-ghost)]',
                    option.disabled
                        ? 'opacity-50 cursor-not-allowed pointer-events-none'
                        : ''
                ]"
                :disabled="option.disabled"
                @click="$emit('select', option.id)"
            >
                <span class="text-sm font-medium text-text">
                    {{ option.label }}
                </span>
                <span v-if="option.description" class="text-xs text-text-muted">
                    {{ option.description }}
                </span>
            </button>
        </div>
    </BasePopup>
</template>

<script setup lang="ts">
import BasePopup from '@/core/components/layouts/Popup/BasePopup.vue';
import type { PopupAnchorPosition } from './popup.types';

export interface SelectionOption {
    /** Identifiant unique du choix (retourné au clic) */
    id: string | number;
    /** Texte principal affiché */
    label: string;
    /** Texte secondaire optionnel (ex: nom du framework dans le slot) */
    description?: string;
    /** Désactive individuellement ce choix */
    disabled?: boolean;
}

interface Props {
    /** Contrôle la visibilité du popup */
    show?: boolean;
    /** Liste des choix disponibles */
    options: SelectionOption[];
    /** Id du choix actuellement sélectionné (pour le mettre en évidence) */
    selectedId?: string | number;
    /** Élément de référence pour le positionnement */
    anchor?: HTMLElement | null;
    /** Coordonnée X de la souris */
    mouseX?: number | null;
    /** Coordonnée Y de la souris */
    mouseY?: number | null;
    /** Position par rapport à l'ancre ou à la souris */
    position?: PopupAnchorPosition;
    /** Espace en pixels entre la cible et le popup */
    offset?: number;
}

const props = withDefaults(defineProps<Props>(), {
    show: true,
    anchor: null,
    mouseX: null,
    mouseY: null,
    position: 'bottom-left',
    offset: 4
});

defineEmits<{
    /** Émis quand l'utilisateur clique sur un choix */
    select: [id: string | number];
    /** Émis quand le popup est fermé sans sélection */
    close: [];
}>();

function isSelected(id: string | number): boolean {
    return props.selectedId !== undefined && props.selectedId === id;
}
</script>