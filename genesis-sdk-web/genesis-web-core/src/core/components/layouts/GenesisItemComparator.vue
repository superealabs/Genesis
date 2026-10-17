<template>
    <section class="flex flex-col w-full h-full min-h-0 gap-2">
        <!-- ═══ Barre du haut : titre à gauche, fermeture de TOUTE la comparaison à droite ═══
             Sur sa propre ligne : elle ne recouvre pas le bouton de fermeture du dernier panneau. -->
        <header class="flex items-center justify-between gap-2 h-8 shrink-0">
            <h1 class="text-sm font-semibold text-text truncate">{{ title }}</h1>

            <GenesisButtonIcon variant="tertiary" size="sm" title="Fermer la comparaison" @click="$emit('close')">
                <IconX />
            </GenesisButtonIcon>
        </header>

        <div class="flex-1 min-h-0">
            <GenesisPanelContainer :panel="panel" :editors="editors" />
        </div>
    </section>
</template>

<script setup lang="ts">
import { watch } from 'vue';
import GenesisButtonIcon from '@genesis-labs/web-core/core/components/ui/actions/GenesisButtonIcon.vue';
import IconX from '@genesis-labs/web-core/core/components/ui/icons/IconX.vue';
import GenesisPanelContainer from '@genesis-labs/web-core/core/components/ux/GenesisPanelContainer.vue';
import {
    useGenesisPanel,
    type PanelEditorDefinition
} from '@genesis-labs/web-core/core/composables/ux/UseGenesisPanel';

const props = withDefaults(defineProps<{
    /** Un éditeur par élément comparable (c'est la liste du sélecteur de chaque colonne) */
    editors: PanelEditorDefinition[];
    /** Ids des éditeurs à afficher au départ, un par colonne, dans l'ordre. Un même id peut revenir. */
    initialEditorIds: string[];
    title?: string;
}>(), {
    title: 'Comparaison'
});

const emit = defineEmits<{ close: [] }>();

/**
 * Colonnes de largeur égale : [a, b, c] devient split(1/3, a, split(1/2, b, c)).
 * `useGenesisPanel` accepte un arbre simplifié (les id et points de vue sont générés).
 */
function columns(ids: string[]): unknown {
    const [head, ...rest] = ids;
    const leaf = { kind: 'leaf', editor: head };
    return rest.length === 0
        ? leaf
        : { kind: 'split', direction: 'column', ratio: 1 / ids.length, first: leaf, second: columns(rest) };
}

// Pris une seule fois : ensuite, c'est l'utilisateur qui pilote les colonnes
const panel = useGenesisPanel({
    defaultEditor: props.initialEditorIds[0],
    initialLayout: columns(props.initialEditorIds)
});

// Comparer demande au moins deux colonnes : en dessous, la vue se ferme d'elle-même
watch(
    () => panel.layout.value.leaves.length,
    (count) => { if (count < 2) emit('close'); },
    { immediate: true }
);
</script>