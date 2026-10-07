<template>
    <div class="w-full h-full bg-bg-light p-2 rounded-lg">
        <div
            ref="rootEl"
            class="relative w-full h-full"
            :class="{ 'select-none': gesture || resizing }"
        >
            <!-- ═══ PANNEAUX ═══
                Affichage à plat : la clé est l'id du panneau, donc son DOM (et son point de vue :
                défilement, curseur) n'est jamais recréé quand l'arbre change (division, fermeture). -->
            <div
                v-for="leaf in layout.leaves"
                :key="leaf.id"
                class="absolute"
                :style="leafBoxStyle(leaf.rect)"
            >    
                <GenesisPanelElement
                    class="w-full h-full"
                    :editors="editors"
                    :editor-id="leaf.node.editor"
                    :view="leaf.node.view"
                    :can-close="canClose"
                    @update:editor-id="panel.setEditor(leaf.id, $event)"
                    @update:view="panel.setView(leaf.id, $event)"
                    @close="panel.closeLeaf(leaf.id)"
                >
                    <component
                        :is="editorMap.get(leaf.node.editor)!.component"
                        v-if="editorMap.get(leaf.node.editor)"
                        v-bind="editorMap.get(leaf.node.editor)!.props"
                    />
                    <div v-else class="p-4 text-sm text-text-muted">
                        Éditeur indisponible (« {{ leaf.node.editor }} »). Choisissez-en un autre dans la liste.
                    </div>
                </GenesisPanelElement>
            </div>

            <!-- ═══ DIVISEURS (redimensionnement) ═══ -->
            <div
                v-for="d in layout.dividers"
                :key="d.id"
                class="group absolute z-20"
                :class="[
                    d.direction === 'column' ? 'cursor-col-resize' : 'cursor-row-resize',
                    { 'pointer-events-none': altDown }
                ]"
                :style="dividerStyle(d)"
                title="Glisser pour redimensionner (double-clic : 50 / 50)"
                @pointerdown="startResize($event, d)"
                @dblclick="resetDivider(d)"
            >
                <div
                    class="absolute group-hover:bg-neutral-light-genesis transition-all"
                    :class="d.direction === 'column'
                        ? 'top-0 bottom-0 left-1/2 -translate-x-1/2 w-px group-hover:w-[3px]'
                        : 'left-0 right-0 top-1/2 -translate-y-1/2 h-px group-hover:h-[3px]'"
                />
            </div>

            <!-- ═══ ZONES DE BORD (visibles uniquement quand Alt est enfoncé) ═══
                Elles n'existent que pendant l'appui sur Alt : le reste du temps, les barres de défilement
                et le contenu des bords restent utilisables normalement. -->
            <div
                v-for="zone in edgeZones"
                :key="zone.key"
                class="absolute z-30 bg-accent/20 hover:bg-accent/50 transition-colors"
                :class="zone.cursor"
                :style="zone.style"
                @pointerdown="startSplit($event, zone.leaf, zone.edge)"
            />

            <!-- ═══ APERÇU DE LA DIVISION ═══ -->
            <template v-if="preview">
                <div
                    class="absolute z-30 pointer-events-none rounded-sm"
                    :class="preview.valid ? 'bg-accent/15' : 'bg-red-500/20'"
                    :style="preview.region"
                />
                <div
                    v-if="preview.line"
                    class="absolute z-30 pointer-events-none transition-colors"
                    :class="preview.snap ? 'bg-accent' : 'bg-accent/60'"
                    :style="preview.line"
                />
            </template>

            <!-- Voile pendant un geste : garde le curseur et empêche le contenu de capter les événements -->
            <div
                v-if="gesture || resizing"
                class="absolute inset-0 z-40"
                :class="overlayCursor"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import GenesisPanelElement from './GenesisPanelElement.vue';
import {
    EDGE_AXIS,
    EDGE_DIRECTION,
    SNAP_DISTANCE,
    resolveSplitPosition,
    type GenesisPanel,
    type PanelAxis,
    type PanelDirection,
    type PanelDividerLayout,
    type PanelEdge,
    type PanelEditorDefinition,
    type PanelLeafLayout,
    type PanelRect,
    type SplitSnap
} from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

// ============================================================================
// 1. PROPS & CONSTANTES
// ============================================================================
const props = withDefaults(defineProps<{
    /** Instance créée par useGenesisPanel() dans la vue */
    panel: GenesisPanel;
    /** Registre des éditeurs disponibles */
    editors: PanelEditorDefinition[];
    gap?: number;
}>(), {
    gap:8
});

