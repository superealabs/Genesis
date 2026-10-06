import {
    ref,
    computed,
    watch,
    inject,
    onScopeDispose,
    type Component,
    type ComputedRef,
    type InjectionKey,
    type Ref
} from 'vue';

// ============================================================================
// TYPES
// ============================================================================

/** column = deux colonnes côte à côte (ligne de division verticale) ; row = deux lignes empilées */
export type PanelDirection = 'column' | 'row';
export type PanelEdge = 'left' | 'right' | 'top' | 'bottom';
export type PanelAxis = 'x' | 'y';

/**
 * « Point de vue » d'un panneau : propre à CE panneau (alors que l'état métier,
 * lui, est partagé par tous les panneaux du même type).
 * - scrollTop / scrollLeft : capturés automatiquement par GenesisPanelElement
 * - extra : libre, pour les éditeurs qui ont leur propre zone de défilement ou un curseur
 *   (voir usePanelView). Doit rester sérialisable en JSON.
 */
export interface PanelView {
    scrollTop: number;
    scrollLeft: number;
    extra: Record<string, unknown>;
}
export type PanelViewPatch = Partial<Omit<PanelView, 'extra'>> & { extra?: Record<string, unknown> };

export interface PanelLeaf {
    id: string;
    kind: 'leaf';
    /** Identifiant d'un PanelEditorDefinition */
    editor: string;
    view: PanelView;
}

export interface PanelSplit {
    id: string;
    kind: 'split';
    direction: PanelDirection;
    /** Part du premier enfant, entre 0 et 1 */
    ratio: number;
    first: PanelNode;
    second: PanelNode;
}

export type PanelNode = PanelLeaf | PanelSplit;

/** Un type d'éditeur proposé dans le menu déroulant de chaque panneau */
export interface PanelEditorDefinition {
    id: string;
    label: string;
    icon?: Component;
    component: Component;
    /** Props passées au composant. L'état métier doit vivre dans un store/composable partagé, pas ici. */
    props?: Record<string, unknown>;
}

/** Rectangle en pourcentage du conteneur */
export interface PanelRect { x: number; y: number; w: number; h: number; }

export interface PanelLeafLayout { id: string; node: PanelLeaf; rect: PanelRect; }

export interface PanelDividerLayout {
    /** Identifiant du nœud de division */
    id: string;
    direction: PanelDirection;
    /** Position de la ligne (en %) sur l'axe de la division */
    line: number;
    /** Rectangle occupé par le nœud de division entier (en %) */
    rect: PanelRect;
}

export type SplitSnap = 'center' | 'min-start' | 'min-end' | null;

export interface SplitResolution {
    /** false si le panneau est trop petit pour être divisé */
    valid: boolean;
    /** Position de la ligne en px depuis le début de l'axe du panneau */
    position: number;
    snap: SplitSnap;
}

// ============================================================================
// CONSTANTES & UTILITAIRES PURS
// ============================================================================

export const EDGE_AXIS: Record<PanelEdge, PanelAxis> = { left: 'x', right: 'x', top: 'y', bottom: 'y' };
export const EDGE_DIRECTION: Record<PanelEdge, PanelDirection> = { left: 'column', right: 'column', top: 'row', bottom: 'row' };

/** Distance (px) sous laquelle la ligne de division s'aimante */
export const SNAP_DISTANCE = 20;

const clamp = (v: number, min: number, max: number) => Math.min(max, Math.max(min, v));

/**
 * Résout la position réelle de la ligne de division à partir de la position brute du pointeur.
 * - La ligne reste libre, sauf aux limites : elle ne peut pas créer un panneau plus petit que `minSize`.
 * - Aimantage (sous SNAP_DISTANCE) : au centre (50/50) et aux tailles minimales de chaque côté.
 */
