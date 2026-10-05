<template>
    <!-- Remplit entièrement son parent : la taille est décidée par le conteneur, pas par ce composant -->
    <div class="relative w-full h-full overflow-hidden rounded-lg bg-bg">
        <CarouselBackground
            :slides="slides"
            :current-slide="currentSlide"
        />

        <CarouselOverlay
            :count="slides.length"
            :current="currentSlide"
            :label="currentActiveSlide?.label"
            @prev="prev"
            @next="next"
            @go="goTo"
        />
    </div>
</template>

<script setup lang="ts">
import {
    useCarousel,
    type CarouselSlide
} from '@genesis-labs/web-core/core/composables/ux/useCarousel';
import CarouselBackground from './CarouselBackground.vue';
import CarouselOverlay from './CarouselOverlay.vue';

const props = withDefaults(defineProps<{
    slides: CarouselSlide[];
    autoPlay?: boolean;
    interval?: number;
}>(), {
    autoPlay: true,
    interval: 4000,
});

const emit = defineEmits<{
    'slide-change': [slide: CarouselSlide, direction: 'left' | 'right'];
}>();

const { currentSlide, currentActiveSlide, next, prev, goTo } = useCarousel({
    slides: () => props.slides,
    autoPlay: () => props.autoPlay,
    interval: () => props.interval,
    onChange: (slide, direction) => emit('slide-change', slide, direction),
});
</script>