/** Épaisseur (px) des zones de bord actives avec Alt */
const EDGE_ZONE = 10;
/** Épaisseur (px) de la zone de saisie d'un diviseur */
const dividerHit = props.gap;

// ============================================================================
// 2. ÉTAT
// ============================================================================
interface SplitGesture {
    leafId: string;
    edge: PanelEdge;
    axis: PanelAxis;
    direction: PanelDirection;
    rect: PanelRect;
    origin: number;
    size: number;
    position: number;
    snap: SplitSnap;
    valid: boolean;
}

const rootEl = ref<HTMLElement | null>(null);
const altDown = ref(false);
const gesture = ref<SplitGesture | null>(null);
const resizing = ref<PanelDirection | null>(null);

const layout = computed(() => props.panel.layout.value);
const canClose = computed(() => props.panel.canClose.value);
const editorMap = computed(() => new Map(props.editors.map(e => [e.id, e])));
const EPS = 0.01;

// ============================================================================
// 3. GÉOMÉTRIE
// ============================================================================
/** Rectangle du panneau + moitié de l'écart sur chaque côté qui touche un voisin (pas sur les bords extérieurs) */
function leafBoxStyle(r: PanelRect) {
    const half = `${props.gap / 2}px`;
    return {
        ...pctStyle(r),
        paddingLeft: r.x > EPS ? half : '0',
        paddingRight: r.x + r.w < 100 - EPS ? half : '0',
        paddingTop: r.y > EPS ? half : '0',
        paddingBottom: r.y + r.h < 100 - EPS ? half : '0'
    };
}

function pctStyle(r: PanelRect) {
    return { left: `${r.x}%`, top: `${r.y}%`, width: `${r.w}%`, height: `${r.h}%` };
}

function dividerStyle(d: PanelDividerLayout) {
    const r = d.rect;
    const half = dividerHit / 2;
    return d.direction === 'column'
        ? { left: `calc(${d.line}% - ${half}px)`, top: `${r.y}%`, width: `${dividerHit}px`, height: `${r.h}%` }
        : { top: `calc(${d.line}% - ${half}px)`, left: `${r.x}%`, height: `${dividerHit}px`, width: `${r.w}%` };
}

/** Origine et taille (px, à l'écran) d'un rectangle en % sur un axe */
function pixelSpan(rect: PanelRect, axis: PanelAxis) {
    const c = rootEl.value!.getBoundingClientRect();
    return axis === 'x'
        ? { origin: c.left + (rect.x / 100) * c.width, size: (rect.w / 100) * c.width }
        : { origin: c.top + (rect.y / 100) * c.height, size: (rect.h / 100) * c.height };
}

// ============================================================================
// 4. ÉCOUTEURS GLOBAUX (un seul geste à la fois)
// ============================================================================
let stopListening: (() => void) | null = null;

function listen(onMove: (e: PointerEvent) => void, onUp: (e: PointerEvent) => void) {
    stopListening?.();
    const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') cancel(); };
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp);
    window.addEventListener('pointercancel', cancel);
    window.addEventListener('keydown', onKey, true);
    stopListening = () => {
        window.removeEventListener('pointermove', onMove);
        window.removeEventListener('pointerup', onUp);
        window.removeEventListener('pointercancel', cancel);
        window.removeEventListener('keydown', onKey, true);
        stopListening = null;
    };
}

function cancel() {
    gesture.value = null;
    resizing.value = null;
    stopListening?.();
}

// ============================================================================
// 5. REDIMENSIONNEMENT PAR LES DIVISEURS
// ============================================================================
function startResize(e: PointerEvent, d: PanelDividerLayout) {
    if (e.button !== 0 || altDown.value) return;
    e.preventDefault();

    const axis: PanelAxis = d.direction === 'column' ? 'x' : 'y';
    const { origin, size } = pixelSpan(d.rect, axis);

    resizing.value = d.direction;
    listen(
        (ev) => props.panel.resize(d.id, ((axis === 'x' ? ev.clientX : ev.clientY) - origin) / size, size),
        cancel
    );
}

function resetDivider(d: PanelDividerLayout) {
    const axis: PanelAxis = d.direction === 'column' ? 'x' : 'y';
    props.panel.resize(d.id, 0.5, pixelSpan(d.rect, axis).size);
}

// ============================================================================
// 6. DIVISION : Alt + appui sur un bord, glisser vers l'intérieur, relâcher
// ============================================================================
let usedAlt = false;

