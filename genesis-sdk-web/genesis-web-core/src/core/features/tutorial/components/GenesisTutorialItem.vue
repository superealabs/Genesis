<template>
    <div ref="wrapper" class="contents">
        <slot />
    </div>

    <Teleport to="body">
        <!-- ✅ Utilisation des hooks GSAP pour l'animation -->
        <Transition 
            name="tutorial-motion" 
            mode="out-in"
            @enter="onEnter"
            @leave="onLeave"
        >
            <div v-if="step && frame" class="tutorial-transition-root fixed inset-0 pointer-events-none">
                
                <!-- Quatre coins autour de la cible -->
                <div
                    class="absolute pointer-events-auto"
                    :style="{ ...frameStyle, zIndex: Z_FRAME }"
                    aria-hidden="true"
                >
                    <span v-for="corner in CORNERS" :key="corner" class="absolute w-3.5 h-3.5 border-primary" :class="corner" />
                </div>

                <!-- Conteneur pour la popup -->
                <div class="absolute inset-0 pointer-events-auto" :style="{ zIndex: Z_POPUP }">
                    <BasePopup
                        :show="true"
                        :anchor="anchor"
                        :position="side"
                        :offset="GAP"
                        :close-on-outside-click="false"
                        :close-on-escape="false"
                        :z-index="0" 
                    >
                        <div class="flex flex-col gap-3 p-4" :style="{ width: `${POPUP_WIDTH}px` }">
                            <p class="text-sm leading-relaxed text-text">{{ step.text }}</p>

                            <div class="flex items-center justify-between gap-2">
                                <GenesisButton variant="accent" size="sm" @click="context?.complete(id)">
                                    OK
                                </GenesisButton>
                                <GenesisButton :variant="'neutral'" size="sm" @click="tutorialSwitch.disable()">
                                    Tout passer
                                </GenesisButton>
                            </div>
                        </div>
                    </BasePopup>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import BasePopup from '@genesis-labs/web-core/core/components/layouts/Popup/BasePopup.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import { useTutorialContext, useTutorialSwitch } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import { useTrackedRect } from '@genesis-labs/web-core/core/features/tutorial/composables/useTrackedRect';
import gsap from 'gsap';

const props = defineProps<{
    /** Identifiant de l'étape, présent dans la liste donnée à useGenesisTutorial */
    id: string;
}>();

// Fragment (conteneur + Teleport) : on ne laisse pas les attributs retomber sur un des deux
defineOptions({ inheritAttrs: false });

// ── Constantes d'affichage ──
const PAD = 4;              // marge entre la cible et les coins
const GAP = 8;              // marge entre les coins et la popup
const POPUP_WIDTH = 288;
const Z_FRAME = 999;
const Z_POPUP = 1000;
const CORNERS = [
    'top-0 left-0 border-t-2 border-l-2 rounded-tl',
    'top-0 right-0 border-t-2 border-r-2 rounded-tr',
    'bottom-0 left-0 border-b-2 border-l-2 rounded-bl',
    'bottom-0 right-0 border-b-2 border-r-2 rounded-br'
];

// ── Étape ──
const context = useTutorialContext();
const tutorialSwitch = useTutorialSwitch();

if (import.meta.env.DEV) {
    if (!context) console.warn(`[GenesisTutorialItem] « ${props.id} » est hors d'un écran qui appelle useGenesisTutorial().`);
    else if (!context.steps.value.some(s => s.id === props.id)) console.warn(`[GenesisTutorialItem] « ${props.id} » n'est pas dans la liste de l'écran « ${context.screenId} ».`);
}

/** Cette étape, seulement si c'est son tour (ordre strict de la liste) */
const step = computed(() => (context?.current.value?.id === props.id ? context.current.value : null));

// ── Position de la cible ──
const wrapper = ref<HTMLElement | null>(null);
const target = () => wrapper.value?.firstElementChild ?? null;

// Cible absente ou repliée : rect = null, l'étape attend sans en sauter aucune
const rect = useTrackedRect(target, computed(() => !!step.value));

const frame = computed(() => {
    const r = rect.value;
    return r && { top: r.top - PAD, left: r.left - PAD, width: r.width + PAD * 2, height: r.height + PAD * 2, vw: r.vw };
});

const frameStyle = computed(() => {
    const f = frame.value;
    return f ? { top: `${f.top}px`, left: `${f.left}px`, width: `${f.width}px`, height: `${f.height}px` } : {};
});

// Popup à droite si la place suffit, sinon à gauche, sinon sous la cible
const side = computed<'right' | 'left' | 'bottom'>(() => {
    const f = frame.value;
    if (!f) return 'right';
    const needed = POPUP_WIDTH + GAP;
    if (f.vw - (f.left + f.width) >= needed) return 'right';
    if (f.left >= needed) return 'left';
    return 'bottom';
});


const onEnter = (el: Element, done: () => void) => {
    gsap.fromTo(
        el,
        { opacity: 0, y: -30 }, // Départ : invisible et 15px plus haut
        { 
            opacity: 1, 
            y: 0, 
            duration: 0.6, 
            ease: "power2.out",
            onComplete: done // Signale à Vue que l'animation est terminée
        }
    );
};

/** Animation de sortie : disparaît en glissant vers le bas */
const onLeave = (el: Element, done: () => void) => {
    gsap.to(el, {
        opacity: 0,
        y: 30, // Arrivée : 15px plus bas
        duration: 0.4,
        ease: "power2.in",
        onComplete: done
    });
};

/**
 * Ancre de BasePopup : il calcule sa position en appelant anchor.getBoundingClientRect().
 * Cet objet lit `frame` (réactif), donc la popup suit la cible sans être recréée.
 */
const anchor = {
    getBoundingClientRect: () => {
        const f = frame.value ?? { top: 0, left: 0, width: 0, height: 0 };
        return { top: f.top, left: f.left, right: f.left + f.width, bottom: f.top + f.height, width: f.width, height: f.height };
    }
} as unknown as HTMLElement;

// Ramène la cible dans la vue quand son étape devient visible (une fois par étape)
watch(() => !!step.value && !!rect.value, (visible) => {
    if (visible) target()?.scrollIntoView({ block: 'nearest', inline: 'nearest' });
});
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}
.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
    background: var(--color-neutral-light);
    border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: var(--color-neutral);
}
</style>