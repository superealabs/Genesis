<template>
    <div 
        class="relative w-full flex-shrink-0 overflow-hidden bg-bg rounded-t-lg"
        :style="{ height: height }"
    >
        <!-- Slides -->
        <div 
            class="flex h-full transition-transform duration-500 ease-in-out"
            :style="{ transform: `translateX(-${currentSlide * 100}%)` }"
        >
            <div 
                v-for="(slide, index) in slides" 
                :key="index"
                class="w-full h-full flex-shrink-0 flex items-center justify-center relative"
            >
                <!-- Support pour les images (futur) ou les couleurs (démo) -->
                <img 
                    v-if="slide.image" 
                    :src="slide.image" 
                    :alt="slide.label || 'Slide'"
                    class="w-full h-full object-cover"
                />
                <!-- Fallback couleur pour la démo -->
                <div 
                    v-else
                    class="w-full h-full flex items-center justify-center"
                    :style="{ backgroundColor: slide.color || '#3B82F6' }"
                >
                    <span v-if="slide.label" class="text-white text-lg font-semibold drop-shadow-md">
                        {{ slide.label }}
                    </span>
                </div>
            </div>
        </div>

        <!-- Bouton Précédent -->
        <button
            v-if="slides.length > 1"
            class="absolute left-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
            @click="prevSlide"
            aria-label="Slide précédent"
        >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
        </button>

        <!-- Bouton Suivant -->
        <button
            v-if="slides.length > 1"
            class="absolute right-2 top-1/2 -translate-y-1/2 w-8 h-8 rounded-full bg-black/40 hover:bg-black/60 text-white flex items-center justify-center transition-colors z-10 backdrop-blur-sm"
            @click="nextSlide"
            aria-label="Slide suivant"
        >
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 18 15 12 9 6"></polyline></svg>
        </button>

        <!-- Indicateurs (dots) -->
        <div v-if="slides.length > 1" class="absolute bottom-3 left-1/2 -translate-x-1/2 flex gap-2 z-10">
            <button
                v-for="(_, index) in slides"
                :key="index"
                class="h-2 rounded-full transition-all duration-300"
                :class="index === currentSlide ? 'bg-white w-6' : 'bg-white/50 hover:bg-white/80 w-2'"
                @click="goToSlide(index)"
                :aria-label="`Aller au slide ${index + 1}`"
            />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue';

export interface CarouselSlide {
    color?: string;
    image?: string;
    label?: string;
}

const props = withDefaults(defineProps<{
    slides: CarouselSlide[];
    height?: string;
    autoPlay?: boolean;
    interval?: number;
}>(), {
    height: '200px',
    autoPlay: true,
    interval: 4000
});

const currentSlide = ref(0);
let autoPlayInterval: ReturnType<typeof setInterval> | null = null;

function nextSlide() {
    currentSlide.value = (currentSlide.value + 1) % props.slides.length;
}

function prevSlide() {
    currentSlide.value = (currentSlide.value - 1 + props.slides.length) % props.slides.length;
}

function goToSlide(index: number) {
    currentSlide.value = index;
    resetAutoPlay();
}

function startAutoPlay() {
    if (props.autoPlay && props.slides.length > 1) {
        autoPlayInterval = setInterval(nextSlide, props.interval);
    }
}

function stopAutoPlay() {
    if (autoPlayInterval) {
        clearInterval(autoPlayInterval);
        autoPlayInterval = null;
    }
}

function resetAutoPlay() {
    stopAutoPlay();
    startAutoPlay();
}

// Gestion du cycle de vie pour l'autoplay
watch(() => props.autoPlay, (newValue) => {
    if (newValue) startAutoPlay();
    else stopAutoPlay();
});

onMounted(() => {
    startAutoPlay();
});

onUnmounted(() => {
    stopAutoPlay();
});
</script>