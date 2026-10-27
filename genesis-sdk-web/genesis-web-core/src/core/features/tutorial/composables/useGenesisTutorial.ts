import {
    computed, inject, onScopeDispose, provide, reactive, ref, shallowReactive,
    shallowRef, toValue, watch, markRaw,
    type ComputedRef, type InjectionKey, type MaybeRefOrGetter
} from 'vue';

export interface TutorialStep { id: string; text: string; }
export interface TutorialSavedState { enabled: boolean; completed: string[]; }
export interface TutorialPersistence {
    load: () => TutorialSavedState | null;
    save: (state: TutorialSavedState) => void;
}

export interface TutorialContext {
    screenId: string;
    steps: ComputedRef<TutorialStep[]>;
    current: ComputedRef<TutorialStep | null>;
    complete: (stepId: string) => void;
    attach: (stepId: string) => () => void;
    registerGroup: (afterId: string, steps: TutorialStep[]) => () => void;
}

// ── État global ──
const enabled = ref(false);
const completed = reactive(new Set<string>());
const activeScreenId = ref<string | null>(null);

const keyOf = (screenId: string, stepId: string) => `${screenId}:${stepId}`;

interface ScreenHandle { screenId: string; restart: () => void }
const screens = shallowReactive<ScreenHandle[]>([]);

let persistence: TutorialPersistence | null = null;

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

export function useTutorialSwitch() {
    return {
        enabled: computed(() => enabled.value),
        available: computed(() => screens.length > 0),
        enable: () => { enabled.value = true; },
        disable: () => { 
            enabled.value = false; 
            activeScreenId.value = null;
        },
        toggle: () => { enabled.value = !enabled.value; },
        reset: () => { 
            completed.clear(); 
            enabled.value = true; 
        },
        restartActive: () => screens[screens.length - 1]?.restart()
    };
}

// ── Écran ──
const TUTORIAL_KEY: InjectionKey<TutorialContext> = Symbol('genesis-tutorial');

export function useGenesisTutorial(screenId: string, steps: MaybeRefOrGetter<TutorialStep[]>) {
    const groups = shallowRef<{ key: symbol; afterId: string; steps: TutorialStep[] }[]>([]);
    const present = reactive(new Set<string>());

    const list = computed(() =>
        toValue(steps).flatMap(step => [
            step,
            ...groups.value.filter(g => g.afterId === step.id).flatMap(g => g.steps)
        ])
    );

    const registerGroup = (afterId: string, groupSteps: TutorialStep[]) => {
        const key = Symbol();
        // On marque l'objet comme brut. Vue ne créera pas de Proxy dessus.
        // Cela empêche le système de réactivité de s'emballer quand on ajoute le groupe.
        const newGroup = markRaw({ key, afterId, steps: groupSteps });
        
        // On recrée le tableau avec le nouvel objet brut
        groups.value = [...groups.value, newGroup];
        
        return () => { 
            groups.value = groups.value.filter(g => g.key !== key); 
        };
    };

    // ✅ NOUVEAU : Auto-compléter les étapes qui ne sont pas dans le DOM.
    // Cela empêche une étape cachée (ex: dans un Disclosure fermé) de devenir 
    // soudainement "current" et de geler l'UI (scrollIntoView) lorsqu'elle est enfin montée.
    watch(
        () => list.value.map(s => s.id),
        (stepIds) => {
            if (!enabled.value || activeScreenId.value !== screenId) return;

            stepIds.forEach(stepId => {
                const fullId = keyOf(screenId, stepId);
                if (!completed.has(fullId) && !present.has(stepId)) {
                    completed.add(fullId);
                }
            });
        },
        { immediate: true }
    );

    const current = computed(() => {
        if (!enabled.value || activeScreenId.value !== screenId) return null;
        
        return list.value.find(s =>
            !completed.has(keyOf(screenId, s.id)) && present.has(s.id)
        ) ?? null;
    });

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

    const restart = () => {
        const prefix = `${screenId}:`;
        [...completed].filter(k => k.startsWith(prefix)).forEach(k => completed.delete(k));
        
        enabled.value = true;
        activeScreenId.value = screenId;
    };

    const handle: ScreenHandle = { screenId, restart };
    screens.push(handle);
    
    onScopeDispose(() => {
        const i = screens.indexOf(handle);
        if (i >= 0) screens.splice(i, 1);
        
        if (activeScreenId.value === screenId) {
            activeScreenId.value = null;
            enabled.value = false;
        }
    });

    return { ...context, restart };
}

export const useTutorialContext = () => inject(TUTORIAL_KEY, null);

export function useTutorialGroup(options: {
    anchorId: MaybeRefOrGetter<string | undefined>;
    include: MaybeRefOrGetter<boolean | undefined>;
    steps: { key: string; text: string }[];
}) {
    const ctx = useTutorialContext();
    const anchor = computed(() =>
        ctx && toValue(options.include) ? toValue(options.anchorId) ?? null : null
    );

    const sub = (key: string) => anchor.value ? `${anchor.value}/${key}` : undefined;

    watch(anchor, (anchorId, _old, onCleanup) => {
        if (!anchorId) return;
        onCleanup(ctx!.registerGroup(
            anchorId,
            options.steps.map(s => ({ id: `${anchorId}/${s.key}`, text: s.text }))
        ));
    }, { 
        immediate: true
    });

    return { sub, active: computed(() => !!anchor.value) };
}