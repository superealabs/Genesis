<template>
    <!-- Pas d'overflow-hidden sur la racine ni de z-index sur l'en-tête : le menu déroulant
         de l'en-tête doit pouvoir déborder sur le contenu et sur les panneaux voisins. -->
    <section
        class="flex flex-col min-w-0 min-h-0 bg-bg border border-secondary/60"
        :aria-label="current?.label"
    >
        <!-- ═══ En-tête : sélecteur d'éditeur, puis bouton de fermeture à sa droite ═══ -->
        <header class="relative flex items-center justify-between gap-1 h-8 px-1 flex-shrink-0 bg-bg-light border-b border-secondary/60">
            <GenesisDropdown
                trigger-variant="tertiary"
                trigger-size="sm"
                dropdown-size="md"
                align="left"
            >
                <template v-if="current?.icon" #triggerIcon>
                    <component :is="current.icon" />
                </template>

                <template #trigger>
                    <span class="truncate">{{ current?.label ?? 'Choisir un éditeur…' }}</span>
                </template>

                <template #default="{ close }">
                    <div class="p-1 space-y-1">
                        <button
                            v-for="editor in editors"
                            :key="editor.id"
                            type="button"
                            class="w-full flex items-center gap-2 text-left px-3 py-2 text-sm text-text hover:bg-[var(--color-hover-ghost)] rounded-md transition-colors"
                            :class="{ 'text-accent font-medium': editor.id === editorId }"
                            @click="select(editor.id, close)"
                        >
                            <component :is="editor.icon" v-if="editor.icon" class="w-4 h-4 flex-shrink-0" />
                            <span class="truncate">{{ editor.label }}</span>
                        </button>
                    </div>
                </template>
            </GenesisDropdown>

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
import GenesisDropdown from '@genesis-labs/web-core/core/components/ui/dropdown/GenesisDropdown.vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import {
    PANEL_VIEW_KEY,
    type PanelEditorDefinition,
    type PanelView,
    type PanelViewPatch
} from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

const props = defineProps<{
    /** Éditeurs proposés dans le menu */
    editors: PanelEditorDefinition[];
    /** Éditeur actuellement affiché */
    editorId: string;
    /** Point de vue de CE panneau (défilement, etc.) */
    view: PanelView;
    canClose: boolean;
}>();

const emit = defineEmits<{
    'update:editorId': [id: string];
    'update:view': [patch: PanelViewPatch];
    close: [];
}>();

const current = computed(() => props.editors.find(e => e.id === props.editorId));

function select(id: string, close: () => void) {
    if (id !== props.editorId) emit('update:editorId', id);
    close();
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