// genesis-sdk-web/genesis-web-core/src/core/features/tutorial/composables/useGenesisTutorial.ts
//
// Responsabilité unique : l'état du tutoriel (quelle étape est courante ? est-il actif ?).
// Aucun DOM, aucun rendu : voir GenesisTutorialItem.
//
//  - Un écran déclare ses SECTIONS (chacune = une liste ordonnée d'étapes) avec useGenesisTutorial(screenId, sections).
//  - Chaque section se lance indépendamment : tutorial.restart('nom-de-section').
//  - Les GenesisTutorialItem de l'écran (ses descendants) lisent l'étape courante par injection.
//  - Les composants enfants réutilisables insèrent leurs sous-étapes via useTutorialGroup.

import {
    computed,
    inject,
    provide,
    onScopeDispose,
    reactive,
    ref,
    shallowReactive,
    shallowRef,
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

/** Nom de section → liste ordonnée d'étapes */
export type TutorialSections = Record<string, TutorialStep[]>;

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
    /** Étapes de la section active (sous-étapes incluses) */
    steps: ComputedRef<TutorialStep[]>;
    /** Première étape non validée ET montée dans le DOM (null si rien à afficher) */
    current: ComputedRef<TutorialStep | null>;
    complete: (stepId: string) => void;
    /** Un GenesisTutorialItem monté déclare sa présence. Retourne la fonction de détachement. */
    attach: (stepId: string) => () => void;
    /** Un composant enfant insère un groupe d'étapes juste après `afterId`. Retourne la fonction de retrait. */
    registerGroup: (afterId: string, steps: TutorialStep[]) => () => void;
}

// ============================================================================
// ÉTAT GLOBAL (partagé par tous les écrans)
// ============================================================================

const enabled = ref(false);
const completed = reactive(new Set<string>());
const activeScreenId = ref<string | null>(null);
const activeSection = ref<string | null>(null);

const keyOf = (screenId: string, stepId: string) => `${screenId}:${stepId}`;

// Écrans montés (sert uniquement à savoir si au moins un tutoriel est disponible)
const screens = shallowReactive<{ screenId: string }[]>([]);

let persistence: TutorialPersistence | null = null;

/**
 * À appeler une fois au démarrage pour mémoriser la progression entre deux ouvertures.
 * On ne restaure PAS `enabled` : le tutoriel reste éteint jusqu'au clic sur un bouton « ? ».
 */
export function configureTutorialPersistence(adapter: TutorialPersistence) {
    persistence = adapter;
    const saved = adapter.load();
    if (!saved) return;
    completed.clear();
    saved.completed.forEach(key => completed.add(key));
}

watch([enabled, () => [...completed]], () => {
    persistence?.save({ enabled: enabled.value, completed: [...completed] });
});

/** Interrupteur global */
export function useTutorialSwitch() {
    return {
        enabled: computed(() => enabled.value),
        /** true si au moins un écran avec tutoriel est monté */
        available: computed(() => screens.length > 0),
        enable: () => { enabled.value = true; },
        disable: () => {
            enabled.value = false;
            activeScreenId.value = null;
            activeSection.value = null;
        },
        toggle: () => { enabled.value = !enabled.value; },
        /** Oublie toute la progression (le tutoriel reste éteint tant qu'une section n'est pas relancée) */
        reset: () => { completed.clear(); }
    };
}

// ============================================================================
// ÉCRAN
// ============================================================================

const TUTORIAL_KEY: InjectionKey<TutorialContext> = Symbol('genesis-tutorial');

/**
 * Déclare les sections de tutoriel d'un écran. L'ordre de chaque tableau est l'ordre d'affichage.
 * À appeler dans le composant de l'écran : ses descendants (GenesisTutorialItem) y accèdent.
 *
 * @example
 * const tutorial = useGenesisTutorial('project-config', {
 *   'tech-stack': [{ id: 'tutorial-tech-stack-config', text: '...' }],
 *   'advanced':   [{ id: 'tutorial-advanced-config',   text: '...' }]
 * });
 * tutorial.restart('tech-stack');
 */
