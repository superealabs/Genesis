// genesis-sdk-web/genesis-web-core/src/core/composables/ux/useGenesisTutorial.ts
//
// Responsabilité unique : l'état du tutoriel (quelle étape est courante ? est-il actif ?).
// Aucun DOM, aucun rendu : voir GenesisTutorialItem.
//
//  - Un écran déclare sa liste ordonnée d'étapes avec useGenesisTutorial(screenId, steps).
//  - Les GenesisTutorialItem de cet écran (ses descendants) lisent l'étape courante par injection.
//  - L'interrupteur et la progression sont globaux : un seul état pour tous les écrans.

import {
    computed,
    inject,
    provide,
    reactive,
    ref,
    toValue,
    watch,
    type ComputedRef,
    type InjectionKey,
    type MaybeRefOrGetter
} from 'vue';

// ============================================================================
// TYPES
// ============================================================================

export interface TutorialStep {
    /** Identifiant de l'élément (celui du GenesisTutorialItem correspondant) */
    id: string;
    /** Texte affiché dans la popup */
    text: string;
}

export interface TutorialSavedState {
    enabled: boolean;
    /** Clés « écran:étape » des étapes validées */
    completed: string[];
}

/** Branchement optionnel sur un stockage (service VS Code, localStorage…) */
export interface TutorialPersistence {
    load: () => TutorialSavedState | null;
    save: (state: TutorialSavedState) => void;
}

export interface TutorialContext {
    screenId: string;
    steps: ComputedRef<TutorialStep[]>;
    /** Première étape non validée de la liste (null si tout est fait ou si le tutoriel est désactivé) */
    current: ComputedRef<TutorialStep | null>;
    complete: (stepId: string) => void;
}

// ============================================================================
// ÉTAT GLOBAL (partagé par tous les écrans)
// ============================================================================

const enabled = ref(true);
const completed = reactive(new Set<string>());

const keyOf = (screenId: string, stepId: string) => `${screenId}:${stepId}`;

let persistence: TutorialPersistence | null = null;

/**
 * À appeler une fois au démarrage pour mémoriser l'état entre deux ouvertures.
 * Sans appel, tout vit en mémoire et repart de zéro à chaque lancement.
 */
export function configureTutorialPersistence(adapter: TutorialPersistence) {
    persistence = adapter;
    const saved = adapter.load();
    if (!saved) return;
    enabled.value = saved.enabled;
    completed.clear();
    saved.completed.forEach(key => completed.add(key));
}

watch([enabled, () => [...completed]], () => {
    persistence?.save({ enabled: enabled.value, completed: [...completed] });
});

/** Interrupteur global : désactive / réactive le tutoriel de tous les écrans à la fois */
export function useTutorialSwitch() {
    return {
        enabled: computed(() => enabled.value),
        enable: () => { enabled.value = true; },
        disable: () => { enabled.value = false; },
        toggle: () => { enabled.value = !enabled.value; },
        /** Oublie toute la progression et réactive le tutoriel */
        reset: () => { completed.clear(); enabled.value = true; }
    };
}

// ============================================================================
// ÉCRAN
// ============================================================================

const TUTORIAL_KEY: InjectionKey<TutorialContext> = Symbol('genesis-tutorial');

/**
 * Déclare le tutoriel d'un écran. L'ordre de `steps` est l'ordre d'affichage.
 * À appeler dans le composant de l'écran : ses descendants (GenesisTutorialItem) y accèdent.
 */
export function useGenesisTutorial(screenId: string, steps: MaybeRefOrGetter<TutorialStep[]>) {
    const list = computed(() => toValue(steps));

    const current = computed(() => {
        if (!enabled.value) return null;
        return list.value.find(step => !completed.has(keyOf(screenId, step.id))) ?? null;
    });

    const context: TutorialContext = {
        screenId,
        steps: list,
        current,
        complete: (stepId) => { completed.add(keyOf(screenId, stepId)); }
    };

    provide(TUTORIAL_KEY, context);

    return {
        ...context,
        /** Rejoue le tutoriel de CET écran uniquement */
        restart: () => list.value.forEach(step => completed.delete(keyOf(screenId, step.id)))
    };
}

/** Pour GenesisTutorialItem : null hors d'un écran qui a déclaré un tutoriel */
export const useTutorialContext = () => inject(TUTORIAL_KEY, null);