// core/composables/ux/useStepInput.ts
//
// Responsabilité unique : traduire molette + clavier en « suivant / précédent ».
// Il ne sait rien de l'animation : le verrou est dans useStepSlider (`busy`),
// ici on ajoute seulement un délai de refroidissement contre l'inertie du trackpad.

import { onBeforeUnmount, onMounted, type Ref } from 'vue';

const WHEEL_THRESHOLD = 20;   // ignore les micro-mouvements
const COOLDOWN_MS = 900;      // ignore l'inertie qui suit un changement de slide

const isTypingTarget = (t: EventTarget | null) =>
    t instanceof HTMLElement && (t.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(t.tagName));

/**
 * Un élément défilable sous le curseur (slide plus haut que l'écran) garde la priorité
 * tant qu'il n'a pas atteint son bord dans la direction demandée.
 */
function canScrollInside(start: EventTarget | null, root: HTMLElement, direction: 1 | -1): boolean {
    let el = start instanceof HTMLElement ? start : null;
    while (el && el !== root) {
        const scrollable = el.scrollHeight > el.clientHeight && /(auto|scroll)/.test(getComputedStyle(el).overflowY);
        if (scrollable) {
            const atEdge = direction > 0
                ? el.scrollTop + el.clientHeight >= el.scrollHeight - 1
                : el.scrollTop <= 0;
            if (!atEdge) return true;
        }
        el = el.parentElement;
    }
    return false;
}

export function useStepInput(
    root: Ref<HTMLElement | null>,
    actions: { next: () => void; prev: () => void }
) {
    let lastStepAt = 0;

    const step = (direction: 1 | -1) => {
        const now = performance.now();
        if (now - lastStepAt < COOLDOWN_MS) return;
        lastStepAt = now;
        direction > 0 ? actions.next() : actions.prev();
    };

    const onWheel = (e: WheelEvent) => {
        if (Math.abs(e.deltaY) < WHEEL_THRESHOLD || !root.value) return;
        const direction = e.deltaY > 0 ? 1 : -1;
        if (canScrollInside(e.target, root.value, direction)) return;
        step(direction);
    };

    const onKeydown = (e: KeyboardEvent) => {
        if (e.altKey || e.ctrlKey || e.metaKey || isTypingTarget(e.target)) return;
        if (e.key === 'ArrowDown' || e.key === 'PageDown') step(1);
        else if (e.key === 'ArrowUp' || e.key === 'PageUp') step(-1);
    };

    onMounted(() => {
        root.value?.addEventListener('wheel', onWheel, { passive: true });
        window.addEventListener('keydown', onKeydown);
    });

    onBeforeUnmount(() => {
        root.value?.removeEventListener('wheel', onWheel);
        window.removeEventListener('keydown', onKeydown);
    });
}