export function useGenesisTutorial(screenId: string, sections: MaybeRefOrGetter<TutorialSections>) {
    const groups = shallowRef<{ key: symbol; afterId: string; steps: TutorialStep[] }[]>([]);
    const present = reactive(new Set<string>());

    // Liste d'une section, avec les groupes (sous-étapes) insérés après leur ancre
    const listOf = (name: string): TutorialStep[] =>
        (toValue(sections)[name] ?? []).flatMap(step => [
            step,
            ...groups.value.filter(g => g.afterId === step.id).flatMap(g => g.steps)
        ]);

    // Liste de la section actuellement lancée sur CET écran
    const list = computed<TutorialStep[]>(() =>
        activeScreenId.value === screenId && activeSection.value
            ? listOf(activeSection.value)
            : []
    );

    const current = computed<TutorialStep | null>(() => {
        if (!enabled.value || activeScreenId.value !== screenId) return null;
        // Une étape dont l'item n'est pas monté est simplement ignorée (elle ne bloque pas la suite)
        return list.value.find(s =>
            !completed.has(keyOf(screenId, s.id)) && present.has(s.id)
        ) ?? null;
    });

    const registerGroup = (afterId: string, groupSteps: TutorialStep[]) => {
        const key = Symbol();
        groups.value = [...groups.value, { key, afterId, steps: groupSteps }];
        return () => {
            groups.value = groups.value.filter(g => g.key !== key);
        };
    };

    const context: TutorialContext = {
        screenId,
        steps: list,
        current,
        complete: (stepId) => { completed.add(keyOf(screenId, stepId)); },
        attach: (stepId) => {
            present.add(stepId);
            return () => { present.delete(stepId); };
        },
        registerGroup
    };

    provide(TUTORIAL_KEY, context);

    /** Lance (ou relance) UNE section : ne réinitialise que les étapes de cette section */
    const restart = (name: string) => {
        if (!(name in toValue(sections))) {
            console.warn(`[tutorial] Section inconnue "${name}" sur l'écran "${screenId}"`);
            return;
        }
        listOf(name).forEach(s => completed.delete(keyOf(screenId, s.id)));
        activeScreenId.value = screenId;
        activeSection.value = name;
        enabled.value = true;
    };

    const handle = { screenId };
    screens.push(handle);

    onScopeDispose(() => {
        const i = screens.indexOf(handle);
        if (i >= 0) screens.splice(i, 1);

        if (activeScreenId.value === screenId) {
            activeScreenId.value = null;
            activeSection.value = null;
            enabled.value = false;
        }
    });

    return { ...context, restart };
}

/** Pour GenesisTutorialItem : null hors d'un écran qui a déclaré un tutoriel */
export const useTutorialContext = () => inject(TUTORIAL_KEY, null);

// ============================================================================
// COMPOSANT ENFANT RÉUTILISABLE
// ============================================================================

/**
 * Permet à un composant réutilisable (ex. GenesisConfigurationPanel) d'insérer ses propres
 * sous-étapes dans la section de l'écran parent, juste après son étape d'ancre.
 * Les ids sont préfixés par `anchorId` pour éviter les collisions entre instances.
 */
export function useTutorialGroup(options: {
    anchorId: MaybeRefOrGetter<string | undefined>;
    include: MaybeRefOrGetter<boolean | undefined>;
    steps: { key: string; text: string }[];
}) {
    const ctx = useTutorialContext();

    const anchor = computed(() =>
        ctx && toValue(options.include) ? toValue(options.anchorId) ?? null : null
    );

    /** id namespacé, ou undefined (→ GenesisTutorialItem transparent) si le sous-tutoriel est inactif */
    const sub = (key: string) => anchor.value ? `${anchor.value}/${key}` : undefined;

    // `watch` (et non `watchEffect`) : le callback n'est pas tracké, donc pas de boucle infinie
    watch(anchor, (anchorId, _old, onCleanup) => {
        if (!anchorId) return;
        onCleanup(ctx!.registerGroup(
            anchorId,
            options.steps.map(s => ({ id: `${anchorId}/${s.key}`, text: s.text }))
        ));
    }, { immediate: true });

    return { sub, active: computed(() => !!anchor.value) };
}