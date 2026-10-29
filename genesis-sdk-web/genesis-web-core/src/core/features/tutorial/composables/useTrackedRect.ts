// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useTrackedRect.ts
//
// Responsabilité unique : suivre la position (à l'écran) d'un élément tant que `active` est vrai.
// Une lecture par image : suit le défilement de n'importe quel parent, les redimensionnements
// et les animations, sans écouteurs à brancher un par un. Au repos (`active` faux), aucun coût.

import { onBeforeUnmount, shallowRef, watch, type Ref } from 'vue';

export interface TrackedRect {
    top: number;
    left: number;
    width: number;
    height: number;
    /** Taille de la fenêtre au moment de la lecture (pour choisir de quel côté placer une popup) */
    vw: number;
    vh: number;
}

const same = (a: TrackedRect | null, b: TrackedRect | null) =>
    a === b ||
    (!!a && !!b &&
        a.top === b.top && a.left === b.left && a.width === b.width && a.height === b.height &&
        a.vw === b.vw && a.vh === b.vh);

/**
 * @returns le rectangle de l'élément, ou null s'il est absent ou invisible (taille nulle)
 */
export function useTrackedRect(getElement: () => Element | null, active: Ref<boolean>) {
    const rect = shallowRef<TrackedRect | null>(null);
    let frame = 0;

    const read = () => {
        const box = getElement()?.getBoundingClientRect();
        const next: TrackedRect | null =
            box && box.width > 0 && box.height > 0
                ? { top: box.top, left: box.left, width: box.width, height: box.height, vw: window.innerWidth, vh: window.innerHeight }
                : null;

        if (!same(rect.value, next)) rect.value = next;
        frame = requestAnimationFrame(read);
    };

    watch(active, (on) => {
        cancelAnimationFrame(frame);
        if (on) read();
        else rect.value = null;
    }, { immediate: true });

    onBeforeUnmount(() => cancelAnimationFrame(frame));

    return rect;
}