export function resolveSplitPosition(
    size: number,
    raw: number,
    minSize: number,
    snapDistance: number = SNAP_DISTANCE
): SplitResolution {
    if (size < minSize * 2) return { valid: false, position: size / 2, snap: null };

    const lo = minSize;
    const hi = size - minSize;
    let position = clamp(raw, lo, hi);

    const targets: { at: number; snap: Exclude<SplitSnap, null> }[] = [
        { at: lo, snap: 'min-start' },
        { at: size / 2, snap: 'center' },
        { at: hi, snap: 'min-end' }
    ];

    let snap: SplitSnap = null;
    let best = snapDistance;
    for (const t of targets) {
        const d = Math.abs(position - t.at);
        if (d < best) { best = d; snap = t.snap; position = t.at; }
    }
    return { valid: true, position, snap };
}

let seq = 0;
const uid = () => `panel-${Date.now().toString(36)}-${(++seq).toString(36)}-${Math.random().toString(36).slice(2, 6)}`;

const createView = (): PanelView => ({ scrollTop: 0, scrollLeft: 0, extra: {} });
const cloneView = (v: PanelView): PanelView => ({
    scrollTop: v.scrollTop,
    scrollLeft: v.scrollLeft,
    extra: JSON.parse(JSON.stringify(v.extra))
});
const createLeaf = (editor: string): PanelLeaf => ({ id: uid(), kind: 'leaf', editor, view: createView() });

function applyViewPatch(view: PanelView, patch: PanelViewPatch) {
    if (patch.scrollTop !== undefined) view.scrollTop = patch.scrollTop;
    if (patch.scrollLeft !== undefined) view.scrollLeft = patch.scrollLeft;
    if (patch.extra) Object.assign(view.extra, patch.extra);
}

/** Valide et normalise un arbre venant de l'extérieur (sauvegarde, configuration). null si invalide. */
function sanitize(raw: any): PanelNode | null {
    if (!raw || typeof raw !== 'object') return null;

    if (raw.kind === 'leaf') {
        if (typeof raw.editor !== 'string') return null;
        const v = raw.view ?? {};
        return {
            id: typeof raw.id === 'string' ? raw.id : uid(),
            kind: 'leaf',
            editor: raw.editor,
            view: {
                scrollTop: Number(v.scrollTop) || 0,
                scrollLeft: Number(v.scrollLeft) || 0,
                extra: v.extra && typeof v.extra === 'object' ? v.extra : {}
            }
        };
    }

    if (raw.kind === 'split') {
        if (raw.direction !== 'column' && raw.direction !== 'row') return null;
        const first = sanitize(raw.first);
        const second = sanitize(raw.second);
        if (!first || !second) return null;
        const ratio = Number(raw.ratio);
        return {
            id: typeof raw.id === 'string' ? raw.id : uid(),
            kind: 'split',
            direction: raw.direction,
            ratio: Number.isFinite(ratio) ? clamp(ratio, 0.05, 0.95) : 0.5,
            first,
            second
        };
    }
    return null;
}

// ============================================================================
// COMPOSABLE
// ============================================================================

export interface UseGenesisPanelOptions {
    /** Éditeur du panneau unique créé quand il n'y a ni sauvegarde ni initialLayout */
    defaultEditor: string;
    /** Disposition de départ (arbre) */
    initialLayout?: PanelNode;
    /** Taille minimale d'un panneau, en px (défaut 240 x 120) */
    minWidth?: number;
    minHeight?: number;
    /**
     * Sauvegarde optionnelle de la disposition (webview VS Code, localStorage, store…).
     * `load` est lu une fois au démarrage ; `save` est appelé (avec délai) à chaque changement.
     */
    persist?: {
        load: () => unknown | null;
        save: (layout: PanelNode) => void;
    };
}

/**
 * Gère l'arbre de panneaux (à la Blender) : chaque feuille est un panneau, chaque nœud une division.
 * Ne contient aucun rendu : voir GenesisPanelContainer et GenesisPanelElement.
 */
