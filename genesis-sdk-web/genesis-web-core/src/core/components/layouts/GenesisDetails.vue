<template>
    <!-- Contenu d'un élément (framework, base, frontend…). Pas de cadre ni de défilement :
         c'est le panneau qui les fournit, le défilement est donc mémorisé par panneau. -->
    <article class="flex flex-col gap-12 p-4 pb-16 overflow-auto">
        <header class="flex flex-col items-center gap-4 min-w-0 mb-4">
            <!-- Logo : slot #logo (comme GenesisCard) > image > carré de couleur -->
            <div class="shrink-0 rounded flex items-center justify-center overflow-hidden mb-4">
                <slot name="logo"></slot>
            </div>

            <h2 class="text-xl font-medium leading-tight text-text break-words min-w-0">{{ title }}</h2>
        </header>

        <!-- Chaque feature met son contenu ici -->
        <div class="flex flex-col gap-4">
            <slot />
        </div>
    </article>
</template>

<script setup lang="ts">
defineProps<{
    title: string;
    /** URL de l'image d'en-tête. Vide : le carré de couleur reste seul (sauf si le slot #logo est fourni). */
    image?: string;
}>();
</script>