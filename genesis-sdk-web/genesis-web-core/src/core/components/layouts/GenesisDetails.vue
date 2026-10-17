<template>
    <!-- Contenu d'un élément (framework, base, frontend…). Pas de cadre ni de défilement :
         c'est le panneau qui les fournit, le défilement est donc mémorisé par panneau. -->
    <article class="flex flex-col gap-6 p-4">
        <header class="flex items-center gap-4 min-w-0">
            <!-- Logo : slot #logo (comme GenesisCard) > image > carré de couleur -->
            <div class="w-12 h-12 shrink-0 rounded flex items-center justify-center overflow-hidden bg-bg-secondary">
                <slot name="logo">
                    <img v-if="image" :src="image" alt="" class="w-full h-full object-contain" draggable="false" />
                </slot>
            </div>

            <h2 class="text-xl font-bold leading-tight text-text break-words min-w-0">{{ title }}</h2>
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