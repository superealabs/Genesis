<template>
    <div
        class="genesis-card relative rounded-lg cursor-pointer transition-all duration-200"
        :class="[layoutClasses, containerClasses, { 'is-selected': selected }]"
        @click="$emit('click', $event)"
    >

        <!-- Mascotte : posée sur le bord haut, rognée par son masque -->
        <div
            class="absolute bottom-full left-1/2 -translate-x-1/2 -mb-px overflow-hidden pointer-events-none"
            :style="{ height: `${mascotSize}px` }"
            aria-hidden="true"
        >
            <img
                :src="genieUrl"
                alt=""
                class="mascot block h-full w-auto max-w-none select-none"
                draggable="false"
            />
        </div>
        <!-- Badge + close -->
        <div v-if="badge || deletable" class="absolute top-2 right-2 flex items-center gap-1 z-10">
            <GenesisButtonIcon
                v-if="deletable"
                size="xs"
                variant="tertiary"
                class="opacity-60 hover:opacity-100"
                @click.stop="$emit('close')"
            >
                <IconTrashAlt />
            </GenesisButtonIcon>
            <div v-if="badge" :class="badgeClasses">
                {{ badge }}
            </div>
        </div>

        <div class="relative w-full">
            <div
                v-for="layer in ['base', 'fill']"
                :key="layer"
                class="flex flex-col items-center gap-4 w-full"
                :class="layer === 'fill' ? 'fill-layer absolute inset-0 pointer-events-none select-none text-primary' : ''"
                :style="layer === 'fill' ? { clipPath: selected ? 'inset(0 0 0 0)' : 'inset(100% 0 0 0)' } : undefined"
                :aria-hidden="layer === 'fill' ? 'true' : undefined"
                :inert="layer === 'fill' ? true : undefined"
            >
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
            </div>
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
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconHelpCircle from '@genesis-labs/web-core/core/components/ui/icons/IconHelpCircle.vue';
import IconTrashAlt from '../../ui/icons/IconTrashAlt.vue';
import genieUrl from '../../../../assets/LOGO/Genesis/genie-normal.svg?url';

withDefaults(defineProps<{
    containerClasses: string;
    layoutClasses: string;
    badgeClasses: string;
    logoClasses: string;
    textClasses: string;
    labelClasses: string;
    sublabelClasses: string;
    complementaryClasses: string;
    infoButtonClasses: string;
    label?: string;
    sublabel?: string;
    badge: string | null;
    showInfoButton: boolean;
    showComplementary: boolean;
    showLogo: boolean;
    deletable: boolean;
    initials: string;
    hasHeaderSlot: boolean;
    hasComplementary: boolean;
    selected?: boolean;
    mascotSize?: number;
}>(), {
    selected: false,
    mascotSize: 32,
});



defineEmits<{
    click: [event: MouseEvent];
    info: [];
    close: [];
}>();
</script>
<style scoped>
.genesis-card {
    --fill-duration: 500ms;        /* montée du fond */
    --mascot-in-duration: 350ms;   /* la mascotte monte */
    --mascot-out-duration: 250ms;  /* la mascotte redescend */
}

/* ── Fond ──
   Désélection : il attend que la mascotte soit redescendue (délai).
   Sélection : il démarre tout de suite. */
.fill-layer {
    transition: clip-path var(--fill-duration) cubic-bezier(0.4, 0, 0.2, 1) var(--mascot-out-duration);
}
.is-selected .fill-layer {
    transition-delay: 0ms;
}

/* ── Mascotte ──
   Désélection : elle descend immédiatement.
   Sélection : elle attend la fin du fond avant de monter. */
.mascot {
    transform: translateY(100%);
    transition: transform var(--mascot-out-duration) ease-in 0ms;
}
.is-selected .mascot {
    transform: translateY(0);
    transition: transform var(--mascot-in-duration) cubic-bezier(0.2, 0.8, 0.2, 1) var(--fill-duration);
}

.fill-layer :deep(*) {
    color: inherit !important;
}

@media (prefers-reduced-motion: reduce) {
    .fill-layer,
    .mascot { transition: none !important; }
}
</style>