<template>
    <div class="relative">
        <div
            v-for="layer in LAYERS"
            :key="layer"
            :class="[
                layerClass,
                layer === 'fill' && [
                    'gfx-fill-layer absolute inset-0 pointer-events-none select-none text-primary',
                    { 'is-selected': selected }
                ]
            ]"
            :aria-hidden="layer === 'fill' ? 'true' : undefined"
            :inert="layer === 'fill' ? true : undefined"
        >
            <slot />
        </div>
    </div>
</template>

<script setup lang="ts">
import './genesisSelectFx.css';

/**
 * Rend son contenu deux fois, superposé :
 * - 'base' : couche normale
 * - 'fill' : même contenu, couleur de la marque, révélé de bas en haut (clip-path)
 *
 * Les deux couches partagent la même structure, donc la progression du remplissage
 * est identique pour le logo et pour le texte.
 *
 * Les classes de mise en page (colonne pour la carte, ligne pour la liste) sont
 * fournies via `layerClass`. Le `class` posé sur le composant s'applique à la racine.
 */
withDefaults(defineProps<{
    selected?: boolean;
    layerClass?: string;
}>(), {
    selected: false,
    layerClass: ''
});

const LAYERS = ['base', 'fill'] as const;
</script>