export function useGenesisPanel(options: UseGenesisPanelOptions) {
    const minWidth = options.minWidth ?? 240;
    const minHeight = options.minHeight ?? 120;

    const initial =
        sanitize(options.persist?.load()) ??
        sanitize(options.initialLayout) ??
        createLeaf(options.defaultEditor);

    const root = ref(initial) as Ref<PanelNode>;

    // ── Recherche ──────────────────────────────────────────────────────────
    function find(node: PanelNode, id: string): PanelNode | null {
        if (node.id === id) return node;
        if (node.kind === 'split') return find(node.first, id) ?? find(node.second, id);
        return null;
    }

    function findLeaf(id: string): PanelLeaf | null {
        const n = find(root.value, id);
        return n && n.kind === 'leaf' ? n : null;
    }

    function findSplit(id: string): PanelSplit | null {
        const n = find(root.value, id);
        return n && n.kind === 'split' ? n : null;
    }

    function findParent(id: string, node: PanelNode = root.value): PanelSplit | null {
        if (node.kind === 'leaf') return null;
        if (node.first.id === id || node.second.id === id) return node;
        return findParent(id, node.first) ?? findParent(id, node.second);
    }

    function replaceNode(target: PanelNode, replacement: PanelNode) {
        if (root.value.id === target.id) {
            root.value = replacement;
            return;
        }
        const parent = findParent(target.id);
        if (!parent) return;
        if (parent.first.id === target.id) parent.first = replacement;
        else parent.second = replacement;
    }

    // ── Tailles minimales ──────────────────────────────────────────────────

    /** Taille minimale d'un panneau seul sur l'axe donné */
    const minLeafSize = (axis: PanelAxis) => (axis === 'x' ? minWidth : minHeight);

    /** Taille minimale d'un sous-arbre sur l'axe donné (somme sur l'axe de la division, max sinon) */
    function minSize(node: PanelNode, axis: PanelAxis): number {
        if (node.kind === 'leaf') return minLeafSize(axis);
        const a = minSize(node.first, axis);
        const b = minSize(node.second, axis);
        const sameAxis = (node.direction === 'column' && axis === 'x') || (node.direction === 'row' && axis === 'y');
        return sameAxis ? a + b : Math.max(a, b);
    }

    // ── Actions ────────────────────────────────────────────────────────────

    /**
     * Divise un panneau. Le nouveau panneau est un double de l'original (même éditeur,
     * même point de vue) et occupe la partie située du côté du bord de départ.
     * @param position fraction (0..1) de la taille du panneau, mesurée depuis son bord gauche/haut
     * @returns l'id du nouveau panneau, ou null
     */
    function splitLeaf(leafId: string, edge: PanelEdge, position: number): string | null {
        const leaf = findLeaf(leafId);
        if (!leaf) return null;

        const fresh: PanelLeaf = {
            id: uid(),
            kind: 'leaf',
            editor: leaf.editor,
            view: cloneView(leaf.view)
        };
        const newIsFirst = edge === 'left' || edge === 'top';

        const split: PanelSplit = {
            id: uid(),
            kind: 'split',
            direction: EDGE_DIRECTION[edge],
            ratio: clamp(position, 0.05, 0.95),
            first: newIsFirst ? fresh : leaf,
            second: newIsFirst ? leaf : fresh
        };

        replaceNode(leaf, split);
        return fresh.id;
    }

    /** Ferme un panneau : son voisin reprend toute la place. Le dernier panneau ne se ferme pas. */
    function closeLeaf(leafId: string): boolean {
        if (!findLeaf(leafId)) return false;
        const parent = findParent(leafId);
        if (!parent) return false;
        const sibling = parent.first.id === leafId ? parent.second : parent.first;
        replaceNode(parent, sibling);
        return true;
    }

    /** Change l'éditeur d'un panneau (son point de vue repart de zéro) */
    function setEditor(leafId: string, editorId: string) {
        const leaf = findLeaf(leafId);
        if (!leaf || leaf.editor === editorId) return;
        leaf.editor = editorId;
        leaf.view = createView();
    }

    function setView(leafId: string, patch: PanelViewPatch) {
        const leaf = findLeaf(leafId);
        if (leaf) applyViewPatch(leaf.view, patch);
    }

    /**
     * Redimensionne une division. Si `totalPx` (taille de la division sur son axe) est fourni,
     * le ratio est limité pour respecter la taille minimale des deux côtés.
     */
    function resize(splitId: string, ratio: number, totalPx?: number) {
        const split = findSplit(splitId);
        if (!split) return;
        const axis: PanelAxis = split.direction === 'column' ? 'x' : 'y';
        let r = clamp(ratio, 0, 1);
        if (totalPx) {
            const minA = minSize(split.first, axis);
            const minB = minSize(split.second, axis);
            if (totalPx >= minA + minB) r = clamp(r, minA / totalPx, 1 - minB / totalPx);
        }
        split.ratio = r;
    }

    // ── Disposition calculée (à plat) ──────────────────────────────────────
    // Chaque panneau reçoit son rectangle en % : le conteneur les affiche à plat, avec l'id
    // du panneau comme clé. Le DOM d'un panneau n'est donc jamais recréé quand l'arbre change
    // (le point de vue — défilement, curseur — est conservé).
    const layout = computed(() => {
        const leaves: PanelLeafLayout[] = [];
        const dividers: PanelDividerLayout[] = [];

        const walk = (node: PanelNode, rect: PanelRect) => {
            if (node.kind === 'leaf') {
                leaves.push({ id: node.id, node, rect });
                return;
            }
            if (node.direction === 'column') {
                const w1 = rect.w * node.ratio;
                walk(node.first, { x: rect.x, y: rect.y, w: w1, h: rect.h });
                walk(node.second, { x: rect.x + w1, y: rect.y, w: rect.w - w1, h: rect.h });
                dividers.push({ id: node.id, direction: 'column', line: rect.x + w1, rect });
            } else {
                const h1 = rect.h * node.ratio;
                walk(node.first, { x: rect.x, y: rect.y, w: rect.w, h: h1 });
                walk(node.second, { x: rect.x, y: rect.y + h1, w: rect.w, h: rect.h - h1 });
                dividers.push({ id: node.id, direction: 'row', line: rect.y + h1, rect });
            }
        };

        walk(root.value, { x: 0, y: 0, w: 100, h: 100 });
        return { leaves, dividers };
    });

    const canClose = computed(() => root.value.kind === 'split');

    // ── Sérialisation & sauvegarde ─────────────────────────────────────────
    const toJSON = (): PanelNode => JSON.parse(JSON.stringify(root.value));

    function load(layoutJson: unknown): boolean {
        const node = sanitize(layoutJson);
        if (!node) return false;
        root.value = node;
        return true;
    }

    function reset() {
        root.value = sanitize(options.initialLayout) ?? createLeaf(options.defaultEditor);
    }

    if (options.persist) {
        let timer: ReturnType<typeof setTimeout> | null = null;
        watch(root, () => {
            if (timer) clearTimeout(timer);
            timer = setTimeout(() => options.persist!.save(toJSON()), 300);
        }, { deep: true });
        onScopeDispose(() => { if (timer) clearTimeout(timer); });
    }

    return {
        root,
        layout,
        canClose,
        minLeafSize,
        minSize,
        splitLeaf,
        closeLeaf,
        setEditor,
        setView,
        resize,
        toJSON,
        load,
        reset
    };
}

export type GenesisPanel = ReturnType<typeof useGenesisPanel>;

// ============================================================================
// POINT DE VUE PAR PANNEAU (pour les éditeurs)
// ============================================================================

export interface PanelViewContext {
    view: ComputedRef<PanelView>;
    update: (patch: PanelViewPatch) => void;
}

export const PANEL_VIEW_KEY: InjectionKey<PanelViewContext> = Symbol('genesis-panel-view');

/**
 * À appeler depuis un éditeur affiché dans un panneau pour lire / écrire son point de vue
 * propre à ce panneau (ex. position de défilement d'une zone interne, curseur) :
 *
 *   const { view, update } = usePanelView();
 *   update({ extra: { editorScrollTop: 120 } });
 *
 * Hors d'un panneau, retourne un état local (le composant reste utilisable seul).
 */
export function usePanelView(): PanelViewContext {
    return inject(
        PANEL_VIEW_KEY,
        () => {
            const local = ref<PanelView>(createView());
            return {
                view: computed(() => local.value),
                update: (patch: PanelViewPatch) => applyViewPatch(local.value, patch)
            };
        },
        true
    );
}