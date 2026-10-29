<!-- genesis-sdk-web/genesis-web-core/src/core/components/layouts/Popup/WarningPopup.vue -->
<template>
    <BaseFormPopup :title="title" :size="size" :position="position" @close="$emit('cancel')">
        <div class="flex flex-col gap-4 py-4 flex-1">
            <!-- Message + conséquences : la couleur de la bordure dépend de la variante -->
            <div class="flex flex-col gap-3 border-l-4 pl-4" :class="accentClass">
                <p class="text-sm text-text">{{ message }}</p>

                <ul v-if="details.length" class="flex flex-col gap-1 list-disc pl-5 text-sm text-text-muted">
                    <li v-for="detail in details" :key="detail">{{ detail }}</li>
                </ul>
            </div>

            <!-- Actions -->
            <div class="flex justify-end items-center gap-2 pt-4 mt-auto border-t border-bg-neutral-genesis">
                <GenesisButton variant="secondary" @click="$emit('cancel')">
                    {{ cancelLabel }}
                </GenesisButton>
                <GenesisButton variant="accent" @click="$emit('confirm')">
                    {{ confirmLabel }}
                </GenesisButton>
            </div>
        </div>
    </BaseFormPopup>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import BaseFormPopup from '@genesis-labs/web-core/core/components/layouts/Popup/BaseFormPopup.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import type { PopupPosition, PopupSize } from './popup.types';

const props = withDefaults(defineProps<{
    title: string;
    message: string;
    /** Conséquences listées sous le message (ex. données réinitialisées) */
    details?: string[];
    confirmLabel?: string;
    cancelLabel?: string;
    /** 'danger' = action destructive */
    variant?: 'warning' | 'danger';
    size?: PopupSize;
    position?: PopupPosition;
}>(), {
    details: () => [],
    confirmLabel: 'Continuer',
    cancelLabel: 'Annuler',
    variant: 'warning',
    size: 'md',
    position: 'center'
});

defineEmits<{
    confirm: [];
    /** Bouton « Annuler » ou fermeture de la fenêtre (croix) */
    cancel: [];
}>();

// Adapte ces classes à tes tokens de couleur si tu en as pour warning / error
const accentClass = computed(() =>
    props.variant === 'danger' ? 'border-red-500' : 'border-amber-500'
);
</script>