// core/composables/ux/useSlideTransition.ts
//
// SEUL fichier du projet qui importe GSAP.
// Responsabilité unique : animer le passage d'un slide à un autre.
// Il ne connaît ni l'index courant, ni la molette, ni le contenu des slides.

import { gsap } from 'gsap';
import { onBeforeUnmount, type Ref } from 'vue';

const DURATION = 0.6;   // secondes
const TRAVEL = 30;      // % de la hauteur parcouru par chaque slide

const prefersReducedMotion = () =>
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/**
 * @param container Élément dont les ENFANTS DIRECTS sont les slides (empilés en `absolute inset-0`).
 */
export function useSlideTransition(container: Ref<HTMLElement | null>) {
    let timeline: gsap.core.Timeline | null = null;

    const slide = (index: number) => container.value!.children[index] as HTMLElement;

    /** Affiche un slide sans animation (état initial). */
    function show(index: number) {
        gsap.set(slide(index), { autoAlpha: 1, yPercent: 0 });
    }

    /** Sortie du slide `from` et entrée du slide `to`, simultanées. Résolue à la fin de l'animation. */
    function play(from: number, to: number): Promise<void> {
        const direction = to > from ? 1 : -1;   // 1 = on descend, -1 = on remonte
        const duration = prefersReducedMotion() ? 0 : DURATION;

        return new Promise<void>((resolve) => {
            timeline = gsap
                .timeline({ defaults: { duration, ease: 'power3.inOut' }, onComplete: resolve })
                .to(slide(from), { yPercent: -direction * TRAVEL, autoAlpha: 0 }, 0)
                .fromTo(
                    slide(to),
                    { yPercent: direction * TRAVEL, autoAlpha: 0 },
                    { yPercent: 0, autoAlpha: 1 },
                    0
                );
        });
    }

    onBeforeUnmount(() => timeline?.kill());

    return { show, play };
}