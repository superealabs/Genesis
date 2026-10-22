<template>
    <!-- Pas d'overflow-hidden sur la racine ni de z-index sur l'en-tête : le menu déroulant
         de l'en-tête doit pouvoir déborder sur le contenu et sur les panneaux voisins. -->
    <section
        class="flex flex-col gap-2 min-w-0 min-h-0 rounded-lg bg-bg-dark p-2"
        :aria-label="current?.label"
    >
        <!-- ═══ En-tête : sélecteur d'éditeur, puis bouton de fermeture à sa droite ═══ -->
         <!-- désactivation intentionnel de la taille maximale, = h-8 -->
        <header class="relative flex justify-between gap-1 shrink-0">
            <!-- GenesisInput select : passe tout seul en combobox (recherche) à partir de 7 options -->
            <GenesisInput
                type="select"
                :size="dropdownSize"
                fill-width
                class="flex-1 min-w-0"
                :model-value="editorId"
                :options="editorOptions"
                placeholder="Choisir un éditeur…"
                @update:model-value="select"
            />

            <GenesisButtonIcon
                variant="tertiary"
                size="sm"
                :disabled="!canClose"
                :title="canClose ? 'Fermer le panneau' : 'Le dernier panneau ne peut pas être fermé'"
                @click="$emit('close')"
            >
                <IconX />
            </GenesisButtonIcon>
        </header>

        <!-- ═══ Contenu : sa position de défilement est mémorisée dans le point de vue du panneau ═══ -->
        <div
            ref="scrollEl"
            class="flex-1 min-h-0 overflow-auto"
            @scroll.passive="onScroll"
        >
            <slot />
        </div>
    </section>
</template>

<script setup lang="ts">
import { ref, computed, provide, watch, nextTick, onMounted, onBeforeUnmount } from 'vue';
import GenesisInput from '@genesis-labs/web-core/core/components/ui/inputs/GenesisInput.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import {
    PANEL_VIEW_KEY,
    type PanelEditorDefinition,
    type PanelView,
    type PanelViewPatch
} from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';
import type { UI_Size_Unit } from '../../config/ui.config';

const props = withDefaults(defineProps<{
    /** Éditeurs proposés dans le menu */
    editors: PanelEditorDefinition[];
    /** Éditeur actuellement affiché */
    editorId: string;
    /** Point de vue de CE panneau (défilement, etc.) */
    view: PanelView;
    canClose: boolean;
    dropdownSize?: UI_Size_Unit;
}>(), {
    dropdownSize: 'sm'
});

const emit = defineEmits<{
    'update:editorId': [id: string];
    'update:view': [patch: PanelViewPatch];
    close: [];
}>();

const current = computed(() => props.editors.find(e => e.id === props.editorId));

// GenesisInput attend { label, value }
const editorOptions = computed(() => props.editors.map(e => ({ label: e.label, value: e.id })));

function select(id: string) {
    if (id !== props.editorId) emit('update:editorId', id);
}

// ═══ Point de vue : défilement du contenu ═══
const scrollEl = ref<HTMLElement | null>(null);
let frame = 0;

function onScroll() {
    cancelAnimationFrame(frame);
    frame = requestAnimationFrame(() => {
        const el = scrollEl.value;
        if (el) emit('update:view', { scrollTop: el.scrollTop, scrollLeft: el.scrollLeft });
    });
}

function restoreScroll() {
    const el = scrollEl.value;
    if (!el) return;
    el.scrollTop = props.view.scrollTop;
    el.scrollLeft = props.view.scrollLeft;
}

// Deux passes : le contenu peut ne pas avoir sa hauteur définitive au premier rendu
onMounted(() => {
    restoreScroll();
    requestAnimationFrame(restoreScroll);
});

// Changer d'éditeur remet le point de vue à zéro (fait par le composable) : on le réapplique
watch(() => props.editorId, () => nextTick(restoreScroll));

onBeforeUnmount(() => cancelAnimationFrame(frame));

// Les éditeurs qui ont leur propre zone de défilement lisent / écrivent leur point de vue via usePanelView()
provide(PANEL_VIEW_KEY, {
    view: computed(() => props.view),
    update: (patch: PanelViewPatch) => emit('update:view', patch)
});
</script>