<template>
    <!-- ✅ CAS 1 : Aucun ID fourni → Rendu transparent (juste le slot) -->
    <slot v-if="!id" />

    <!-- ✅ CAS 2 : Un ID est fourni → On active le wrapper et la logique de tutoriel -->
    <template v-else>
        <div ref="wrapper" class="contents">
            <slot />
        </div>

        <Teleport to="body">
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
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import BasePopup from '@genesis-labs/web-core/core/components/layouts/Popup/BasePopup.vue';
import GenesisButton from '@genesis-labs/web-core/core/components/ui/actions/GenesisButton.vue';
import { useTutorialContext, useTutorialSwitch } from '@genesis-labs/web-core/core/features/tutorial/composables/useGenesisTutorial';
import { useTrackedRect } from '@genesis-labs/web-core/core/features/tutorial/composables/useTrackedRect';
import gsap from 'gsap';

const props = defineProps<{
    /** Identifiant de l'étape. Optionnel : si absent, le composant agit comme un simple conteneur transparent. */
    id?: string;
}>();

defineOptions({ inheritAttrs: false });

// ── Constantes d'affichage ──
const PAD = 4;
const GAP = 8;
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


// ✅ AJOUTE CE LOG ICI (avant le computed)
console.log(`👁️ [GenesisTutorialItem] Évaluation pour ID: "${props.id}" | Contexte injecté:`, context ? `screenId='${context.screenId}'` : "NULL");

const step = computed(() => {
    if (!props.id || !context?.current.value) {
        console.log(`  ↳ [${props.id}] Rejeté: pas d'ID ou current est null`);
        return null;
    }
    
    const isMatch = context.current.value.id === props.id;
    console.log(`  ↳ [${props.id}] Match? ${isMatch} | Current ID du contexte: "${context.current.value.id}"`);
    
    return isMatch ? context.current.value : null;
});

// ── Position de la cible ──
const wrapper = ref<HTMLElement | null>(null);
const target = () => wrapper.value?.firstElementChild ?? null;

// ✅ On ne track la rect que si un ID est fourni et que l'étape est active
const rect = useTrackedRect(target, computed(() => !!props.id && !!step.value));

const frame = computed(() => {
    const r = rect.value;
    return r && { top: r.top - PAD, left: r.left - PAD, width: r.width + PAD * 2, height: r.height + PAD * 2, vw: r.vw };
});

const frameStyle = computed(() => {
    const f = frame.value;
    return f ? { top: `${f.top}px`, left: `${f.left}px`, width: `${f.width}px`, height: `${f.height}px` } : {};
});

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
        { opacity: 0, y: -15 },
        { 
            opacity: 1, 
            y: 0, 
            duration: 0.4, 
            ease: "power2.out",
            onComplete: done
        }
    );
};

const onLeave = (el: Element, done: () => void) => {
    gsap.to(el, {
        opacity: 0,
        y: 15,
        duration: 0.3,
        ease: "power2.in",
        onComplete: done
    });
};

const anchor = {
    getBoundingClientRect: () => {
        const f = frame.value ?? { top: 0, left: 0, width: 0, height: 0 };
        return { top: f.top, left: f.left, right: f.left + f.width, bottom: f.top + f.height, width: f.width, height: f.height };
    }
} as unknown as HTMLElement;

watch(() => !!props.id && !!step.value && !!rect.value, (visible) => {
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