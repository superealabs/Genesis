<template>
    <BasePopup
        :title="title"
        :size="size"
        :isClosable="isClosable"
        :draggable="draggable"
        @close="$emit('close')"
    >
        <div class="flex flex-col gap-1">
            <button
                v-for="option in options"
                :key="String(option.id)"
                type="button"
                class="w-full text-left px-3 py-2 rounded-md transition-all duration-150
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
    /** Titre du popup */
    title?: string;
    /** Liste des choix disponibles */
    options: SelectionOption[];
    /** Id du choix actuellement sélectionné (pour le mettre en évidence) */
    selectedId?: string | number;
    /** Taille du popup */
    size?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | '3xl' | 'full';
    /** Affiche le bouton de fermeture */
    isClosable?: boolean;
    /** Permet de déplacer le popup */
    draggable?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
    title: 'Sélectionner',
    size: 'sm',
    isClosable: true,
    draggable: true
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