function startSplit(e: PointerEvent, leaf: PanelLeafLayout, edge: PanelEdge) {
    if (e.button !== 0 || !e.altKey) return;
    e.preventDefault();

    const axis = EDGE_AXIS[edge];
    const { origin, size } = pixelSpan(leaf.rect, axis);
    const min = props.panel.minLeafSize(axis);

    const update = (ev: { clientX: number; clientY: number }) => {
        const raw = (axis === 'x' ? ev.clientX : ev.clientY) - origin;
        const r = resolveSplitPosition(size, raw, min, SNAP_DISTANCE);
        gesture.value = {
            leafId: leaf.id,
            edge,
            axis,
            direction: EDGE_DIRECTION[edge],
            rect: leaf.rect,
            origin,
            size,
            position: r.position,
            snap: r.snap,
            valid: r.valid
        };
    };

    update(e);
    usedAlt = true;

    listen(update, () => {
        const g = gesture.value;
        cancel();
        if (g?.valid) props.panel.splitLeaf(g.leafId, g.edge, g.position / g.size);
    });
}

/** Zones de bord : une par côté et par panneau, uniquement pendant Alt */
const edgeZones = computed(() => {
    if (!altDown.value || gesture.value) return [];

    const Z = `${EDGE_ZONE}px`;
    const edges: PanelEdge[] = ['left', 'right', 'top', 'bottom'];

    return layout.value.leaves.flatMap(leaf => {
        const { x, y, w, h } = leaf.rect;
        return edges.map(edge => {
            // Les zones latérales s'arrêtent avant les coins : les coins reviennent à haut / bas
            const style = {
                left: { left: `${x}%`, top: `calc(${y}% + ${Z})`, width: Z, height: `calc(${h}% - ${Z} * 2)` },
                right: { left: `calc(${x + w}% - ${Z})`, top: `calc(${y}% + ${Z})`, width: Z, height: `calc(${h}% - ${Z} * 2)` },
                top: { left: `${x}%`, top: `${y}%`, width: `${w}%`, height: Z },
                bottom: { left: `${x}%`, top: `calc(${y + h}% - ${Z})`, width: `${w}%`, height: Z }
            }[edge];

            return {
                key: `${leaf.id}-${edge}`,
                leaf,
                edge,
                style,
                cursor: EDGE_AXIS[edge] === 'x' ? 'cursor-col-resize' : 'cursor-row-resize'
            };
        });
    });
});

/** Aperçu : ligne de division + région du futur panneau */
const preview = computed(() => {
    const g = gesture.value;
    if (!g) return null;

    const r = g.rect;
    if (!g.valid) return { valid: false, snap: null as SplitSnap, region: pctStyle(r), line: null };

    const frac = g.position / g.size;
    const newFirst = g.edge === 'left' || g.edge === 'top';

    if (g.axis === 'x') {
        const lineX = r.x + r.w * frac;
        return {
            valid: true,
            snap: g.snap,
            region: pctStyle({ x: newFirst ? r.x : lineX, y: r.y, w: newFirst ? r.w * frac : r.w * (1 - frac), h: r.h }),
            line: { left: `calc(${lineX}% - 1px)`, top: `${r.y}%`, width: '2px', height: `${r.h}%` }
        };
    }

    const lineY = r.y + r.h * frac;
    return {
        valid: true,
        snap: g.snap,
        region: pctStyle({ x: r.x, y: newFirst ? r.y : lineY, w: r.w, h: newFirst ? r.h * frac : r.h * (1 - frac) }),
        line: { top: `calc(${lineY}% - 1px)`, left: `${r.x}%`, height: '2px', width: `${r.w}%` }
    };
});

const overlayCursor = computed(() => {
    const dir = gesture.value?.direction ?? resizing.value;
    return dir === 'column' ? 'cursor-col-resize' : 'cursor-row-resize';
});

// ============================================================================
// 7. SUIVI DE LA TOUCHE ALT
// ============================================================================
function onKeyDown(e: KeyboardEvent) {
    if (e.key === 'Alt') altDown.value = true;
}

function onKeyUp(e: KeyboardEvent) {
    if (e.key !== 'Alt') return;
    altDown.value = false;
    // Sur Windows / Linux, relâcher Alt seul peut activer la barre de menus de l'application :
    // on l'évite uniquement si Alt vient de servir à un geste.
    if (usedAlt) {
        e.preventDefault();
        usedAlt = false;
    }
}

function onBlur() {
    altDown.value = false;
}

onMounted(() => {
    window.addEventListener('keydown', onKeyDown);
    window.addEventListener('keyup', onKeyUp);
    window.addEventListener('blur', onBlur);
});

onBeforeUnmount(() => {
    cancel();
    window.removeEventListener('keydown', onKeyDown);
    window.removeEventListener('keyup', onKeyUp);
    window.removeEventListener('blur', onBlur);
});
</script>