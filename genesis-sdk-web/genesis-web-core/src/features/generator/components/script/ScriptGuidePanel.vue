<template>
    <CarrouselPanel class="h-full" :slides="slides" />
</template>

<script setup lang="ts">
import CarrouselPanel from '@genesis-labs/web-core/core/components/ui/carrousel/CarrouselPanel.vue';
import type { CarouselSlide } from '@genesis-labs/web-core/core/composables/ux/useCarousel';

// Contenu provisoire : à remplacer par les vraies étapes du tutoriel (textes, captures).
const slides: CarouselSlide[] = [
    { color: '#3B82F6', label: "1 · Choisissez votre script SQL" },
    { color: '#10B981', label: "2 · Ouvrez-le dans l'éditeur de code" },
    { color: '#8B5CF6', label: "3 · Demandez de l'aide à l'assistant IA" },
    { color: '#F59E0B', label: "4 · Divisez les panneaux : Alt + glisser depuis un bord" },
];
</script>