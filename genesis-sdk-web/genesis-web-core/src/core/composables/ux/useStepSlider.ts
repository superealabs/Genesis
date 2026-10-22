// core/composables/ux/useStepSlider.ts
//
// Responsabilité unique : l'état « quel slide est affiché » + le verrou anti-chevauchement.
// Aucune dépendance au DOM ni à GSAP : l'animation est injectée (`transition`).

import { computed, ref, toValue, type MaybeRefOrGetter } from 'vue';

export type SlideTransition = (from: number, to: number) => Promise<void>;

export function useStepSlider(count: MaybeRefOrGetter<number>, transition: SlideTransition) {
    const index = ref(0);
    const busy = ref(false);

    const isFirst = computed(() => index.value === 0);
    const isLast = computed(() => index.value === toValue(count) - 1);

    /** Point d'entrée unique : molette, clavier et frise passent tous par ici. */
    async function goTo(target: number) {
        if (busy.value || target === index.value || target < 0 || target >= toValue(count)) return;

        const from = index.value;
        busy.value = true;
        index.value = target;               // la frise réagit tout de suite
        try {
            await transition(from, target);
        } finally {
            busy.value = false;
        }
    }

    const next = () => goTo(index.value + 1);
    const prev = () => goTo(index.value - 1);

    return { index, busy, isFirst, isLast, goTo, next, prev };
}