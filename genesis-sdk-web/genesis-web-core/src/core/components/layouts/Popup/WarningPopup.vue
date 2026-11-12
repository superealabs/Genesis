<template>
    <BaseFormPopup :title="title" :size="size" :position="position" @close="$emit('cancel')">
        <div class="flex flex-col h-full gap-4">

            <!-- ═══ ZONE SCROLLABLE : contenu (image + titre + message + détails + slot) ═══ -->
            <div class="flex-1 min-h-0 overflow-y-auto custom-scrollbar flex flex-col gap-6 py-4">
                <img
                    :src="WarningPicture"
                    alt="Alerte"
                    class="w-1/2 max-w-full h-64 object-contain shrink-0 mx-auto"
                />

                <div class="flex flex-col justify-center gap-3 px-8" :class="accentClass">
                    <!-- ✅ NOUVEAU : sous-titre dans le contenu (défaut "Warning") -->
                    <h3 class="text-lg font-semibold text-text text-center">
                        {{ contentTitle }}
                    </h3>

                    <!-- Message principal -->
                    <p class="text-sm text-text">{{ message }}</p>

                    <!-- Détails -->
                    <ul v-if="details.length" class="flex flex-col gap-1 list-disc pl-5 text-sm text-text-muted">
                        <li v-for="detail in details" :key="detail">{{ detail }}</li>
                    </ul>
                </div>

                <!-- Slot custom (ex: liste scrollable des doublons) -->
                <slot name="custom" />
            </div>

            <!-- ═══ ACTIONS FIXES : toujours visibles ═══ -->
            <div class="flex justify-end items-center gap-2 pt-4 border-t border-bg-neutral-genesis shrink-0">
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
import WarningPicture from '../../../../assets/PICTURES/warning.png';
import type { PopupPosition, PopupSize } from './popup.types';

const props = withDefaults(defineProps<{
    title: string;
    /** ✅ NOUVEAU : sous-titre affiché dans le contenu (au-dessus du message) */
    contentTitle?: string;
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
    position: 'center',
    contentTitle: 'Warning',   // ✅ défaut
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